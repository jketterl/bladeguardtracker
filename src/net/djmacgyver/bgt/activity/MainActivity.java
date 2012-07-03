package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.GCMIntentService;
import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventList;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	public static final int DIALOG_CONNECTING = 1;
	private HttpSocketConnection socket;
	private SocketService sockService;
	private EventList events;

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
			// NOOP
		}

		@Override
		public void receiveCommand(String command, JSONObject data) {
			// NOOP
		}

		@Override
		public void receiveStateChange(int newState) {
			Message msg = new Message();
			msg.obj = newState;
			stateHandler.sendMessage(msg);
		}
	};
	private ServiceConnection sconn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			sockService = ((SocketService.LocalBinder) service).getService();
			socket = sockService.getSharedConnection(MainActivity.this);
			onConnect();
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.app_name);
        
        events = new EventList(this);
        ListView eventList = (ListView) findViewById(R.id.upcomingEvents);
        eventList.setAdapter(events);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// get the event the user clicked on
				Event event = new Event((JSONObject) events.getItem(position));
				
				Intent i = new Intent(MainActivity.this, EventDetail.class);
				i.putExtra("event", event);
				startActivity(i);
			}
		});
        
        GCMIntentService.gcmId = getResources().getString(R.string.gcm_id);
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, GCMIntentService.gcmId);
		} else {
			ServiceConnection conn = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {
				}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					SocketService s = ((SocketService.LocalBinder) service).getService();
					JSONObject data = new JSONObject();
					try {
						data.put("regId", regId);
					} catch (JSONException e) {}
					SocketCommand c = new SocketCommand("updateRegistration", data);
					s.getSharedConnection().sendCommand(c);
					unbindService(this);
				}
			};
			bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
			Log.v("GCM registration", "Already registered (id=\"" + regId + "\"");
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
			case R.id.config:
				i = new Intent(getApplicationContext(), Settings.class);
				startActivity(i);
				return true;
			case R.id.admin:
				i = new Intent(getApplicationContext(), Admin.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONNECTING:
				ProgressDialog d = new ProgressDialog(this);
				d.setTitle(R.string.connect_progress);
				return d;
		}
		return super.onCreateDialog(id);
	}
	
	private void onConnect() {
		socket.addListener(listener);
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) removeDialog(Map.DIALOG_CONNECTING);
	}

	@Override
	protected void onPause() {
		super.onPause();
		socket.removeListener(listener);
		sockService.removeStake(this);
		unbindService(sconn);
	}

	@Override
	protected void onResume() {
        events.refresh();
		showDialog(DIALOG_CONNECTING);
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupVisible(R.id.adminGroup, Session.hasUser() && Session.getUser().isAdmin());
		return super.onPrepareOptionsMenu(menu);
	}
}