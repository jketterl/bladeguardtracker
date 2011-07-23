package net.djmacgyver.bgt;

import net.djmacgyver.bgt.upstream.Connection;
import net.djmacgyver.bgt.upstream.HttpStreamingConnection;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

public class GPSListener implements LocationListener {
	private static GPSListener sharedInstance;
	private Connection conn;
	
	public static GPSListener getSharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new GPSListener();
		}
		return sharedInstance;
	}
	
	public void setContext(Context context) {
		getConnection().setContext(context);
	}
	
	private Connection getConnection() {
		if (conn == null) {
			//conn = new HttpPollingConnection();
			conn = new HttpStreamingConnection();
			conn.connect();
		}
		return conn;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		getConnection().sendLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		System.out.println("onProviderDisabled: " + provider);
		if (!provider.equals("gps")) return;
		getConnection().sendQuit();
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
				getConnection().sendQuit();
				break;
		}
	}

	public void disable() {
		getConnection().disconnect();
		conn = null;
	}
}
