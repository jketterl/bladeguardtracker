package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.widget.TextView;

public class Settings extends PreferenceActivity {
	SocketService sockService;
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			sockService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("connected to service");
			sockService = ((SocketService.LocalBinder) service).getService();
			sockService.getSharedConnection(this).authenticate();
			sockService.removeStake(this);
			unbindService(this);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.settings);
        addPreferencesFromResource(R.xml.settings);

        Preference signup = findPreference("signup");
        signup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(getApplicationContext(), Signup.class);
				startActivity(i);
				return true;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		startService(new Intent(this, SocketService.class));
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}
}
