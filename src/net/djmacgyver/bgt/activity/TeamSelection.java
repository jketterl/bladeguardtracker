package net.djmacgyver.bgt.activity;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.team.TeamList;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class TeamSelection extends ListActivity {
	private int selected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.fullscreenlist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.teamselection);
        
        setListAdapter(new TeamList(this));
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService s = ((SocketService.LocalBinder) service).getService();
			JSONObject data = new JSONObject();
			try {
				data.put("id", selected);
				final SocketCommand command = s.getSharedConnection().sendCommand(new SocketCommand("setTeam", data));
				command.setCallback(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			unbindService(this);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		selected = (int) id;
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}
}
