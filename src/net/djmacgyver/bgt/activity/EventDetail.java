package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.alarm.AlarmReceiver;
import net.djmacgyver.bgt.event.Event;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
				// set up a system alarm that will wake us up when the time has come
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(EventDetail.this, AlarmReceiver.class);
				i.putExtra("event", event);
				PendingIntent sender = PendingIntent.getBroadcast(EventDetail.this, 113124, i, PendingIntent.FLAG_UPDATE_CURRENT);
				if (isChecked) {
					am.set(AlarmManager.RTC_WAKEUP, event.getControlConnectionStartTime().getTime(), sender);
				} else {
					am.cancel(sender);
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
