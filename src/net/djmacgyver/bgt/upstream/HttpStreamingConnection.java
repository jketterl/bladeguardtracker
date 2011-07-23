package net.djmacgyver.bgt.upstream;

import android.content.Context;
import android.location.Location;

public class HttpStreamingConnection implements Connection {
	private HttpStreamingThread thread;
	
	private HttpStreamingThread getThread() {
		if (thread == null) {
			thread = new HttpStreamingThread();
			thread.start();
		}
		return thread;
	}

	@Override
	public void connect() {
	}

	@Override
	public void disconnect() {
		getThread().terminate();
	}

	@Override
	public void sendLocation(Location location) {
		String data = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		if (location.hasSpeed()) data += "&speed=" + location.getSpeed();
		getThread().sendData(data.getBytes());
	}

	@Override
	public void setContext(Context context) {
		getThread().setContext(context);
	}

	@Override
	public void sendQuit() {
		getThread().sendData("quit".getBytes());
	}
}
