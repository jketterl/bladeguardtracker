package net.djmacgyver.bgt.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class UserOverlayItem extends OverlayItem {
	private GeoPoint point;
	
	public UserOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		this.point = point;
	}
	
	public void setPoint(GeoPoint point) {
		this.point = point;
	}
	
	public GeoPoint getPoint() {
		return point;
	}
}
