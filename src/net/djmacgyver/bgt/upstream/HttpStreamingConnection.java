package net.djmacgyver.bgt.upstream;

import java.util.Random;

import android.content.Context;
import android.location.Location;

public class HttpStreamingConnection extends Connection {
	private HttpStreamingThread thread;
	private int userId;
	private Context context;
	
	public HttpStreamingConnection(Context context) {
		Random r = new Random();
		this.userId = r.nextInt(100);
		this.context = context;
	}
	
	private HttpStreamingThread getThread() {
		if (thread == null) {
			thread = new HttpStreamingThread(context);
		}
		return thread;
	}

	@Override
	public void connect() {
		getThread().start();
		getThread().sendData("uid=" + userId);
		getGpsReminder().start();
	}

	@Override
	public void disconnect() {
		getGpsReminder().terminate();
		getThread().terminate();
	}

	@Override
	protected void executeLocationSend(Location location) {
		String data = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		if (location.hasSpeed()) data += "&speed=" + location.getSpeed();
		getThread().sendData(data);
	}

	@Override
	public void sendQuit() {
		getThread().sendData("quit");
	}
}
