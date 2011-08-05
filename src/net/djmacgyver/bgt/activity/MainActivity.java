package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.GPSListener;
import net.djmacgyver.bgt.LoginService;
import net.djmacgyver.bgt.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private GPSListener service;
	private boolean bound = false;
    ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((GPSListener.LocalBinder) binder).getService();
	        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
	        b.setChecked(service.isEnabled());
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.app_name);
        
        startService(new Intent(this, LoginService.class));
        
        startService(new Intent(this, GPSListener.class));
        bindService(new Intent(this, GPSListener.class), conn, Context.BIND_AUTO_CREATE);
        bound = true;
        
        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					service.enable();
				} else {
					service.disable();
				}
			}
		});
        
        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Map.class);
				startActivity(i);
			}
		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.config:
				Intent i = new Intent(getApplicationContext(), Settings.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bound) {
			if (!service.isEnabled()) stopService(new Intent(this, GPSListener.class));
			unbindService(conn);
			bound = false;
		}
	}
}