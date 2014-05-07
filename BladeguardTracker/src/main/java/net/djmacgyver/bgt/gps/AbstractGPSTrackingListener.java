package net.djmacgyver.bgt.gps;

abstract public class AbstractGPSTrackingListener implements GPSTrackingListener {
    @Override
    public void trackingEnabled() {

    }

    @Override
    public void trackingDisabled() {

    }

    @Override
    public void onPositionLock(int position) {

    }

    @Override
    public void onPositionLost() {

    }

    @Override
    public void onDistanceToEnd(double distance) {

    }

    @Override
    public void onDistanceToFront(double distance) {

    }

    @Override
    public void onDistanceToEndLost() {

    }

    @Override
    public void onDistanceToFrontLost() {

    }
}
