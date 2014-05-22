package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.dialog.ProgressDialog;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventList;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.AbstractHttpSocketListener;
import net.djmacgyver.bgt.socket.CommandExecutor;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.RegistrationUpdateCommand;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    public static final String DIALOG_CONNECTING = "dialog_connecting";
	private HttpSocketConnection socket;
	private SocketService sockService;
	private EventList events;

	private Handler stateHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int state = (Integer) msg.obj;
			if (state == HttpSocketConnection.STATE_CONNECTED) {
                dismissConnectDialog();
			} else {
                showConnectDialog();
			}
		}
	};
	private HttpSocketListener listener = new AbstractHttpSocketListener() {
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
        setContentView(R.layout.main);

        events = new EventList(this);
        ListView eventList = (ListView) findViewById(R.id.upcomingEvents);
        eventList.setAdapter(events);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
                try {
                    // get the event the user clicked on
                    Event event = new Event((JSONObject) events.getItem(position));

                    Intent i = new Intent(MainActivity.this, EventDetail.class);
                    i.putExtra("event", event);
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
			}
		});

        new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                try {
                    return gcm.register(getResources().getString(R.string.gcm_id));
                } catch (IOException e1) {
                    Log.e(TAG, "gcm registration failed", e1);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String regId) {
                Log.d("MainActivity", "got registration id: " + regId);
                if (regId == null) return;
                SocketCommand c = new RegistrationUpdateCommand(regId);
                new CommandExecutor(MainActivity.this).execute(c);
            }
        }.execute();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
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

	private void onConnect() {
		socket.addListener(listener);
        socket.addAuthCallback(new SocketCommandCallback() {
            @Override
            public void run(SocketCommand command) {
                supportInvalidateOptionsMenu();
            }
        });
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) dismissConnectDialog();
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
        showConnectDialog();
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupVisible(R.id.adminGroup, Session.hasUser() && Session.getUser().isAdmin());
		return super.onPrepareOptionsMenu(menu);
	}

    private void showConnectDialog() {
        dismissConnectDialog();
        ProgressDialog d = new ProgressDialog();
        d.show(getSupportFragmentManager(), DIALOG_CONNECTING);
    }

    private void dismissConnectDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialog d = ((ProgressDialog) fm.findFragmentByTag(DIALOG_CONNECTING));
        if (d != null) d.dismiss();
    }
}