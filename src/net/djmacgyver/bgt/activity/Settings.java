package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class Settings extends Activity {
	public static final int DIALOG_LOGGING_IN = 1;
	public static final int DIALOG_CREDENTIALS_WRONG = 2;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private UiLifecycleHelper uiHelper;
	
	private class CallbackService implements ServiceConnection {
		private Runnable callback;
		
		public void setCallback(Runnable callback) {
			this.callback = callback;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService sockService = ((SocketService.LocalBinder) service).getService();
			final SocketCommand command = sockService.getSharedConnection().getAuthentication();
			if (command != null) command.addCallback(new Runnable() {
				@Override
				public void run() {
					dismissDialog(DIALOG_LOGGING_IN);
					if (command.wasSuccessful()) {
						runCallback();
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
				runCallback();
			}
			unbindService(this);
		}
		
		private void runCallback(){
			if (callback == null) return;
			callback.run();
			callback = null;
		}
	};
	
	private CallbackService conn = new CallbackService();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.settings);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.settings);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        
        CheckBox anonymous = (CheckBox) findViewById(R.id.anonymousCheckbox);
        anonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Session.getActiveSession().close();
				}
				updateUI();
			}
		});
        
        Button signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Signup.class);
				startActivity(i);
			}
		});
        
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
        
        /*
        Preference team = findPreference("team");
        team.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				testLogin(new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(Settings.this, TeamSelection.class));
					}
				});
				return true;
			}
		});
        */
        
        Session.openActiveSession(this, false, callback);
	}
	
	private void updateUI() {
        View regularLogin = findViewById(R.id.regularLogin);
        View anonymousInfo = findViewById(R.id.anonymousInfoText);
        CheckBox anonymousCheckbox = (CheckBox) findViewById(R.id.anonymousCheckbox);
        View loginOptions = findViewById(R.id.loginOptions);
        if (anonymousCheckbox.isChecked()) {
        	loginOptions.setVisibility(View.GONE);
        	anonymousInfo.setVisibility(View.VISIBLE);
        } else {
        	anonymousInfo.setVisibility(View.GONE);
        	loginOptions.setVisibility(View.VISIBLE);
	        if (Session.getActiveSession().isOpened()) {
		        regularLogin.setVisibility(View.GONE);
	        } else {
		        regularLogin.setVisibility(View.VISIBLE);
	        }
        }
	}
	
	@Override
	public void onBackPressed() {
		testLogin(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
	}
	
	private void testLogin(Runnable callback)
	{
		conn.setCallback(callback);
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

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i("facebook", "Logged in...");
	    } else if (state.isClosed()) {
	        Log.i("facebook", "Logged out...");
	    }
	    updateUI();
	}

	@Override
	public void onResume() {
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    super.onResume();
	    uiHelper.onResume();
	    
	    updateUI();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
}