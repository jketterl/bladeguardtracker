package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
	public static final int DIALOG_LOGGING_IN = 1;
	public static final int DIALOG_CREDENTIALS_WRONG = 2;
	
	SocketService sockService;
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			sockService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			sockService = ((SocketService.LocalBinder) service).getService();
			final SocketCommand command = sockService.getSharedConnection(this).authenticate();
			if (command != null) command.setCallback(new Runnable() {
				@Override
				public void run() {
					dismissDialog(DIALOG_LOGGING_IN);
					if (command.wasSuccessful()) {
						finish();
					} else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showDialog(DIALOG_CREDENTIALS_WRONG);
							}
						});
					}
				}
			}); else {
				dismissDialog(DIALOG_LOGGING_IN);
				finish();
			}
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
	public void onBackPressed() {
		// the service is setup to test the credentials provided automatically.
		// display a progress dialog
		showDialog(DIALOG_LOGGING_IN);
		// that way, we only have to bind the service and prevent the parent function. that's all
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		switch (id) {
			case DIALOG_LOGGING_IN:
				d = new ProgressDialog(this);
				d.setCancelable(false);
				d.setTitle(R.string.logging_in);
				return d;
			case DIALOG_CREDENTIALS_WRONG:
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setMessage(R.string.credentials_wrong)
			     .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return b.create();
		}
		return super.onCreateDialog(id);
	}
}
