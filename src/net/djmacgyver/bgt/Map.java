package net.djmacgyver.bgt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity {
	private UserOverlay users;
	private Drawable drawable;
	private MapView view;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	view = (MapView) findViewById(R.id.mapview);
    	view.setBuiltInZoomControls(true);
    	
    	drawable = this.getResources().getDrawable(R.drawable.map_pin);
    	users = new UserOverlay(drawable);
    	
    	MapClient c = new MapClient(users, getApplicationContext());
    	c.start();
    	
    	view.getOverlays().add(users);
    	
    	(new MapUpdater(this, 10)).start();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void refresh() {
		runOnUiThread(new Runnable() {
			public void run() {
				view.invalidate();
			}
		});
	}
}