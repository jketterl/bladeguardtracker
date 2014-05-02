package net.djmacgyver.bgt.event;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.ServerList;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.command.GetEventsCommand;

import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;

public class EventList extends ServerList {
	public EventList(Context context) {
		super(context);
	}

    public EventList(Context context, boolean autoload) {
        super(context, autoload);
    }
	
	@Override
	protected Class <? extends SocketCommand> getServerCommand() {
		return GetEventsCommand.class;
	}

	@Override
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inf = LayoutInflater.from(getContext());
		View v = inf.inflate(R.layout.eventlistitem, arg2, false);
		TextView text = (TextView) v.findViewById(R.id.title);
		TextView map = (TextView) v.findViewById(R.id.map);
		ImageView weatherIcon = (ImageView) v.findViewById(R.id.weatherIcon);
		Event event;
		try {
			event = new Event(getData().getJSONObject(arg0));
			text.setText(event.getTitle());
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
			map.setText(dateFormat.format(event.getStart()) + " " + event.getMapName());
			if (event.hasWeatherDecision()) {
				if (event.getWeatherDecision()) {
					weatherIcon.setImageResource(R.drawable.ampel_gruen);
				} else {
					weatherIcon.setImageResource(R.drawable.ampel_rot);
				}
			}
		} catch (JSONException e) {
			text.setText("undefined");
		}
		return v;
	}

    public Event findById(int id) throws EventNotFoundException {
        for (int i = 0; i < getCount(); i++) {
            try {
                Event e = new Event(getData().getJSONObject(i));
                if (e.getId() == id) return e;
            } catch (JSONException ignored) {}
        }
        throw new EventNotFoundException();
    }

	@Override
	public int getViewTypeCount() {
		return 1;
	}
}
