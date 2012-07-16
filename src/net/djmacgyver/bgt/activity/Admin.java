package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Admin extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.admin);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.admin_commands);
        
        Button disableButton = (Button) findViewById(R.id.disableButton);
        disableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireCommand("disableBridges");
			}
		});
        
        Button enableButton = (Button) findViewById(R.id.enableButton);
        enableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireCommand("enableBridges");
			}
		});
	}
	
	protected void fireCommand(final String command) {
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();
				SocketCommand c = new SocketCommand(command);
				s.getSharedConnection().sendCommand(c);
				unbindService(this);
			}
		};
		
		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
	}
}
