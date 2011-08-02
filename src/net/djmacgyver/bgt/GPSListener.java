package net.djmacgyver.bgt;

import net.djmacgyver.bgt.activity.MainActivity;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import net.djmacgyver.bgt.upstream.Connection;
import net.djmacgyver.bgt.upstream.HttpStreamingConnection;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class GPSListener extends Service implements LocationListener, KeepAliveTarget {
	private Connection conn;
	private KeepAliveThread gpsReminder;
	private boolean enabled = false;
	private LocationManager locationManager;
	private static final int NOTIFICATION = 1;
	
	@Override
	public void onCreate() {
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void onDestroy() {
		this.disable();
	}
	
	private Connection getConnection() {
		if (conn == null) {
			conn = new HttpStreamingConnection(getApplicationContext());
			conn.connect();
		}
		return conn;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (!getGpsReminder().isAlive()) getGpsReminder().start();
		getGpsReminder().interrupt();
		getConnection().sendLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (!provider.equals("gps")) return;
		getConnection().sendGpsUnavailable();
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
				getConnection().sendGpsUnavailable();
				break;
		}
	}
	
	public void disable() {
		if (!enabled) return;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION);

		locationManager.removeUpdates(this);
		getConnection().disconnect();
		getGpsReminder().terminate();
		gpsReminder = null;
		conn = null;
		enabled = false;
	}
	
	private KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 60);
		}
		return gpsReminder;
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source != getGpsReminder()) return;
		getConnection().sendGpsUnavailable();
		getGpsReminder().terminate();
		gpsReminder = null;
	}

	public void enable() {
		enabled = true;
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
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
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public class LocalBinder extends Binder {
		public GPSListener getService() {
			return GPSListener.this;
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
}
