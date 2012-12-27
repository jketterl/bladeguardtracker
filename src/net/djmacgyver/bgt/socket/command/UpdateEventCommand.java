package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateEventCommand extends SocketCommand {

	public UpdateEventCommand(Event event, int weather) {
		super("updateEvent");
		data = new JSONObject();
		try {
			data.put("eventId", event.getId());
			data.put("weather", weather);
		} catch (JSONException e) {}
	}
}
