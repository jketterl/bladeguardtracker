package net.djmacgyver.bgt.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SocketService extends Service {
	// necessary for interaction with service clients
	private Binder binder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public SocketService getService() {
			return SocketService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	// testing
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("onCreate");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("onDestroy");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		System.out.println("onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("onStartCommand");
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	// end of testing

	// instance variables
	private HttpSocketConnection sharedConn; 
	
	// public service methods
	public HttpSocketConnection getSharedConnection()
	{
		if (sharedConn == null) {
			System.out.println("starting new connection!");
			sharedConn = new HttpSocketConnection(getApplicationContext());
			sharedConn.connect();
		}
		return sharedConn;
	}
}