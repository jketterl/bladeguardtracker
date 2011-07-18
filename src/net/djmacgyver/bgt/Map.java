package net.djmacgyver.bgt;

import com.google.android.maps.MapActivity;

import android.os.Bundle;

public class Map extends MapActivity {
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}