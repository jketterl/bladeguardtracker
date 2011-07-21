package net.djmacgyver.bgt;

import net.djmacgyver.bgt.downstream.HttpConnection;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class Map extends MapActivity implements KeepAliveTarget {
	private UserOverlay users;
	private MapView view;
	private HttpConnection updater;
	private KeepAliveThread refresher;
	
	private UserOverlay getUserOverlay()
	{
		if (users == null) {
	    	Drawable d = this.getResources().getDrawable(R.drawable.map_pin);
	    	users = new UserOverlay(d);
		}
		return users;
	}
	
	private HttpConnection getUpdater()
	{
		if (updater == null) {
			updater = new HttpConnection(getUserOverlay(), getApplicationContext());
		}
		return updater;
	}
	
	private KeepAliveThread getRefresher()
	{
		if (refresher == null) {
			refresher = new KeepAliveThread(this, 5);
		}
		return refresher;
	}
	
    public void onCreate(Bundle savedInstanceState) {
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

	@Override
	protected void onResume() {
		super.onResume();
    	getUpdater().start();
    	getRefresher().start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getUpdater().terminate();
		this.updater = null;
		getRefresher().terminate();
		this.refresher = null;
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source != getRefresher()) return;
		runOnUiThread(new Runnable() {
			public void run() {
				view.invalidate();
			}
		});
	}
}