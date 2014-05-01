package net.djmacgyver.bgt.socket.command;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.socket.SocketCommand;

public class SetTeamCommand extends SocketCommand {

	public SetTeamCommand(int teamId) {
		super("setTeam");
		try {
			data = new JSONObject();
			data.put("id", teamId);
		} catch (JSONException ignored) {}
	}

}
