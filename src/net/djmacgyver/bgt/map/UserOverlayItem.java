package net.djmacgyver.bgt.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class UserOverlayItem extends OverlayItem {
	private GeoPoint point;
	private int userId;
	
	public UserOverlayItem(GeoPoint point, int userId, String username, String team) {
		super(point, username, team);
		this.point = point;
		this.userId = userId;
	}
	
	public void setPoint(GeoPoint point) {
		this.point = point;
	}
	
	public GeoPoint getPoint() {
		return point;
	}
	
	public int getUserId() {
		return userId;
	}
}
