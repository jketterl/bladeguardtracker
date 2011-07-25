package net.djmacgyver.bgt;

import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class UserOverlay extends ItemizedOverlay<OverlayItem> {
	
	private HashMap<Integer, OverlayItem> mOverlays = new HashMap<Integer, OverlayItem>();

	public UserOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
		populate();
	}
	
	public synchronized void updateUser(int userId, GeoPoint point) {
		OverlayItem i = mOverlays.get(userId);
		if (i != null) mOverlays.remove(i);
		mOverlays.put(userId, new OverlayItem(point, "", ""));
		setLastFocusedIndex(-1);
		populate();
	}
	
	public synchronized void removeUser(int userId) {
		mOverlays.remove(userId);
		setLastFocusedIndex(-1);
		populate();
	}
	
	public synchronized void reset()
	{
		mOverlays.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return (OverlayItem) mOverlays.values().toArray()[i];
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
	}
}
