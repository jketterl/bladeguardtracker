package net.djmacgyver.bgt.gps;

public interface GPSTrackingListener {
	public void trackingEnabled();
	public void trackingDisabled();
	public void onPositionLock(int position);
	public void onPositionLost();
}
