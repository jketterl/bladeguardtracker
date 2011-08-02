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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Window;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class Map extends MapActivity implements KeepAliveTarget {
	private UserOverlay users;
	private MyLocationOverlay myLoc;
	private HttpConnection updater;
	private KeepAliveThread refresher;
	private RouteOverlay route;
	private GPSListener service;
	private boolean bound = false;
	private MapView map;
	
    ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
			if (!hasLocationOverlay()) return;
	    	getMap().getOverlays().remove(getLocationOverlay());
	    	getLocationOverlay().disableMyLocation();
	    	setLocationOverlay(null);
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((GPSListener.LocalBinder) binder).getService();
			if (!service.isEnabled()) return;
			setLocationOverlay(new MyLocationOverlay(getApplicationContext(), getMap()));
			getLocationOverlay().enableMyLocation();
	    	getMap().getOverlays().add(getLocationOverlay());
		}
	};
	
	public MapView getMap() {
		if (map == null) {
			map = (MapView) findViewById(R.id.mapview);
		}
		return map;
	}
	
	private RouteOverlay getRoute() {
		if (route == null) {
			route = new RouteOverlay(getMap());
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
	    	users = new UserOverlay(d, this);
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
			final TextView length = (TextView) findViewById(R.id.bladeNightLength);
			Handler h = new Handler(){
				public void handleMessage(Message msg) {
					length.setText((String) msg.obj);
				}
			};
			updater = new HttpConnection(getUserOverlay(), getRoute(), getApplicationContext(), h);
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
    	
    	getMap().setBuiltInZoomControls(true);
    	
    	getMap().getOverlays().add(getRoute());
    	getMap().getOverlays().add(getUserOverlay());

        bindService(new Intent(this, GPSListener.class), conn, Context.BIND_AUTO_CREATE);
        bound = true;
    	
    	//new MapDownloaderThread(getApplicationContext(), getRoute(), view).start();
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
				getMap().invalidate();
			}
		});
	}
}