package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.socket.SocketCommand;

public class RegistrationUpdateCommand extends SocketCommand {

	public RegistrationUpdateCommand(String regId) {
		super("updateRegistration");
		try {
			data = new JSONObject();
			data.put("regId", regId);
		} catch (JSONException e) {}
	}
}
