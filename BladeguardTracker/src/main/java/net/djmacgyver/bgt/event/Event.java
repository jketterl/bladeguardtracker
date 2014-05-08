package net.djmacgyver.bgt.event;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.event.update.Movement;
import net.djmacgyver.bgt.event.update.Quit;
import net.djmacgyver.bgt.event.update.Stats;
import net.djmacgyver.bgt.event.update.Update;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.StartEventCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Event implements Parcelable {
    public static final String MOVEMENTS = "movements";
    public static final String MAP = "map";
    public static final String QUIT = "quit";
    public static final String STATS = "stats";

    @SuppressWarnings("unused")
    private final static String TAG = "Event";

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

    private final Map<String, List<EventListener>> listeners = new HashMap<String, List<EventListener>>();

    private Map<EventListener, LocalSocketServiceConnection> connections = new HashMap<EventListener, LocalSocketServiceConnection>();

    public void receiveUpdate(List<Update> updates) {
        List<Movement> movements = new LinkedList<Movement>();
        List<Quit> quits = new LinkedList<Quit>();

        for (Update u : updates) {
            if (u instanceof EventMap) {
                fireEventMap((EventMap) u);
            } else if (u instanceof Movement) {
                movements.add((Movement) u);
            } else if (u instanceof Quit) {
                quits.add((Quit) u);
            } else if (u instanceof Stats) {
                fireStats((Stats) u);
            }
        }

        fireMovements(movements);
        fireQuits(quits);
    }

    private void fireStats(Stats stats) {
        synchronized (listeners) {
            if (!listeners.containsKey(STATS)) return;
            for (EventListener l : listeners.get(STATS)) l.onStats(stats);
        }
    }

    private void fireQuits(List<Quit> quits) {
        synchronized (listeners) {
            if (!listeners.containsKey(QUIT)) return;
            for (EventListener l : listeners.get(QUIT)) l.onQuit(quits);
        }
    }

    private void fireEventMap(EventMap e) {
        synchronized (listeners) {
            if (!listeners.containsKey(MAP)) return;
            for (EventListener l : listeners.get(MAP)) l.onMap(e);
        }
    }

    private void fireMovements(List<Movement> m) {
        synchronized (listeners) {
            if (!listeners.containsKey(MOVEMENTS)) return;
            for (EventListener l : listeners.get(MOVEMENTS)) l.onMovement(m);
        }
    }

    private class LocalSocketServiceConnection implements ServiceConnection {
        private final String[] types;

        public LocalSocketServiceConnection(String[] types) {
            this.types = types;
        }

        HttpSocketConnection socket;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SocketService s = ((SocketService.LocalBinder) iBinder).getService();
            s.registerEvent(Event.this);
            socket = s.getSharedConnection(Event.this);
            socket.subscribeUpdates(Event.this, types);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    }

    public void subscribeUpdates(EventListener listener, String... types) {
        synchronized (listeners) {
            for (String type : types) {
                List<EventListener> tl;
                if (listeners.containsKey(type)) {
                    tl = listeners.get(type);
                } else {
                    tl = new LinkedList<EventListener>();
                    listeners.put(type, tl);
                }
                tl.add(listener);
            }
        }

        LocalSocketServiceConnection connection = new LocalSocketServiceConnection(types);
        Context c = listener.getContext();
        c.bindService(new Intent(c, SocketService.class), connection, Context.BIND_AUTO_CREATE);
        connections.put(listener, connection);

    }

    public void unsubscribeUpdates(EventListener listener) {
        List<String> toRemove = new LinkedList<String>();

        synchronized (listeners) {
            for (String type : listeners.keySet()) {
                List<EventListener> tl = listeners.get(type);
                tl.remove(listener);
                if (tl.isEmpty()) toRemove.add(type);
            }

            for (String type : toRemove) listeners.remove(type);
        }

        if (!connections.containsKey(listener)) return;
        listener.getContext().unbindService(connections.get(listener));
    }
}
