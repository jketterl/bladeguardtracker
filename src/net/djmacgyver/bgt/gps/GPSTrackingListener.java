package net.djmacgyver.bgt.gps;

public interface GPSTrackingListener {
	public void trackingEnabled();
	public void trackingDisabled();
	public void onPositionLock(int position);
	public void onPositionLost();
	public void onDistanceToEnd(double distance);
	public void onDistanceToFront(double distance);
	public void onDistanceToEndLost();
	public void onDistanceToFrontLost();
}
