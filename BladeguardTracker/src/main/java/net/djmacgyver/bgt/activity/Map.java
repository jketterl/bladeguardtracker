package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.gps.GPSTrackingService;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import net.djmacgyver.bgt.map.BladeMapFragment;
import net.djmacgyver.bgt.map.MapHandler;
import net.djmacgyver.bgt.map.RouteOverlay;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.user.User;

import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class Map extends ActionBarActivity implements KeepAliveTarget {
    private static final String TAG = "Map";

	//private RouteOverlay route;
	//private GPSTrackingService service;
	private boolean bound = false;
	private MapHandler handler = new MapHandler(this);
	private Handler stateHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int state = (Integer) msg.obj;
			if (state == HttpSocketConnection.STATE_CONNECTED) {
				removeDialog(DIALOG_CONNECTING);
			} else {
				showDialog(DIALOG_CONNECTING);
				handler.getUserOverlay().reset();
				getLengthTextView().setText("n/a");
				getSpeedTextView().setText("n/a");
				getCycleTimeTextView().setText("n/a");
			}
		}
	};
	private HttpSocketListener listener = new HttpSocketListener() {
		@Override
		public void receiveUpdate(JSONObject data) {
			Message msg = new Message();
			msg.obj = data;
			handler.sendMessage(msg);
		}

		@Override
		public void receiveCommand(String command, JSONObject data) {
		}

		@Override
		public void receiveStateChange(int newState) {
			Message msg = new Message();
			msg.obj = newState;
			stateHandler.sendMessage(msg);
		}
	};
	
	public static final int DIALOG_CONNECTING = 1;

	private HttpSocketConnection socket;
	private SocketService sockService;
	
	// SocketService connection
	private ServiceConnection sconn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder s) {
			sockService = ((SocketService.LocalBinder) s).getService();
			socket = sockService.getSharedConnection(Map.this);
			onConnect();
		}
	};
	private Event event;

    /*
	public RouteOverlay getRoute() {
		if (route == null) {
			route = new RouteOverlay(getMap());
		}
		return route;
	}
	*/
	
	public TextView getLengthTextView() {
		return (TextView) findViewById(R.id.bladeNightLength);
	}
	
	public TextView getSpeedTextView() {
		return (TextView) findViewById(R.id.bladeNightSpeed);
	}
	
	public TextView getCycleTimeTextView() {
		return (TextView) findViewById(R.id.bladeNightCycleTime);
	}
	
	public TextView getTimeToEndView() {
		return (TextView) findViewById(R.id.timeToEnd);
	}

    public void setMapName(String name) {
        getSupportActionBar().setTitle(name);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        if (savedInstanceState != null) {
            event = savedInstanceState.getParcelable("event");
        } else {
            event = getIntent().getExtras().getParcelable("event");
        }
        Log.d(TAG, "working on event: " + event.getId());

        BladeMapFragment bmf = new BladeMapFragment(event);
        getSupportFragmentManager().beginTransaction().replace(R.id.mapview, bmf).commit();

        /*
        getMap().setBuiltInZoomControls(true);
    	
    	getMap().getOverlays().add(getRoute());
    	*/

        //bindService(new Intent(this, GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
        bound = true;
    }



	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bound) {
			//unbindService(conn);
			bound = false;
		}
	}

    /*
    todo find out where to put these
	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	
	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}
	*/

	@Override
	protected void onResume() {
        Log.d(TAG, "onResume()");
		super.onResume();
		showDialog(DIALOG_CONNECTING);
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
        /*
    	if (service != null  && service.isEnabled()) {
            getMap().setMyLocationEnabled(true);
    	}
    	*/
        Log.d(TAG, "onResume() finised");
	}

	@Override
	protected void onPause() {
		super.onPause();
        //getMap().setMyLocationEnabled(false);

		if (socket != null) {
			handler.disable();
			socket.removeListener(listener);
			sockService.removeStake(this);
			unbindService(sconn);
		}
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
        /*
    	todo see if this is still necessary
		if (source != getRefresher()) return;
		runOnUiThread(new Runnable() {
			public void run() {
				getMap().invalidate();
			}
		});
    	*/
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONNECTING:
				Dialog d = new ProgressDialog(this);
				d.setCancelable(false);
				d.setTitle(R.string.connect_progress);
				return d;
		}
		return super.onCreateDialog(id);
	}
	
	public void onConnect() {
		socket.addListener(listener);
		handler.setSocket(socket);
		handler.enable();
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) removeDialog(Map.DIALOG_CONNECTING);
	}
	
	public void onDisconnect() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		User u = Session.getUser();
		if (u == null || !u.isAdmin()) return false;
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mapselection:
				Intent i = new Intent(getApplicationContext(), MapSelection.class);
				i.putExtra("event", event);
				startActivity(i);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onBeforeConnect() {
		if (!this.isFinishing()) showDialog(Map.DIALOG_CONNECTING);
	}
	
	public Event getEvent() {
		return event;
	}
}
