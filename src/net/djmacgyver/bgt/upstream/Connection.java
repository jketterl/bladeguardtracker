package net.djmacgyver.bgt.upstream;

import android.content.Context;
import android.location.Location;

public abstract class Connection {

	public abstract void connect();

	public abstract void disconnect();

	public abstract void sendLocation(Location location);
	
	public abstract void setContext(Context context);

	public abstract void sendQuit();
}