package net.djmacgyver.bgt;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.activity.MainActivity;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public static String gcmId;
	
	public GCMIntentService() {
		super(gcmId);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		System.out.println("message received: \"" + arg1.getExtras().getString("message") + "\"");
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ampel_gruen, "Server Message received", System.currentTimeMillis());
		n.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		PendingIntent i = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), 0);
		n.setLatestEventInfo(getApplicationContext(), "Message Received", arg1.getExtras().getString("message"), i);
		nm.notify(1, n);
	}

	@Override
	protected void onRegistered(Context arg0, final String regId) {
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
		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
		Log.v("GCM registration", "registered (id=\"" + regId + "\"");
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
