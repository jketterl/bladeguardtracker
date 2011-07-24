package net.djmacgyver.bgt;

import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import net.djmacgyver.bgt.upstream.Connection;
import net.djmacgyver.bgt.upstream.HttpStreamingConnection;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

public class GPSListener implements LocationListener, KeepAliveTarget {
	private static GPSListener sharedInstance;
	private Connection conn;
	private Context context;
	private KeepAliveThread gpsReminder;
	
	public GPSListener(Context context) {
		this.context = context;
	}

	public static GPSListener getSharedInstance(Context context) {
		if (sharedInstance == null) {
			sharedInstance = new GPSListener(context);
		}
		return sharedInstance;
	}
	
	private Connection getConnection() {
		if (conn == null) {
			//conn = new HttpPollingConnection(context);
			conn = new HttpStreamingConnection(context);
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
		System.out.println("onProviderDisabled: " + provider);
		if (!provider.equals("gps")) return;
		getConnection().sendGpsUnavailable();
	}

	@Override
	public void onProviderEnabled(String provider) {
		System.out.println("onProviderEnabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		System.out.println("onStatusChanged: " + provider + "; " + status);
		if (!provider.equals("gps")) return;
		switch (status) {
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
			case LocationProvider.OUT_OF_SERVICE:
				getConnection().sendGpsUnavailable();
				break;
		}
	}
	
	public void disable() {
		getConnection().disconnect();
		getGpsReminder().terminate();
		gpsReminder = null;
		conn = null;
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
}
