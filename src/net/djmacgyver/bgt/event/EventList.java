package net.djmacgyver.bgt.event;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.ServerList;

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
		JSONObject event;
		try {
			event = getData().getJSONObject(arg0);
			text.setText(event.getString("title"));
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
