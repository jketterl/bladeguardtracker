package net.djmacgyver.bgt;

import java.io.IOException;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

public class GPSListener implements LocationListener {
	final static String baseUrl = "https://djmacgyver.homelinux.org/bgt/";
	
	private static GPSListener sharedInstance;
	private int userId;
	
	public static GPSListener getSharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new GPSListener();
		}
		return sharedInstance;
	}
	
	public GPSListener() {
		Random r = new Random();
		this.userId = r.nextInt(100);
	}
	
	private Context context;
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	private HttpClient client;
	
	private HttpClient getClient() {
		if (client == null) {
			client = new net.djmacgyver.bgt.http.HttpClient(context);
		}
		return client;
	}
	
	private void sendRequest(HttpUriRequest req) {
		try {
			getClient().execute(req).getEntity().consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendLocation(Location location) {
		HttpGet req = new HttpGet(GPSListener.baseUrl + "log?uid=" + this.userId + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
		sendRequest(req);
	}
	
	private void sendQuit() {
		HttpGet req = new HttpGet(GPSListener.baseUrl + "quit?uid=" + this.userId);
		sendRequest(req);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		sendLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		System.out.println("onProviderDisabled: " + provider);
		if (!provider.equals("gps")) return;
		sendQuit();
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
			case LocationProvider.OUT_OF_SERVICE:
				this.sendQuit();
				break;
		}
	}

	public void disable() {
		sendQuit();
	}
}
