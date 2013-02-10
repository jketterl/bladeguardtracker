package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class LogCommand extends SocketCommand {
	private int position;

	public LogCommand(Event event, Location location) {
		super("log");
		try {
			data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
			data.put("eventId", event.getId());
		} catch (JSONException e) {}
	}

	@Override
	public void updateResult(boolean success) {
		if (success) {
			position = extractPosition(getResponseData());
		}
		super.updateResult(success);
	}
	
	protected int extractPosition(JSONArray resultArray) {
		try {
			JSONObject result = resultArray.getJSONObject(0);
			if (result.has("locked") && result.getBoolean("locked")) {
				return result.getInt("index");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public boolean hasPosition() {
		return position > 0;
	}
	
	public int getPosition() {
		return position;
	}
}
