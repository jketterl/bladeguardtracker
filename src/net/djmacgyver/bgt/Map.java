package net.djmacgyver.bgt;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.os.Bundle;

public class Map extends MapActivity {
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	MapView v = (MapView) findViewById(R.id.mapview);
    	v.setBuiltInZoomControls(true);
    	
    	MapClient c = new MapClient(this, getApplicationContext());
    	c.start();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}