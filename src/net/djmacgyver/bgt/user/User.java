package net.djmacgyver.bgt.user;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private int id;
	private String name;
	private boolean admin;
	private String teamName;
	
	public User(JSONObject data) throws JSONException {
		id = data.getInt("uid");
		name = data.getString("name");
		admin = data.getBoolean("admin");
		teamName = data.getString("team_name");
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
	
	public String getTeamName() {
		return teamName;
	}
}
