package net.djmacgyver.bgt.upstream;

import android.location.Location;

public abstract class Connection {

	public abstract void connect();

	public abstract void disconnect();

	public abstract void sendLocation(Location location);
	
	public abstract void sendQuit();
}