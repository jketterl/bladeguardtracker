package net.djmacgyver.bgt;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
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
			HttpParams params = new BasicHttpParams();
			try {
				HttpProtocolParams.setUserAgent(
						params,
						context.getString(R.string.app_name) +
						"/" +
						context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName +
						"(Android/" +
						android.os.Build.DEVICE + " " + android.os.Build.VERSION.RELEASE + 
						")"
				);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SchemeRegistry schreg = new SchemeRegistry();
			schreg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			
			final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
	        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        schreg.register(new Scheme("https", sslSocketFactory, 443));

	        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schreg), params);
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
