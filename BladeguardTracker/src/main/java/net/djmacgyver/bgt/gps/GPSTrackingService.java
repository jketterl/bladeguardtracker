package net.djmacgyver.bgt.gps;

import java.util.ArrayList;
import java.util.Iterator;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.activity.MainActivity;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.GPSUnavailableCommand;
import net.djmacgyver.bgt.socket.command.LogCommand;
import net.djmacgyver.bgt.socket.command.QuitCommand;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class GPSTrackingService extends Service implements LocationListener, KeepAliveTarget {
	private HttpSocketConnection conn;
	private KeepAliveThread gpsReminder;
	private boolean enabled = false;
	private LocationManager locationManager;
	private static final int NOTIFICATION = 1;
	private KeepAliveThread locationReminder;
	private boolean updateBlocked = false;
	private Location queuedLocation;
	private Location lastLocation;
	private ArrayList<GPSTrackingListener> listeners = new ArrayList<GPSTrackingListener>();
	private Event boundEvent;
	
	private int position = -1;
	private double distanceToFront = -1;
	private double distanceToEnd = -1;

	private SocketService sockService;
	private ServiceConnection sconn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			sockService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			sockService = ((SocketService.LocalBinder) service).getService();
		}
	};
	
	@Override
	public void onCreate() {
        //startService(new Intent(this, SocketService.class));
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
        
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void onDestroy() {
		unbindService(sconn);
		this.disable();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (!getGpsReminder().isAlive()) getGpsReminder().start();
		getGpsReminder().interrupt();
		sendLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (!provider.equals("gps")) return;
		sendGpsUnavailable();
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (!provider.equals("gps")) return;
		switch (status) {
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
			case LocationProvider.OUT_OF_SERVICE:
				sendGpsUnavailable();
				break;
		}
	}
	
	public void disable() {
		if (!enabled) return;
		
		fireTrackingDisabled();
		
		getLocationReminder().terminate();
		locationReminder = null;
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION);

		locationManager.removeUpdates(this);
		
		getGpsReminder().terminate();
		gpsReminder = null;
		
		// it is possible that the connection has been closed before we were able so send the "quit" message.
		try {
			conn.sendCommand(new QuitCommand(boundEvent));
		} catch (NullPointerException ignored) {}
		
		this.setPosition(-1);
		this.setDistanceToEnd(-1);
		this.setDistanceToFront(-1);
		
		sockService.removeStake(this);
		conn = null;
		
		enabled = false;
	}
	
	private void setDistanceToFront(double distance) {
		this.distanceToFront = distance;
		if (distance >= 0) fireDistanceToFront(distance); else fireDistanceToFrontLost();
	}

	private void setDistanceToEnd(double distance) {
		this.distanceToEnd = distance;
		if (distance >= 0) fireDistanceToEnd(distance); else fireDistanceToEndLost();
	}

	private void setPosition(int position) {
		this.position = position;
		if (position >= 0) firePositionLock(position); else firePositionLost();
	}

	private KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 60);
		}
		return gpsReminder;
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source == getGpsReminder()) {
			sendGpsUnavailable();
			getGpsReminder().terminate();
			gpsReminder = null;
		}
		if (source == getLocationReminder()) {
			updateBlocked = false;
			sendLocation();
		}
	}

	public void enable(Event event) {
		if (enabled) return;
		enabled = true;
		
		bindEvent(event);
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
		conn = sockService.getSharedConnection(this);
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(
				R.drawable.notification,
				getResources().getString(R.string.tracker_activated),
				System.currentTimeMillis()
		);
		Intent intent = new Intent(this, MainActivity.class);
		notification.setLatestEventInfo(
				getApplicationContext(),
				getResources().getString(R.string.app_name),
				getResources().getString(R.string.gps_ongoing),
				PendingIntent.getActivity(this, 0, intent, 0)
		);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		nm.notify(NOTIFICATION, notification);

		getLocationReminder().start();
		
		fireTrackingEnabled();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public class LocalBinder extends Binder {
		public GPSTrackingService getService() {
			return GPSTrackingService.this;
		}
	}
	
	private final Binder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	protected KeepAliveThread getLocationReminder() {
		if (locationReminder == null) {
			locationReminder = new KeepAliveThread(this, 5);
		}
		return locationReminder;
	}

	private void sendLocation() {
		if (queuedLocation == null) return;
		Location l = queuedLocation;
		queuedLocation = null;
		sendLocation(l);
	}

	public void sendLocation(Location location) {
		if (location.equals(lastLocation)) return;
		if (lastLocation != null && location.distanceTo(lastLocation) == 0) return;
		if (updateBlocked) {
			queuedLocation = location;
			return;
		}
	
		LogCommand log = new LogCommand(boundEvent, location);
		log.addCallback(new SocketCommandCallback() {
			@Override
			public void run(SocketCommand command) {
				LogCommand log = (LogCommand) command;
				setPosition(log.hasPosition() ? log.getPosition() : -1);
				setDistanceToFront(log.hasDistanceToFront() ? log.getDistanceToFront() : -1);
				setDistanceToEnd(log.hasDistanceToEnd() ? log.getDistanceToEnd() : -1);
			}
		});
		conn.sendCommand(log);
		
		getLocationReminder().interrupt();
		lastLocation = location;
		updateBlocked = true;
	}

	protected void firePositionLost() {
        for (GPSTrackingListener listener : listeners) listener.onPositionLost();
	}

	protected void firePositionLock(int position) {
        for (GPSTrackingListener listener : listeners) listener.onPositionLock(position);
	}
	
	protected void fireDistanceToEnd(double distance) {
        for (GPSTrackingListener listener : listeners) listener.onDistanceToEnd(distance);
	}

	protected void fireDistanceToFront(double distance) {
        for (GPSTrackingListener listener : listeners) listener.onDistanceToFront(distance);
	}
	
	protected void fireDistanceToEndLost() {
        for (GPSTrackingListener listener : listeners) listener.onDistanceToEndLost();
	}

	protected void fireDistanceToFrontLost() {
        for (GPSTrackingListener listener : listeners) listener.onDistanceToFrontLost();
	}

	private void sendGpsUnavailable() {
		conn.sendCommand(new GPSUnavailableCommand(boundEvent));
		lastLocation = null;
		setPosition(-1);
		setDistanceToEnd(-1);
		setDistanceToFront(-1);
	}
	
	public void addListener(GPSTrackingListener l) {
		if (listeners.contains(l)) return;
		listeners.add(l);
	}
	
	public void removeListener(GPSTrackingListener l) {
		if (!listeners.contains(l)) return;
		listeners.remove(l);
	}
	
	private void fireTrackingEnabled() {
        for (GPSTrackingListener listener : listeners) listener.trackingEnabled();
	}
	
	private void fireTrackingDisabled() {
        for (GPSTrackingListener listener : listeners) listener.trackingDisabled();
	}
	
	public void bindEvent(Event event) {
		this.boundEvent = event;
	}

	public int getPosition() {
		return position;
	}

	public double getDistanceToFront() {
		return distanceToFront;
	}

	public double getDistanceToEnd() {
		return distanceToEnd;
	}
}