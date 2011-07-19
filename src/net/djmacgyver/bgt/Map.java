package net.djmacgyver.bgt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity {
	private UserOverlay users;
	private Drawable drawable;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	MapView v = (MapView) findViewById(R.id.mapview);
    	v.setBuiltInZoomControls(true);
    	
    	drawable = this.getResources().getDrawable(R.drawable.icon);
    	users = new UserOverlay(drawable);
    	
    	MapClient c = new MapClient(users, getApplicationContext());
    	c.start();
    	
    	v.getOverlays().add(users);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}