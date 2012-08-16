package net.djmacgyver.bgt.map;

import java.util.Iterator;
import java.util.Vector;

import net.djmacgyver.bgt.user.Team;

import android.content.Context;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class UserOverlayItem extends OverlayItem {
	private GeoPoint point;
	private int userId;
	private Vector<UserOverlayItemListener> listeners = new Vector<UserOverlayItemListener>();
	
	public UserOverlayItem(GeoPoint point, int userId, String username, String teamName, Context context) {
		super(point, username, teamName);
		this.point = point;
		this.userId = userId;
		
		Team team = Team.getTeam(teamName, context);
		this.setMarker(team.getPin());
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