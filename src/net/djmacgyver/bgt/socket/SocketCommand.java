package net.djmacgyver.bgt.socket;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketCommand {
	private String command;
	private JSONObject data;
	private Runnable callback;
	private int requestId;
	private JSONObject responseData;
	private boolean result = false;
	
	public SocketCommand(String command, JSONObject data, Runnable callback) {
		this(command, data);
		setCallback(callback);
	}
	
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
			obj.put("requestId", getRequestId());
			if (data != null) obj.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj.toString();
	}
	
	protected void setRequestId(int requestId)
	{
		this.requestId = requestId;
	}
	
	protected int getRequestId()
	{
		return this.requestId;
	}
	
	public void updateResult(JSONObject response)
	{
		try {
			result = response.getBoolean("success");
			if (response.has("data")) responseData = response.getJSONObject("data");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (callback == null) return;
		callback.run();
	}
	
	public boolean wasSuccessful()
	{
		return result;
	}
	
	public JSONObject getResponseData()
	{
		return responseData;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
}
