package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.SignupCommand;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Signup extends PreferenceActivity {
	static final int DIALOG_SIGNUP_RUNNING = 0;
	static final int DIALOG_SIGNUP_SUCCESSFUL = 1;
	static final int DIALOG_SIGNUP_FAILED = 2;
	
	private class Result
	{
		public boolean success;
		public JSONArray data;
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService s = ((SocketService.LocalBinder) service).getService();
			String user = ((EditTextPreference) findPreference("username")).getEditText().getText().toString();
			String pass = ((EditTextPreference) findPreference("password")).getEditText().getText().toString();
			final SocketCommand c = new SignupCommand(user, pass);
			c.addCallback(new SocketCommandCallback() {
				@Override
				public void run(SocketCommand command) {
					Message msg = new Message();
					Result res = new Result();
					res.success = c.wasSuccessful();
					res.data = c.getResponseData();
					msg.obj = res;
					handler.sendMessage(msg);
				}
			});
			s.getSharedConnection().sendCommand(c);
			unbindService(this);
		}
	};
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dismissDialog(DIALOG_SIGNUP_RUNNING);
			Result res = (Result) msg.obj;
			if (!res.success) {
				String message = "unknown error";
				try {
					message = res.data.getJSONObject(0).getString("message");
				} catch (JSONException e) {};
				Bundle b = new Bundle();
				b.putString("message", message);
				showDialog(DIALOG_SIGNUP_FAILED, b);
			} else {
				showDialog(DIALOG_SIGNUP_SUCCESSFUL);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.signup);

        Preference signup = findPreference("signup");

        signup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return signUp();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_SIGNUP_RUNNING:
				d = new ProgressDialog(this);
				d.setTitle(R.string.signup_progress);
				break;
			case DIALOG_SIGNUP_SUCCESSFUL:
				builder.setMessage(R.string.signup_successful)
					   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
					   });
				d = builder.create();
				break;
			case DIALOG_SIGNUP_FAILED:
				builder.setMessage(R.string.signup_failed)
					   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
					   });
				d = builder.create();
				break;
			default:
				d = super.onCreateDialog(id);
		}
		return d;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
			case DIALOG_SIGNUP_FAILED:
				AlertDialog d = (AlertDialog) dialog;
				String message = getResources().getString(R.string.signup_failed);
				d.setMessage(message.concat("\n\n" + args.getString("message")));
		}
		super.onPrepareDialog(id, dialog, args);
	}

	private boolean signUp() {
		String username = ((EditTextPreference) findPreference("username")).getEditText().getText().toString();
		String password = ((EditTextPreference) findPreference("password")).getEditText().getText().toString();
		String passwordConfirmation = ((EditTextPreference) findPreference("password_confirm")).getEditText().getText().toString();
		
		Bundle b = new Bundle();
		
		if (username.equals("")) {
			b.putString("message", getResources().getString(R.string.username_must_not_be_empty));
			showDialog(DIALOG_SIGNUP_FAILED, b);
			return false;
		}
		if (password.equals("")) {
			b.putString("message", getResources().getString(R.string.password_must_not_be_empty));
			showDialog(DIALOG_SIGNUP_FAILED, b);
			return false;
		}
		if (!password.equals(passwordConfirmation)) {
			b.putString("message", getResources().getString(R.string.password_mismatch));
			showDialog(DIALOG_SIGNUP_FAILED, b);
			return false;
		}
		
		showDialog(DIALOG_SIGNUP_RUNNING);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				bindService(new Intent(Signup.this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
			}
		}).start();
		
		return true;
	}
}
