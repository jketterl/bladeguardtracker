package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;

abstract public class AbstractAuthCommand extends SocketCommand {
	private User user;
	
	public AbstractAuthCommand(String command) {
		super(command);
	}

	@Override
	protected void updateResult(boolean success) {
		if (success) {
			try {
				user = new User(getResponseData().getJSONObject(0));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		super.updateResult(success);
	}

    public User getUser() {
		return user;
	}
}