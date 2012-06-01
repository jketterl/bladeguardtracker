package net.djmacgyver.bgt.socket;

import org.json.JSONObject;

public interface HttpSocketListener {
	public void receiveUpdate(JSONObject data);
}
