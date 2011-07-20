package net.djmacgyver.bgt.upstream;

import java.io.IOException;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.location.Location;

import net.djmacgyver.bgt.Config;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;

public class HttpConnection implements KeepAliveTarget {
	private KeepAliveThread gpsReminder;
	private KeepAliveThread timeoutReminder;
	private int userId;
	private Context context;
	private HttpClient client;
	private boolean updateBlocked = false;
	private Location myLocation;
	
	public HttpConnection() {
		Random r = new Random();
		this.userId = r.nextInt(100);
	}
	
	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source == getGpsReminder()) {
			updateBlocked = false;
			sendLocation();
		} else if (source == getTimeoutReminder()) {
			sendKeepAlive();
		}
	}

	private KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 10);
		}
		return gpsReminder;
	}

	private KeepAliveThread getTimeoutReminder() {
		if (timeoutReminder == null) {
			timeoutReminder = new KeepAliveThread(this, 50);
		}
		return timeoutReminder;
	}
	
	public void connect() {
		getGpsReminder().start();
		getTimeoutReminder().start();
	}
	
	public void disconnect() {
		getGpsReminder().terminate();
		getTimeoutReminder().terminate();
		sendQuit();
	}
	
	private HttpClient getClient() {
		if (client == null) {
			client = new net.djmacgyver.bgt.http.HttpClient(context);
		}
		return client;
	}
	
	private void sendRequest(HttpUriRequest req) {
		try {
			getClient().execute(req).getEntity().consumeContent();
			getTimeoutReminder().interrupt();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendLocation() {
		if (myLocation == null) return;
		Location l = myLocation;
		myLocation = null;
		sendLocation(l);
	}
	
	public void sendLocation(Location location) {
		if (updateBlocked) {
			myLocation = location;
			return;
		}
		HttpGet req = new HttpGet(Config.baseUrl + "log?uid=" + this.userId + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&speed=" + location.getSpeed());
		sendRequest(req);
		getGpsReminder().interrupt();
		updateBlocked = true;
	}
	
	public void sendQuit() {
		HttpGet req = new HttpGet(Config.baseUrl + "quit?uid=" + this.userId);
		sendRequest(req);
	}
	
	public void sendKeepAlive() {
		HttpGet req = new HttpGet(Config.baseUrl + "keepalive?uid=" + this.userId);
		sendRequest(req);
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
