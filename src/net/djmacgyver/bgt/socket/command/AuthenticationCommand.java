package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.socket.SocketCommand;

public class AuthenticationCommand extends SocketCommand {

	public AuthenticationCommand(String user, String pass) {
		super("auth");
		try {
			data = new JSONObject();
			data.put("user", user);
			data.put("pass", pass);
		} catch (JSONException e) {}
	}

}
