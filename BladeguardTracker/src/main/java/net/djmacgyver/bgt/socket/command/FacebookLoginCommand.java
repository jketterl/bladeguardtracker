package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;


public class FacebookLoginCommand extends AbstractAuthCommand {
	public FacebookLoginCommand(String accessToken) {
		super ("facebookLogin");
		data = new JSONObject();
		try {
			this.data.put("accessToken", accessToken);
		} catch (JSONException e) {}
	}
}
