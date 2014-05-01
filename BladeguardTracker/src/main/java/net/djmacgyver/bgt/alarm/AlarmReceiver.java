package net.djmacgyver.bgt.alarm;

import net.djmacgyver.bgt.control.ControlService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("received alarm!");
		Intent start = new Intent(context, ControlService.class);
		start.putExtra("event", intent.getExtras().getParcelable("event"));
		context.startService(start);
	}
}
