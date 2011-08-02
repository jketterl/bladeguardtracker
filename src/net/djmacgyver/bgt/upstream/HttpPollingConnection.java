package net.djmacgyver.bgt.upstream;

import java.io.IOException;
import java.util.Random;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.location.Location;

public class HttpPollingConnection extends Connection {
	private KeepAliveThread timeoutReminder;
	private int userId;
	private Context context;
	private HttpClient client;
	public HttpPollingConnection(Context context) {
		Random r = new Random();
		this.userId = r.nextInt(100);
		this.context = context;
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source == getTimeoutReminder()) {
			sendKeepAlive();
		}
		super.keepAlive(source);
	}

	private KeepAliveThread getTimeoutReminder() {
		if (timeoutReminder == null) {
			timeoutReminder = new KeepAliveThread(this, 50);
		}
		return timeoutReminder;
	}
	
	/* (non-Javadoc)
	 * @see net.djmacgyver.bgt.upstream.Connection#connect()
	 */
	@Override
	public void connect() {
		getGpsReminder().start();
		getTimeoutReminder().start();
	}
	
	/* (non-Javadoc)
	 * @see net.djmacgyver.bgt.upstream.Connection#disconnect()
	 */
	@Override
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
	
	protected void executeLocationSend(Location location) {
		String url = context.getResources().getString(R.string.base_url) + "log?uid=" + this.userId + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		if (location.hasSpeed()) url += "&speed=" + location.getSpeed();
		HttpGet req = new HttpGet(url);
		sendRequest(req);
	}
	
	public void sendQuit() {
		HttpGet req = new HttpGet(context.getResources().getString(R.string.base_url) + "quit?uid=" + this.userId);
		sendRequest(req);
	}
	
	public void sendKeepAlive() {
		HttpGet req = new HttpGet(context.getResources().getString(R.string.base_url) + "keepalive?uid=" + this.userId);
		sendRequest(req);
	}

	@Override
	protected KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 10);
		}
		return gpsReminder;
	}

	@Override
	public void sendGpsUnavailable() {
		sendQuit();
		super.sendGpsUnavailable();
	}
}
