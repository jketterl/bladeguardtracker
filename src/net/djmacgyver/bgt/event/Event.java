package net.djmacgyver.bgt.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
	private int id;
	private String title;
	private Date start;
	private Date controlConnectionStartTime;
	private Boolean weather = null;
	
	public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
		@Override
		public Event createFromParcel(Parcel source) {
			return new Event(source);
		}

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}
	};
	
	public Event(JSONObject obj) {
		try {
			id = obj.getInt("id");
			title = obj.getString("title");
			// parse the event start time
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			start = format.parse(obj.getString("start"));
			if (obj.has("weather") && !obj.isNull("weather")) {
				weather = obj.getInt("weather") != 0;
			} else {
				weather = null;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Event(Parcel source) {
		id = source.readInt();
		start = new Date(source.readLong());
		title = source.readString();
		if (source.readInt() != 0) {
			weather = source.readInt() != 0;
		} else {
			weather = null;
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeLong(start.getTime());
		dest.writeString(title);
		dest.writeInt(hasWeatherDecision() ? 1 : 0);
		if (hasWeatherDecision()) dest.writeInt(getWeatherDecision() ? 1 : 0);
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

	@Override
	public int describeContents() {
		return hashCode();
	}
	
	public boolean hasWeatherDecision() {
		return weather != null;
	}
	
	public boolean getWeatherDecision() {
		return weather.booleanValue();
	}
}
