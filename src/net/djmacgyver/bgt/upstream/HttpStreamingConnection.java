package net.djmacgyver.bgt.upstream;

import java.util.Random;

import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.content.Context;
import android.location.Location;

public class HttpStreamingConnection extends Connection implements KeepAliveTarget {
	private HttpStreamingThread thread;
	private KeepAliveThread gpsReminder;
	private boolean updateBlocked = false;
	private Location queuedLocation;
	private Location lastLocation;
	private int userId;
	
	public HttpStreamingConnection() {
		Random r = new Random();
		this.userId = r.nextInt(100);
	}
	
	private HttpStreamingThread getThread() {
		if (thread == null) {
			thread = new HttpStreamingThread();
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
	public void sendLocation(Location location) {
		if (location.equals(lastLocation)) return;
		if (lastLocation != null && location.distanceTo(lastLocation) == 0) return;
		if (updateBlocked) {
			queuedLocation = location;
			return;
		}

		String data = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		if (location.hasSpeed()) data += "&speed=" + location.getSpeed();
		getThread().sendData(data);
		
		getGpsReminder().interrupt();
		lastLocation = location;
		updateBlocked = true;
	}

	@Override
	public void setContext(Context context) {
		getThread().setContext(context);
	}

	@Override
	public void sendQuit() {
		getThread().sendData("quit");
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source == getGpsReminder()) {
			updateBlocked = false;
			sendLocation();
		}
	}

	private void sendLocation() {
		if (queuedLocation == null) return;
		Location l = queuedLocation;
		queuedLocation = null;
		sendLocation(l);
	}

	private KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 5);
		}
		return gpsReminder;
	}
}
