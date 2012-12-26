package net.djmacgyver.bgt.socket;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SocketCommand {
	private String command;
	protected JSONObject data;
	private ArrayList<Runnable> callbacks = new ArrayList<Runnable>();
	private int requestId;
	private JSONArray responseData;
	private boolean result = false;
	
	public SocketCommand(String command, JSONObject data, Runnable callback) {
		this(command, data);
		addCallback(callback);
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
		Log.d("commandResult", response.toString());
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
			runCallbacks();
		}
	}
	
	protected void updateResult(boolean success) {
		result = success;
		runCallbacks();
	}
	
	private void runCallbacks() {
		Iterator<Runnable> i = callbacks.iterator();
		while (i.hasNext()) i.next().run();
		callbacks = null;
	}
	
	public boolean wasSuccessful()
	{
		return result;
	}
	
	public JSONArray getResponseData()
	{
		return responseData;
	}

	public void addCallback(Runnable callback) {
		if (callbacks != null) {
			callbacks.add(callback);
		} else {
			callback.run();
		}
	}
}
