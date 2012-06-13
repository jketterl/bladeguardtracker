package net.djmacgyver.bgt.user;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private int id;
	private String name;
	private boolean admin;
	
	public User(JSONObject data) throws JSONException {
		id = data.getInt("uid");
		name = data.getString("name");
		admin = data.getBoolean("admin");
	}
	
	public int getId(){
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAdmin() {
		return admin;
	}
}
