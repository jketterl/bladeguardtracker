package net.djmacgyver.bgt;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import android.support.v4.app.NotificationCompat.Builder;

import net.djmacgyver.bgt.activity.MainActivity;

public class GcmIntentService extends IntentService {
    private static final String TAG = "GcmIntentService";
    private static final int WEATHER_NOTIFICATION = 1;

	public GcmIntentService() {
		super("GcmIntentService");
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
        if (intent.hasExtra("type")) {
            String type = intent.getStringExtra("type");
            Log.d(TAG, "received message of type '" + type + "'");
            if (type.equals("weather")) {
                buildWeatherNotification(intent);
            } else if (type.equals("test")) {
                /*
                // this should not get into production. production versions should silently
                // ignore test messages
                Intent i = new Intent();
                i.putExtra("title", "Blade Night test");
                i.putExtra("weather", "0");
                buildWeatherNotification(i);
                */
            } else {
                Log.w(TAG, "unable to handle message of type '" + type + "'");
            }
        }
	}

    private void buildWeatherNotification(Intent intent) {
        if (!intent.hasExtra("title") || !intent.hasExtra("weather")) return;

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification n;
        PendingIntent i = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), 0);

        // somehow, GCM seems to convert the server int to a string
        int weather = Integer.parseInt(intent.getStringExtra("weather"));
        String message = getResources().getString(R.string.bladenight) + ": ";
        String title = intent.getStringExtra("title");
        Builder b = new Builder(getApplicationContext());
        if (weather != 0) {
            message += getResources().getString(R.string.yes_rolling);
            b.setSmallIcon(R.drawable.ampel_gruen);
        } else {
            message += getResources().getString(R.string.no_cancelled);
            b.setSmallIcon(R.drawable.ampel_rot);
        }

        b.setTicker(message)
                .setContentText(message)
                .setContentIntent(i)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis());
        n = b.build();

		n.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		n.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(WEATHER_NOTIFICATION, n);
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
