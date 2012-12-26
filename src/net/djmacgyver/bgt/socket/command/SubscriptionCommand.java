package net.djmacgyver.bgt.socket.command;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

abstract public class SubscriptionCommand extends SocketCommand {
	public SubscriptionCommand(String command) {
		super(command);
	}
	
	protected void setData(Event event, String[] categories) {
		try {
			data = new JSONObject();
			data.put("eventId", event.getId());
			JSONArray cats = new JSONArray();
			for (String cat : categories) cats.put(cat);
			data.put("category", cats);
		} catch (JSONException e) {}
	}
}
