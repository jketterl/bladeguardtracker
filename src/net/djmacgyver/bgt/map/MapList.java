package net.djmacgyver.bgt.map;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.ServerList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MapList extends ServerList {
	
	public MapList(Context context) {
		super(context);
	}
	
	public MapList(Context context, boolean autoLoad) {
		super(context, autoLoad);
	}
	
	@Override
	protected String getServerCommand() {
		return "getMaps";
	}
	
	@Override
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inf = LayoutInflater.from(getContext());
		View v = inf.inflate(R.layout.teamlistitem, arg2, false);
		TextView text = (TextView) v.findViewById(R.id.teamName);
		JSONObject map;
		try {
			map = getData().getJSONObject(arg0);
			text.setText(map.getString("name"));
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
