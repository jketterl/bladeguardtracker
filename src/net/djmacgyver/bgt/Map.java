package net.djmacgyver.bgt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity {
	private UserOverlay users;
	private MapView view;
	private MapClient updater;
	
	private UserOverlay getUserOverlay()
	{
		if (users == null) {
	    	Drawable d = this.getResources().getDrawable(R.drawable.map_pin);
	    	users = new UserOverlay(d);
		}
		return users;
	}
	
	private MapClient getUpdater()
	{
		if (updater == null) {
			updater = new MapClient(getUserOverlay(), getApplicationContext());
		}
		return updater;
	}
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	getUpdater().start();
    	
    	view = (MapView) findViewById(R.id.mapview);
    	view.setBuiltInZoomControls(true);
    	
    	view.getOverlays().add(getUserOverlay());
    	
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