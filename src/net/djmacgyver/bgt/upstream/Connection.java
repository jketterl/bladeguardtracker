package net.djmacgyver.bgt.upstream;

import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.location.Location;

public abstract class Connection implements KeepAliveTarget {

	private boolean updateBlocked = false;
	private Location queuedLocation;
	private Location lastLocation;
	protected KeepAliveThread gpsReminder;

	public abstract void connect();

	public abstract void disconnect();

	protected abstract void executeLocationSend(Location location);
	
	public abstract void sendQuit();

	public void sendLocation(Location location) {
		if (location.equals(lastLocation)) return;
		if (lastLocation != null && location.distanceTo(lastLocation) == 0) return;
		if (updateBlocked) {
			queuedLocation = location;
			return;
		}
	
		executeLocationSend(location);
		
		getGpsReminder().interrupt();
		lastLocation = location;
		updateBlocked = true;
	}

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

	protected KeepAliveThread getGpsReminder() {
		if (gpsReminder == null) {
			gpsReminder = new KeepAliveThread(this, 5);
		}
		return gpsReminder;
	}

	public void sendGpsUnavailable() {
		lastLocation = null;
	}
}