package net.djmacgyver.bgt.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.StartEventCommand;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
	private int id;
	private String title;
	private Date start;
	private Boolean weather = null;
	private String mapName;
	
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
			mapName = obj.getString("mapName");
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
		mapName = source.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeLong(start.getTime());
		dest.writeString(title);
		dest.writeInt(hasWeatherDecision() ? 1 : 0);
		if (hasWeatherDecision()) dest.writeInt(getWeatherDecision() ? 1 : 0);
		dest.writeString(mapName);
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
	
	@Override
	public int describeContents() {
		return hashCode();
	}
	
	public boolean hasWeatherDecision() {
		return weather != null;
	}
	
	public boolean getWeatherDecision() {
		return weather;
	}

	public String getMapName() {
		return mapName;
	}
	
	public SocketCommand start(Context c) {
		final SocketCommand command = new StartEventCommand(this);
		ServiceConnection s = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				HttpSocketConnection conn = ((SocketService.LocalBinder) service).getService().getSharedConnection();
				conn.sendCommand(command);
			}
		};
		c.bindService(new Intent(c, SocketService.class), s, Context.BIND_AUTO_CREATE);
		return command;
	}
}
