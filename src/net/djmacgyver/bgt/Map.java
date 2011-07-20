package net.djmacgyver.bgt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity {
	private UserOverlay users;
	private MapView view;
	private MapClient updater;
	private MapRefresher refresher;
	
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
	
	private MapRefresher getRefresher()
	{
		if (refresher == null) {
			refresher = new MapRefresher(this, 10);
		}
		return refresher;
	}
	
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("onCreate()");
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map);
    	
    	view = (MapView) findViewById(R.id.mapview);
    	view.setBuiltInZoomControls(true);
    	
    	view.getOverlays().add(getUserOverlay());
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

	@Override
	protected void onStart() {
		super.onStart();
    	getUpdater().start();
    	getRefresher().start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		getUpdater().terminate();
		this.updater = null;
		getRefresher().terminate();
		this.refresher = null;
	}
}