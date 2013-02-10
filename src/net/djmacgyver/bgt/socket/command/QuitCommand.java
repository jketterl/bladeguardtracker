package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class QuitCommand extends SocketCommand {
	public QuitCommand(Event event){
		super("quit");
		data = new JSONObject();
		try {
			data.put("eventId", event.getId());
		} catch (JSONException e) {}
	}
}
