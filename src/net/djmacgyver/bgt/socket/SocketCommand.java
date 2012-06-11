package net.djmacgyver.bgt.socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SocketCommand {
	private String command;
	private JSONObject data;
	private Runnable callback;
	private int requestId;
	private JSONArray responseData;
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
			if (response.has("data")) {
				Object data = response.get("data");
				if (data instanceof JSONObject) {
					responseData = new JSONArray();
					responseData.put(0, data);
				} else if (data instanceof JSONArray) {
					responseData = (JSONArray) data;
				}
			}
			updateResult(response.getBoolean("success"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			runCallback();
		}
	}
	
	protected void updateResult(boolean success) {
		result = success;
		runCallback();
	}
	
	private void runCallback() {
		if (callback == null) return;
		callback.run();
		callback = null;
	}
	
	public boolean wasSuccessful()
	{
		return result;
	}
	
	public JSONArray getResponseData()
	{
		return responseData;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
}
