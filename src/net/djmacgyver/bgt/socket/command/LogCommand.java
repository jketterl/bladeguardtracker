package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class LogCommand extends SocketCommand {

	public LogCommand(Location location) {
		super("log");
		try {
			data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
		} catch (JSONException e) {}
	}
}
