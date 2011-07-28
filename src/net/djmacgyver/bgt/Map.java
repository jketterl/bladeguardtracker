package net.djmacgyver.bgt;

import net.djmacgyver.bgt.downstream.HttpConnection;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
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
	private GPSListener service;
	private boolean bound = false;
    ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
			if (!hasLocationOverlay()) return;
	    	view = (MapView) findViewById(R.id.mapview);
	    	view.getOverlays().remove(getLocationOverlay());
	    	getLocationOverlay().disableMyLocation();
	    	setLocationOverlay(null);
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((GPSListener.LocalBinder) binder).getService();
			if (!service.isEnabled()) return;
	    	view = (MapView) findViewById(R.id.mapview);
			setLocationOverlay(new MyLocationOverlay(getApplicationContext(), view));
			getLocationOverlay().enableMyLocation();
	    	view.getOverlays().add(getLocationOverlay());
		}
	};
	
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
	
	private MyLocationOverlay getLocationOverlay() {
		return myLoc;
	}
	
	private void setLocationOverlay(MyLocationOverlay myLoc) {
		this.myLoc = myLoc;
	}
	
	private boolean hasLocationOverlay() {
		return myLoc != null;
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

        bindService(new Intent(this, GPSListener.class), conn, Context.BIND_AUTO_CREATE);
        bound = true;
    	
    	new MapDownloaderThread(getApplicationContext(), getRoute(), view).start();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bound) {
			unbindService(conn);
			bound = false;
		}
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
    	if (service != null && hasLocationOverlay() && service.isEnabled()) {
    		getLocationOverlay().enableMyLocation();
    	}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (hasLocationOverlay()) getLocationOverlay().disableMyLocation();
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