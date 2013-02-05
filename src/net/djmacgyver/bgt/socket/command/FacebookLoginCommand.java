package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.model.GraphUser;

public class FacebookLoginCommand extends SocketCommand {
	public FacebookLoginCommand(GraphUser user) {
		super("facebookLogin");
		data = new JSONObject();
		try {
			this.data.put("userId", user.getId());
		} catch (JSONException e) {}
	}
}
