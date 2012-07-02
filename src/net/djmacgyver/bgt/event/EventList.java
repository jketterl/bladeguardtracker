package net.djmacgyver.bgt.event;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.ServerList;

import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class EventList extends ServerList {
	public EventList(Context context) {
		super(context);
	}
	
	@Override
	protected String getServerCommand() {
		return "getEvents";
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
		TextView date = (TextView) v.findViewById(R.id.date);
		ImageView weatherIcon = (ImageView) v.findViewById(R.id.weatherIcon);
		Event event;
		try {
			event = new Event(getData().getJSONObject(arg0));
			text.setText(event.getTitle());
			date.setText(event.getStart().toLocaleString());
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

	@Override
	public int getViewTypeCount() {
		return 1;
	}
}
