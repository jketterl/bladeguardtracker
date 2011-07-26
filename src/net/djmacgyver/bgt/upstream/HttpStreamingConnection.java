package net.djmacgyver.bgt.upstream;

import java.util.Random;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

public class HttpStreamingConnection extends Connection {
	private HttpStreamingThread thread;
	private Context context;
	
	public HttpStreamingConnection(Context context) {
		this.context = context;
	}
	
	private HttpStreamingThread getThread() {
		if (thread == null) {
			String userName = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "0");
			int userId = -1;
			try {
				userId = Integer.parseInt(userName);
			} catch (Exception e) {
				Random r = new Random();
				userId = 9000 + r.nextInt(1000);
			}
			thread = new HttpStreamingThread(context, userId);
		}
		return thread;
	}

	@Override
	public void connect() {
		getThread().start();
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

	@Override
	public void sendGpsUnavailable() {
		getThread().sendData("gpsunavailable");
		super.sendGpsUnavailable();
	}
}
