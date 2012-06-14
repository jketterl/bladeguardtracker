package net.djmacgyver.bgt.activity;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.alarm.AlarmReceiver;
import net.djmacgyver.bgt.control.ControlService;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.user.User;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventDetail extends Activity {
	private Event event;
	
	private class SingleCommandConnection implements ServiceConnection {
		private SocketCommand command;
		
		private SingleCommandConnection(SocketCommand c) {
			command = c;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			SocketService s = ((SocketService.LocalBinder) arg1).getService();
			s.getSharedConnection().sendCommand(command);
			unbindService(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	}
	
	private class EventBoundOnClickListener implements View.OnClickListener {
		private String command;
		
		private EventBoundOnClickListener(String command) {
			this.command = command;
		}
		
		@Override
		public void onClick(View v) {
			try {
				JSONObject data = new JSONObject();
				data.put("eventId", event.getId());
				SocketCommand command = new SocketCommand(this.command, data);
				bindService(
						new Intent(EventDetail.this, SocketService.class),
						new SingleCommandConnection(command),
						Context.BIND_AUTO_CREATE
				);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.eventdetail);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.event);
        
        if (savedInstanceState != null) event = savedInstanceState.getParcelable("event");
        
        CheckBox c = (CheckBox) findViewById(R.id.participateCheckbox);
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Date start = event.getControlConnectionStartTime();
				if (start.before(new Date())) {
					if (isChecked) {
						Intent i = new Intent(EventDetail.this, ControlService.class);
						i.putExtra("event", event);
						startService(i);
					} else {
						ServiceConnection conn = new ServiceConnection() {
							@Override
							public void onServiceDisconnected(ComponentName name) {
							}
							
							@Override
							public void onServiceConnected(ComponentName name, IBinder service) {
								ControlService s = ((ControlService.LocalBinder) service).getService();
								s.shutdown();
								unbindService(this);
							}
						};
						bindService(new Intent(EventDetail.this, ControlService.class), conn, Context.BIND_AUTO_CREATE);
					}
				} else {
					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					Intent i = new Intent(EventDetail.this, AlarmReceiver.class);
					i.putExtra("event", event);
					PendingIntent sender = PendingIntent.getBroadcast(EventDetail.this, 113124, i, PendingIntent.FLAG_UPDATE_CURRENT);
					// set up a system alarm that will wake us up when the time has come
					if (isChecked) {
						am.set(AlarmManager.RTC_WAKEUP, start.getTime(), sender);
					} else {
						am.cancel(sender);
					}
				}
			}
		});
        
        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new EventBoundOnClickListener("startEvent"));
        
        Button pause = (Button) findViewById(R.id.pauseButton);
        pause.setOnClickListener(new EventBoundOnClickListener("pauseEvent"));
        
        Button shutdown = (Button) findViewById(R.id.shutdownButton);
        shutdown.setOnClickListener(new EventBoundOnClickListener("shutdownEvent"));
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		// show the admin section, if the user is an admin
        User user = Session.getUser();
        LinearLayout adminLayout = (LinearLayout) findViewById(R.id.adminArea);
        if (user != null && user.isAdmin()) {
	        adminLayout.setVisibility(View.VISIBLE);
        } else {
        	adminLayout.setVisibility(View.GONE);
        }		
		
        event = getIntent().getExtras().getParcelable("event");
        
        TextView title = (TextView) findViewById(R.id.titleView);
        title.setText(event.getTitle());
        
        TextView start = (TextView) findViewById(R.id.startView);
        start.setText(event.getStart().toLocaleString());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("event", event);
	}
}
