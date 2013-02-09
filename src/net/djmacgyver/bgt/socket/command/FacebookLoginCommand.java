package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.model.GraphUser;

public class FacebookLoginCommand extends AbstractAuthCommand {
	public FacebookLoginCommand() {
		super ("facebookLogin");
	}
	
	public FacebookLoginCommand(GraphUser user) {
		this();
		setUser(user);
	}
	
	public void setUser(GraphUser user) {
		data = new JSONObject();
		try {
			this.data.put("userId", user.getId());
		} catch (JSONException e) {}
	}
}
