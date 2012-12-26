package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

public class SetMapCommand extends SocketCommand {
	public SetMapCommand(Event event, int mapId) {
		super("setMap");
		try {
			data = new JSONObject();
			data.put("eventId", event.getId());
			data.put("id", mapId);
		} catch (JSONException e) {}
	}
}
