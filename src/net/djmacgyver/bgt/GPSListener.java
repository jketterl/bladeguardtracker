package net.djmacgyver.bgt;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSListener implements LocationListener {
	final static String baseUrl = "http://jketterl-nb.tech/bgt/";
	
	private static GPSListener sharedInstance;
	
	public static GPSListener getSharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new GPSListener();
		}
		return sharedInstance;
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
	
	@Override
	public void onLocationChanged(Location location) {
		HttpGet req = new HttpGet(GPSListener.baseUrl + "log.php?lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
		try {
			HttpResponse res = getClient().execute(req);
			res.getEntity().consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		System.out.println("onProviderDisabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		System.out.println("onProviderEnabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		System.out.println("onStatusChanged: " + provider + "; " + status);
	}

}
