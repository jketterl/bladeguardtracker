package net.djmacgyver.bgt.map;

import java.util.Iterator;
import java.util.Vector;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class UserOverlayItem extends OverlayItem {
	private GeoPoint point;
	private int userId;
	private Vector<UserOverlayItemListener> listeners = new Vector<UserOverlayItemListener>();
	
	public UserOverlayItem(GeoPoint point, int userId, String username, String team) {
		super(point, username, team);
		this.point = point;
		this.userId = userId;
	}
	
	public void setPoint(GeoPoint point) {
		this.point = point;
		this.firePointUpdated(point);
	}
	
	public GeoPoint getPoint() {
		return point;
	}
	
	public int getUserId() {
		return userId;
	}
	
	private void firePointUpdated(GeoPoint newPoint) {
		Iterator<UserOverlayItemListener> i = listeners.iterator();
		while (i.hasNext()) i.next().pointUpdated(newPoint);
	}
	
	public void addListener(UserOverlayItemListener l) {
		listeners.add(l);
	}
	
	public void removeListener(UserOverlayItemListener l) {
		listeners.remove(l);
	}
}