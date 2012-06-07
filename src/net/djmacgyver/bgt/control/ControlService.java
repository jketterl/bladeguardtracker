package net.djmacgyver.bgt.control;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.GPSListener;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ControlService extends Service implements HttpSocketListener {
	private HttpSocketConnection socket;
	private JSONObject event;

	@Override
	public IBinder onBind(Intent intent) {
		// nothing to bind here (for now)
		return null;
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		private SocketService s;
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			s.removeStake(ControlService.this);
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			s = ((SocketService.LocalBinder) service).getService();
			setConnection(s.getSharedConnection(ControlService.this));
		}
	};

	@Override
	public void onCreate() {
		System.out.println("ControlService created.");
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		System.out.println("ControlService destroyed");
	}
	
	private void setConnection(HttpSocketConnection socket) {
		this.socket = socket;
		JSONObject data = new JSONObject();
		try {
			data.put("eventId", event.getInt("id"));
		} catch (JSONException e) {}
		final SocketCommand command = new SocketCommand("enableControl", data);
		command.setCallback(new Runnable() {
			@Override
			public void run() {
				if (!command.wasSuccessful()) stopSelf();
			}
		});
		socket.sendCommand(command);
		
		socket.addListener(this);
	}
	
	private void shutdown() {
		if (socket != null) {
			socket.sendCommand("disableControl");
			socket.removeListener(this);
		}
		if (trackingEnabled) {
			ServiceConnection conn = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					GPSListener l = ((GPSListener.LocalBinder) service).getService();
					l.disable();
					unbindService(this);
				}
			};
			bindService(new Intent(getApplicationContext(), GPSListener.class), conn, Context.BIND_AUTO_CREATE);
		}
		stopSelf();
	}
	
	private boolean trackingEnabled = false;
	
	private void startTracking() {
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
			}
			
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				GPSListener l = ((GPSListener.LocalBinder) arg1).getService();
				l.enable();
				unbindService(this);
			}
		};
		
		startService(new Intent(getApplicationContext(), GPSListener.class));
		bindService(new Intent(getApplicationContext(), GPSListener.class), conn, Context.BIND_AUTO_CREATE);
		trackingEnabled = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			event = new JSONObject(intent.getExtras().getString("event"));
			bindService(new Intent(getApplicationContext(), SocketService.class), conn, Context.BIND_AUTO_CREATE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return START_NOT_STICKY;
	}
	
	@Override
	public void receiveUpdate(JSONObject data) {
	}
	
	@Override
	public void receiveCommand(String command, JSONObject data) {
		System.out.println("received command: " + command);
		if (command.equals("shutdown")) {
			shutdown();
			return;
		}
		if (command.equals("enableGPS")) {
			startTracking();
			return;
		}
		System.out.println("received unknown command: \"" + command + "\"");
	}
}
