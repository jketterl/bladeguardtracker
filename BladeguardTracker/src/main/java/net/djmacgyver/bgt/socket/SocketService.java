package net.djmacgyver.bgt.socket;

import java.util.Vector;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.update.UpdateParser;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;

public class SocketService extends Service implements KeepAliveTarget {
	// necessary for interaction with service clients
	private Binder binder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public SocketService getService() {
			return SocketService.this;
		}
	}

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	// instance variables
	private HttpSocketConnection sharedConn;
	private Vector<Object> stakes = new Vector<Object>();
	private KeepAliveThread socketTimeout;
    private UpdateParser parser = new UpdateParser();

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
            sharedConn.addListener(parser);
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
			disconnect();
		}
	}
	
	private void disconnect() {
		if (sharedConn == null) return;
		sharedConn.disconnect();
        sharedConn.removeListener(parser);
		sharedConn = null;
		getSocketTimeout().terminate();
		socketTimeout = null;
	}

	@Override
	public void onDestroy() {
		disconnect();
		super.onDestroy();
	}

    public void registerEvent(Event e) {
        addStake(e);
        parser.registerEvent(e);
    }

    public void removeEvent(Event e) {
        parser.removeEvent(e);
        removeStake(e);
    }
}