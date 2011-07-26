package net.djmacgyver.bgt.upstream;

import android.content.Context;
import android.content.SharedPreferences;
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
			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
			if (p.getBoolean("anonymous", true)) {
				thread = new HttpStreamingThread(context);
			} else {
				String userName = p.getString("username", "");
				String password = p.getString("password", "");
				thread = new HttpStreamingThread(context, userName, password);
			}
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
