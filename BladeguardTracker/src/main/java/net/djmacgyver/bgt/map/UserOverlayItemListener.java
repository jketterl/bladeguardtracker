package net.djmacgyver.bgt.map;

import com.google.android.maps.GeoPoint;

public interface UserOverlayItemListener {
	public void pointUpdated(GeoPoint newPoint);
}
