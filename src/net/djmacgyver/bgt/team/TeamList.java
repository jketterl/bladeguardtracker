package net.djmacgyver.bgt.team;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TeamList implements ListAdapter {
	private Context context;
	private JSONArray teams;
	private ArrayList<DataSetObserver> observers = new ArrayList<DataSetObserver>();

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService s = ((SocketService.LocalBinder) service).getService();
			HttpSocketConnection socket = s.getSharedConnection();
			final SocketCommand command = new SocketCommand("getTeams");
			command.setCallback(new Runnable() {
				@Override
				public void run() {
					teams = command.getResponseData();
					fireChanged();
				}
			});
			socket.sendCommand(command);
		}
	};
	
	public TeamList(Context context){
		this.context = context;
		context.bindService(new Intent(context, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public int getCount() {
		if (getTeams() == null) return 0;
		return getTeams().length();
	}

	@Override
	public Object getItem(int arg0) {
		try {
			return getTeams().getJSONObject(arg0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		try {
			return getTeams().getJSONObject(arg0).getInt("id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inf = LayoutInflater.from(context);
		View v = inf.inflate(R.layout.maplistitem, arg2, false);
		TextView text = (TextView) v.findViewById(R.id.mapName);
		JSONObject team;
		try {
			team = getTeams().getJSONObject(arg0);
			text.setText(team.getString("name"));
		} catch (JSONException e) {
			text.setText("undefined");
		}
		return v;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount() <= 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		observers.add(arg0);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		observers.remove(arg0);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

	private JSONArray getTeams() {
		return teams;
	}

	private void fireChanged() {
		h.sendEmptyMessage(0);
	}

	private Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Iterator<DataSetObserver> i = observers.iterator();
			while (i.hasNext()) i.next().onChanged();
		}
	};
}
