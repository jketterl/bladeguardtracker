package net.djmacgyver.bgl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSListener implements LocationListener {
	private static GPSListener sharedInstance;
	
	public static GPSListener getSharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new GPSListener();
		}
		return sharedInstance;
	}
	
	private HttpClient client;
	
	private HttpClient getClient() {
		if (client == null) {
			client = new DefaultHttpClient();
		}
		return client;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		HttpGet req = new HttpGet("http://jketterl-nb.tech/log.php?lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
		try {
			getClient().execute(req);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
