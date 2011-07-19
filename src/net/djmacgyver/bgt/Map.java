package net.djmacgyver.bgt;

import java.util.List;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class Map extends MapActivity {
	private List<Overlay> mapOverlays;
	private UserOverlay users;
	private Drawable drawable;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	MapView v = (MapView) findViewById(R.id.mapview);
    	v.setBuiltInZoomControls(true);
    	
    	mapOverlays = v.getOverlays();
    	drawable = this.getResources().getDrawable(R.drawable.icon);
    	users = new UserOverlay(drawable);
    	
    	MapClient c = new MapClient(users, getApplicationContext());
    	c.start();
    	
    	mapOverlays.add(users);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}