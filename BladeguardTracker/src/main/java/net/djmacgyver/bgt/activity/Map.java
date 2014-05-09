package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.AbstractEventListener;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventListener;
import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.map.BladeMapFragment;
import net.djmacgyver.bgt.map.InfoFragment;
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
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Map extends ActionBarActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "Map";

	private Handler stateHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int state = (Integer) msg.obj;
			if (state == HttpSocketConnection.STATE_CONNECTED) {
				removeDialog(DIALOG_CONNECTING);
			} else {
				showDialog(DIALOG_CONNECTING);
			}
		}
	};
	private HttpSocketListener listener = new HttpSocketListener() {
		@Override
		public void receiveUpdate(JSONObject data) {
			Message msg = new Message();
			msg.obj = data;
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

    public void setMapName(String name) {
        getSupportActionBar().setTitle(name);
    }

    private EventListener nameUpdater = new AbstractEventListener(this) {
        @Override
        public void onMap(final EventMap map) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setMapName(map.getName());
                }
            });
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        if (savedInstanceState != null) {
            event = savedInstanceState.getParcelable("event");
        } else {
            event = getIntent().getExtras().getParcelable("event");
        }

        BladeMapFragment bmf = new BladeMapFragment();
        Bundle b = new Bundle();
        b.putParcelable("event", event);
        bmf.setArguments(b);

        InfoFragment infobox = new InfoFragment();
        b = new Bundle();
        b.putParcelable("event", event);
        infobox.setArguments(b);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.infobox, infobox)
                .replace(R.id.mapview, bmf)
                .commit();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("event", event);
    }

    @Override
	protected void onResume() {
		super.onResume();
        event.subscribeUpdates(nameUpdater, Event.MAP);
        showDialog(DIALOG_CONNECTING);
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
        event.unsubscribeUpdates(nameUpdater);

		if (socket != null) {
			socket.removeListener(listener);
			sockService.removeStake(this);
			unbindService(sconn);
        }

        super.onPause();
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
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) removeDialog(Map.DIALOG_CONNECTING);
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
}
