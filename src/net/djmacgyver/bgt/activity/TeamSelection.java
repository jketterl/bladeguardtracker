package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.SetTeamCommand;
import net.djmacgyver.bgt.team.TeamList;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
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
	public static final int DIALOG_SWITCHING = 1;
	
	private int selected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.fullscreenlist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.teamselection);
        
        setListAdapter(new TeamList(this, true));
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService s = ((SocketService.LocalBinder) service).getService();
			final SocketCommand command = s.getSharedConnection().sendCommand(new SetTeamCommand(selected));
			command.addCallback(new Runnable() {
				@Override
				public void run() {
					dismissDialog(DIALOG_SWITCHING);
					finish();
				}
			});
			unbindService(this);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		showDialog(DIALOG_SWITCHING);
		selected = (int) id;
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_SWITCHING:
				Dialog d = new ProgressDialog(this);
				d.setTitle(R.string.switching_team);
				return d;
		}
		return super.onCreateDialog(id);
	}
}
