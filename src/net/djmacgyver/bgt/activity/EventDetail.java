package net.djmacgyver.bgt.activity;

import java.util.Date;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.alarm.AlarmReceiver;
import net.djmacgyver.bgt.control.ControlService;
import net.djmacgyver.bgt.event.Event;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class EventDetail extends Activity {
	private Event event;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.eventdetail);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.event);
        
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
								unbindService(this);
							}
							
							@Override
							public void onServiceConnected(ComponentName name, IBinder service) {
								ControlService s = ((ControlService.LocalBinder) service).getService();
								s.shutdown();
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
    }

	@Override
	protected void onResume() {
		super.onResume();
        event = getIntent().getExtras().getParcelable("event");
        
        TextView title = (TextView) findViewById(R.id.titleView);
        title.setText(event.getTitle());
        
        TextView start = (TextView) findViewById(R.id.startView);
        start.setText(event.getStart().toLocaleString());
	}
}
