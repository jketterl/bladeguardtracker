package net.djmacgyver.bgt.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	private int id;
	private String title;
	private Date start;
	private Date controlConnectionStartTime;
	
	public Event(JSONObject obj) {
		try {
			id = obj.getInt("id");
			title = obj.getString("title");
			// parse the event start time
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			start = format.parse(obj.getString("start"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Date getStart() {
		return start;
	}
	
	public Date getControlConnectionStartTime() {
		if (controlConnectionStartTime == null) {
			// we want the control connection to be up 2 hours in advance, so adjust the start time accordingly
			Calendar c = Calendar.getInstance();
			c.setTime(getStart());
			c.add(Calendar.HOUR, -2);
			controlConnectionStartTime = c.getTime();
		}
		return controlConnectionStartTime;
	}
}
