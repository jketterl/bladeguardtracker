package net.djmacgyver.bgt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class LoginService extends Service {
	private Binder binder = new LocalBinder();
	private static LoginServiceThread thread;
	
	public class LocalBinder extends Binder {
		public LoginService getService() {
			return LoginService.this;
		}
	}
	
	private static void startThread(Context context) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		String user = p.getString("username", "");
		String pass = p.getString("password", "");
		if (thread == null) {
			thread = new LoginServiceThread(context, user, pass);
			thread.start();
		} else {
			thread.setCredentials(user, pass);
		}
	}
	
	private static void stopThread() {
		if (thread == null) return;
		thread.terminate();
		thread = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (!p.getBoolean("anonymous", true)) {
			startThread(getApplicationContext());
			return START_STICKY;
		} else {
			stopThread();
			stopSelf();
			return START_NOT_STICKY;
		}
	}
}
