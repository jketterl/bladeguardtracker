package net.djmacgyver.bgt;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import net.djmacgyver.bgt.activity.MainActivity;

public class GCMIntentService extends IntentService {
	public static String gcmId;
	
	public GCMIntentService() {
		super(gcmId);
	}

    /*
	@Override
	protected void onError(Context context, String message) {
		if (message.equals("ACCOUNT_MISSING")) {
			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
			if (!p.getBoolean("showGCMNotification", true)) return;
			
			Intent i = new Intent(context, GCMAccountNotification.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
	*/

    @Override
    protected void onHandleIntent(Intent intent) {
		if (!intent.hasExtra("title") || !intent.hasExtra("weather")) return;
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification n;
		PendingIntent i = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), 0);
		
		// somehow, GCM seems to convert the server int to a string
		int weather = Integer.parseInt(intent.getExtras().getString("weather"));
		String message = getResources().getString(R.string.bladenight) + ": ";
		if (weather != 0) {
			message += getResources().getString(R.string.yes_rolling);
			n = new Notification(R.drawable.ampel_gruen, message, System.currentTimeMillis());
			n.setLatestEventInfo(getApplicationContext(), intent.getExtras().getString("title"), message, i);
		} else {
			message += getResources().getString(R.string.no_cancelled);
			n = new Notification(R.drawable.ampel_rot, message, System.currentTimeMillis());
			n.setLatestEventInfo(getApplicationContext(), intent.getExtras().getString("title"), message, i);
		}
		
		n.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(1, n);
	}

    /*
	@Override
	protected void onRegistered(Context arg0, final String regId) {
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();
				SocketCommand c = new RegistrationUpdateCommand(regId);
				s.getSharedConnection().sendCommand(c);
				unbindService(this);
			}
		};
		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
		Log.v("GCM registration", "registered (id=\"" + regId + "\"");
	}

	@Override
	protected void onUnregistered(Context arg0, final String regId) {
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();
				SocketCommand c = new RegistrationDeleteCommand(regId);
				s.getSharedConnection().sendCommand(c);
				unbindService(this);
			}
		};
		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
		Log.v("GCM registration", "registered (id=\"" + regId + "\"");
	}
    */
}
