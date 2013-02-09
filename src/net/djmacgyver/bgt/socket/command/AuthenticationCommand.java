package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationCommand extends AbstractAuthCommand {

	public AuthenticationCommand(String user, String pass) {
		super("auth");
		try {
			data = new JSONObject();
			data.put("user", user);
			data.put("pass", pass);
		} catch (JSONException e) {}
	}

}
