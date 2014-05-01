package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationDeleteCommand extends SocketCommand {

	public RegistrationDeleteCommand(String regId) {
		super("deleteRegistration");
		try {
			data = new JSONObject();
			data.put("regId", regId);
		} catch (JSONException ignored) {}
	}
}
