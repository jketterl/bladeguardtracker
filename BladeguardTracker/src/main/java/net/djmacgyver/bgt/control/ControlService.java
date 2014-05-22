package net.djmacgyver.bgt.control;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventList;
import net.djmacgyver.bgt.event.EventNotFoundException;
import net.djmacgyver.bgt.event.ParticipationStore;
import net.djmacgyver.bgt.gps.GPSTrackingService;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.DisableControlCommand;
import net.djmacgyver.bgt.socket.command.EnableControlCommand;

import org.json.JSONObject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ControlService extends Service implements HttpSocketListener {
    private static final String TAG = "ControlService";
    private static final String PREFS_EVENT_ID = "eventId";

	private HttpSocketConnection socket;
	private Event event;
	
	public class LocalBinder extends Binder {
		public ControlService getService() {
			return ControlService.this;
		}
	}
	
	private final Binder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private class SocketServiceConnection implements ServiceConnection{
		public SocketService s;
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			s = ((SocketService.LocalBinder) service).getService();
			setConnection(s.getSharedConnection(ControlService.this));
		}
	}
	
	private SocketServiceConnection conn = new SocketServiceConnection();

	@Override
	public void onCreate() {
		System.out.println("ControlService created.");
	}

	@Override
	public void onDestroy() {
		shutdown();
		System.out.println("ControlService destroyed");
	}
	
	private void setConnection(HttpSocketConnection socket) {
		this.socket = socket;
		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) enableControlSession();
		socket.addListener(this);
	}
	
	private void enableControlSession()
	{
		SocketCommand enable = new EnableControlCommand(event);
		enable.addCallback(new SocketCommandCallback() {
			@Override
			public void run(SocketCommand enable) {
				if (enable.wasSuccessful()) return;
				Log.e("ControlService", "Server did not accept control connection; error: " + enable.getResponseData());
				stopSelf();
			}
		});
		socket.sendCommand(enable);
	}
	
	public void shutdown() {
        if (socket != null) {
            SocketCommand c = new DisableControlCommand(event);
            socket.sendCommand(c);
            socket.removeListener(this);
        }
        stopTracking();
        if (conn.s != null) {
            conn.s.removeStake(this);
            unbindService(conn);
        }
    }
	
	private boolean trackingEnabled = false;
	
	private void startTracking() {
		if (trackingEnabled) return;
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
			}
			
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				GPSTrackingService l = ((GPSTrackingService.LocalBinder) arg1).getService();
				l.enable(event);
				unbindService(this);
			}
		};
		
		startService(new Intent(getApplicationContext(), GPSTrackingService.class));
		bindService(new Intent(getApplicationContext(), GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
		trackingEnabled = true;
	}
	
	private void stopTracking() {
		if (!trackingEnabled) return;
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				GPSTrackingService l = ((GPSTrackingService.LocalBinder) service).getService();
				l.disable();
				unbindService(this);
				stopService(new Intent(getApplicationContext(), GPSTrackingService.class));
			}
		};
		bindService(new Intent(getApplicationContext(), GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
		trackingEnabled = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.w(TAG, "restarting without intent; loading previous event");
            SharedPreferences prefs = getSharedPreferences("ControlService", MODE_PRIVATE);
            startOnEvent(prefs.getInt(PREFS_EVENT_ID, -1));
        } else {
            if (intent.hasExtra("event")) {
                startOnEvent(intent.<Event>getParcelableExtra("event"));
            } else {
                startOnEvent(intent.getIntExtra("eventId", -1));
            }
        }

        return START_STICKY;
	}

    private void startOnEvent(final int eventId) {
        final EventList list = new EventList(getApplicationContext(), true);
        list.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                try {
                    startOnEvent(list.findById(eventId));
                } catch (EventNotFoundException e) {
                    Log.e(TAG, "could not start control service for event id " + eventId + ": event not found");
                    stopSelf();
                }
            }
        });

    }

    private void startOnEvent(Event event) {
        this.event = event;

        SharedPreferences prefs = getSharedPreferences("ControlService", MODE_PRIVATE);
        prefs.edit().putInt(PREFS_EVENT_ID, event.getId()).commit();

        ParticipationStore store = new ParticipationStore(getApplicationContext());
        if (store.doesParticipate(event)) {
            bindService(new Intent(getApplicationContext(), SocketService.class), conn, Context.BIND_AUTO_CREATE);
        } else {
            Log.i(TAG, "not starting because we are not participating.");
            stopSelf();
        }
    }
	
	@Override
	public void receiveUpdate(JSONObject data) {
	}
	
	@Override
	public void receiveCommand(String command, JSONObject data) {
		System.out.println("received command: " + command);
		if (command.equals("shutdown")) {
			stopSelf();
			return;
		}
		if (command.equals("disableGPS")) {
			stopTracking();
			return;
		}
		if (command.equals("enableGPS")) {
			startTracking();
			return;
		}
		System.out.println("received unknown command: \"" + command + "\"");
	}

	@Override
	public void receiveStateChange(int newState) {
		if (newState == HttpSocketConnection.STATE_CONNECTED) {
			enableControlSession();
		} else {
			// stop the tracking if there is no socket connection available. there's no need to track
			// when the server is gone :)
			stopTracking();
		}
	}
}
