package net.djmacgyver.bgt.socket;

import java.util.Vector;

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

	// instance variables
	private HttpSocketConnection sharedConn;
	private Vector<Object> stakes = new Vector<Object>();
	
	// public service methods
	public HttpSocketConnection getSharedConnection(Object stake) {
		if (sharedConn == null) {
			System.out.println("starting new connection!");
			sharedConn = new HttpSocketConnection(getApplicationContext());
			sharedConn.connect();
		}
		addStake(stake);
		return sharedConn;
	}
	
	public void addStake(Object obj) {
		if (stakes.contains(obj)) return;
		stakes.add(obj);
	}
	
	public void removeStake(Object obj) {
		stakes.remove(obj);
		if (stakes.isEmpty() && sharedConn != null) {
			sharedConn.disconnect();
			sharedConn = null;
		}
	}
}