package net.djmacgyver.bgt.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketCommand {
	private String command;
	private JSONObject data;
	
	public SocketCommand(String command, JSONObject data)
	{
		this(command);
		this.data = data;
	}
	
	public SocketCommand(String command) {
		this.command = command;
	}

	public String getJson()
	{
		JSONObject obj = new JSONObject();
		try {
			obj.put("command", command);
			if (data != null) obj.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj.toString();
	}
	
	public void updateResult()
	{
		
	}
}
