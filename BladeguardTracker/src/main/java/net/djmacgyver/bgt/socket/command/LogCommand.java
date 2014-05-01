package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class LogCommand extends SocketCommand {
	private int position;
	private double distanceToFront;
	private double distanceToEnd;

	public LogCommand(Event event, Location location) {
		super("log");
		try {
			data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
			data.put("eventId", event.getId());
		} catch (JSONException ignored) {}
	}

	@Override
	public void updateResult(boolean success) {
		if (success) {
			position = extractPosition(getResponseData());
			distanceToEnd = extractDistanceToEnd(getResponseData());
			distanceToFront = extractDistanceToFront(getResponseData());
		}
		super.updateResult(success);
	}
	
	protected int extractPosition(JSONArray resultArray) {
		try {
			JSONObject result = resultArray.getJSONObject(0);
			if (result.has("locked") && result.getBoolean("locked")) {
				return result.getInt("index");
			}
		} catch (JSONException ignored) {}
		return -1;
	}
	
	protected double extractDistanceToFront(JSONArray resultArray) {
		try {
			JSONObject result = resultArray.getJSONObject(0);
			if (result.has("distanceToFront")) return result.getDouble("distanceToFront");
		} catch (JSONException ignored) {}
		return -1;
	}
	
	protected double extractDistanceToEnd(JSONArray resultArray) {
		try {
			JSONObject result = resultArray.getJSONObject(0);
			if (result.has("distanceToEnd")) return result.getDouble("distanceToEnd");
		} catch (JSONException ignored) {}
		return -1;
	}
	
	public boolean hasPosition() {
		return position >= 0;
	}
	
	public int getPosition() {
		return position;
	}
	
	public boolean hasDistanceToFront() {
		return distanceToFront >= 0;
	}
	
	public double getDistanceToFront() {
		return distanceToFront;
	}
	
	public boolean hasDistanceToEnd() {
		return distanceToEnd >= 0;
	}
	
	public double getDistanceToEnd() {
		return distanceToEnd;
	}
}
