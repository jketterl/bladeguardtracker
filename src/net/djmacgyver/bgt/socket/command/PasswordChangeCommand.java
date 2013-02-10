package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class PasswordChangeCommand extends SocketCommand {
	public PasswordChangeCommand(String newPassword) {
		super("changePassword");
		data = new JSONObject();
		try {
			data.put("pass", newPassword);
		} catch (JSONException e) {}
	}
}
