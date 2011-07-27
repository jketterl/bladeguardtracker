package net.djmacgyver.bgt;

import net.djmacgyver.bgt.downstream.HttpConnection;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class Map extends MapActivity implements KeepAliveTarget {
	private UserOverlay users;
	private MyLocationOverlay myLoc;
	private MapView view;
	private HttpConnection updater;
	private KeepAliveThread refresher;
	private RouteOverlay route;
	
	private RouteOverlay getRoute() {
		if (route == null) {
			route = new RouteOverlay();
			route.getPaint().setAntiAlias(true);
			route.getPaint().setColor(Color.BLUE);
			route.getPaint().setAlpha(64);
			route.getPaint().setStrokeWidth(2);
		}
		return route;
	}
	
	private UserOverlay getUserOverlay()
	{
		if (users == null) {
	    	Drawable d = this.getResources().getDrawable(R.drawable.map_pin);
	    	users = new UserOverlay(d);
		}
		return users;
	}
	
	private MyLocationOverlay getMyLocationOverlay() {
		if (myLoc == null) {
			myLoc = new MyLocationOverlay(getApplicationContext(), view);			
		}
		return myLoc;
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
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.map);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.map_name);
    	
    	view = (MapView) findViewById(R.id.mapview);
    	view.setBuiltInZoomControls(true);
    	
    	view.getOverlays().add(getRoute());
    	view.getOverlays().add(getUserOverlay());
    	view.getOverlays().add(getMyLocationOverlay());

    	new MapDownloaderThread(getApplicationContext(), getRoute(), view).start();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	
	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
    	getUpdater().start();
    	getRefresher().start();
    	if (true/*GPSListener.isActive()*/) {
	    	getMyLocationOverlay().enableMyLocation();
	    	getMyLocationOverlay().enableCompass();
    	}
	}

	@Override
	protected void onPause() {
		super.onPause();
		getMyLocationOverlay().disableCompass();
		getMyLocationOverlay().disableMyLocation();
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