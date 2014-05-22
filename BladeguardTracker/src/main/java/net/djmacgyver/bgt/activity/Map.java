package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.dialog.ProgressDialog;
import net.djmacgyver.bgt.event.AbstractEventListener;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventListener;
import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.map.BladeMapFragment;
import net.djmacgyver.bgt.map.InfoFragment;
import net.djmacgyver.bgt.map.RouteSelectionDialog;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.AbstractHttpSocketListener;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.UpdateEventCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Map extends ActionBarActivity implements RouteSelectionDialog.OnRouteSelectedListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Map";

    private static final String DIALOG_CONNECTING = "dialog_connecting";
    private static final String DIALOG_ROUTE_SELECTION = "dialog_routeselection";
    private static final String DIALOG_CONFIRM = "dialog_confirm";
    private static final String DIALOG_SWITCHING = "dialog_switching";

	private HttpSocketListener listener = new AbstractHttpSocketListener() {
		@Override
		public void receiveStateChange(final int newState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newState == HttpSocketConnection.STATE_CONNECTED) {
                        dismissConnectingDialog();
                    } else {
                        showConnectingDialog();
                    }
                }
            });
		}
	};
	
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
        showConnectingDialog();
        bindService(new Intent(this, SocketService.class), sconn, Context.BIND_AUTO_CREATE);
	}

    private void showConnectingDialog() {
        dismissConnectingDialog();
        DialogFragment connecting = new ProgressDialog(R.string.connect_progress);
        connecting.show(getSupportFragmentManager(), DIALOG_CONNECTING);
    }

    private void dismissConnectingDialog() {
        DialogFragment connecting = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_CONNECTING);
        if (connecting != null) connecting.dismiss();
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

	public void onConnect() {
		socket.addListener(listener);
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) dismissConnectingDialog();
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
                DialogFragment f = new RouteSelectionDialog();
                f.show(getSupportFragmentManager(), DIALOG_ROUTE_SELECTION);
		}
		return super.onOptionsItemSelected(item);
	}

    private abstract class ConfirmDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder b = new AlertDialog.Builder(Map.this);
            b.setTitle(R.string.are_you_sure)
                    .setMessage(R.string.map_will_be_reset)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            perform();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
            return b.create();
        }

        protected abstract void perform();
    }

    @Override
    public void onRouteSelected(final int id) {
        DialogFragment confirm = new ConfirmDialog() {
            @Override
            protected void perform() {
                final DialogFragment switching = new ProgressDialog(R.string.switching_map);
                switching.show(getSupportFragmentManager(), DIALOG_SWITCHING);

                try {
                    JSONObject data = new JSONObject();
                    data.put("map", id);
                    SocketCommand command = new UpdateEventCommand(event, data);

                    command.addCallback(new SocketCommandCallback() {
                        @Override
                        public void run(SocketCommand command) {
                            switching.dismiss();
                        }
                    });

                    socket.sendCommand(command);
                } catch (JSONException ignored) {}
            }
        };
        confirm.show(getSupportFragmentManager(), DIALOG_CONFIRM);
    }
}
