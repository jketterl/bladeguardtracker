package net.djmacgyver.bgt.socket;

import java.util.Vector;

import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SocketService extends Service implements KeepAliveTarget {
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
	private KeepAliveThread socketTimeout;
	
	// public service methods
	public HttpSocketConnection getSharedConnection(Object stake) {
		addStake(stake);
		return getSharedConnection();
	}
	
	public void addStake(Object obj) {
		if (stakes.contains(obj)) return;
		if (socketTimeout != null) {
			getSocketTimeout().terminate();
			socketTimeout = null;
		}
		stakes.add(obj);
	}
	
	public void removeStake(Object obj) {
		if (!stakes.contains(obj)) return;
		stakes.remove(obj);
		if (stakes.isEmpty() && sharedConn != null) {
			if (!getSocketTimeout().isAlive()) getSocketTimeout().start();
		}
	}

	public HttpSocketConnection getSharedConnection() {
		if (sharedConn == null) {
			System.out.println("starting new connection!");
			sharedConn = new HttpSocketConnection(getApplicationContext());
			sharedConn.connect();
		}
		return sharedConn;
	}

	private KeepAliveThread getSocketTimeout() {
		if (socketTimeout == null) {
			socketTimeout = new KeepAliveThread(this, 300);
		}
		return socketTimeout;
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
		if (source == getSocketTimeout()) {
			System.out.println("disconnecting now");
			sharedConn.disconnect();
			sharedConn = null;
			getSocketTimeout().terminate();
			socketTimeout = null;
		}
	}
}