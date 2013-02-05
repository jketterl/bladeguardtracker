package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.AuthenticationCommand;
import net.djmacgyver.bgt.socket.command.SetTeamCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
	
	public static final int REQUEST_TEAM_SELECTION = 1;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	    	updateUI();
	    }
	};
	
	private UiLifecycleHelper uiHelper;
	
	private SocketCommand authentication;
	
	private class CallbackService implements ServiceConnection {
		private Runnable callback;
		private String user;
		private String pass;
		
		public void setCallback(Runnable callback) {
			this.callback = callback;
		}
		
		public void setCredentials(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SocketService sockService = ((SocketService.LocalBinder) service).getService();
			//final SocketCommand command = sockService.getSharedConnection().getAuthentication();
			authentication = new AuthenticationCommand(user, pass);
			sockService.getSharedConnection().sendCommand(authentication);
			authentication.addCallback(new Runnable() {
				@Override
				public void run() {
					final SocketCommand command = authentication;
					authentication = null;
					dismissDialog(DIALOG_LOGGING_IN);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setLoggedIn(command.wasSuccessful());
							if (command.wasSuccessful()) {
								try {
									User user = new User(command.getResponseData().getJSONObject(0));
									TextView team = (TextView) findViewById(R.id.teamView);
									team.setText(user.getTeamName());
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								runCallback();
							} else {
								showDialog(DIALOG_CREDENTIALS_WRONG);
							}
						}
					});
				}
			});
			unbindService(this);
		}
		
		private void runCallback(){
			if (callback == null) return;
			callback.run();
			callback = null;
		}
	};
	
	private CallbackService conn = new CallbackService();
	
	private Boolean isLoggedIn = false;
	
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
				testLogin(null);
			}
		});
        
        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        TextView pass = (TextView) findViewById(R.id.pass);
				pass.setText("");
				setLoggedIn(false);
			}
		});
        
        
        Button team = (Button) findViewById(R.id.teamButton);
        team.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testLogin(new Runnable() {
					@Override
					public void run() {
						startActivityForResult(new Intent(Settings.this, TeamSelection.class), REQUEST_TEAM_SELECTION);
					}
				});
			}
		});
        
        Session.openActiveSession(this, false, callback);
	}
	
	private void updateUI() {
        View regularLogin = findViewById(R.id.regularLogin);
        View anonymousInfo = findViewById(R.id.anonymousInfoText);
        CheckBox anonymousCheckbox = (CheckBox) findViewById(R.id.anonymousCheckbox);
        View loginOptions = findViewById(R.id.loginOptions);
        View logout = findViewById(R.id.logout);
        View facebook = findViewById(R.id.facbookLogin);
        View profile = findViewById(R.id.profileOptions);
        
        if (anonymousCheckbox.isChecked()) {
        	loginOptions.setVisibility(View.GONE);
        	anonymousInfo.setVisibility(View.VISIBLE);
        	profile.setVisibility(View.GONE);
        } else {
        	anonymousInfo.setVisibility(View.GONE);
        	loginOptions.setVisibility(View.VISIBLE);
	        if (Session.getActiveSession().isOpened()) {
		        regularLogin.setVisibility(View.GONE);
		        logout.setVisibility(View.GONE);
		        profile.setVisibility(View.VISIBLE);
	        } else {
		        if (isLoggedIn) {
		            logout.setVisibility(View.VISIBLE);
			        regularLogin.setVisibility(View.GONE);
			        facebook.setVisibility(View.GONE);
			        profile.setVisibility(View.VISIBLE);
		        } else {
		            logout.setVisibility(View.GONE);
			        regularLogin.setVisibility(View.VISIBLE);
			        facebook.setVisibility(View.VISIBLE);
			        profile.setVisibility(View.GONE);
		        }
	        }
        }
	}
	
	private void setLoggedIn(Boolean loggedIn) {
		isLoggedIn = loggedIn;
		updateUI();
	}
	
	@Override
	public void onBackPressed() {
		testLogin(new Runnable() {
			@Override
			public void run() {
				Settings.super.onBackPressed();
			}
		});
	}
	
	private void storeSettings() {
        CheckBox anonymousCheckbox = (CheckBox) findViewById(R.id.anonymousCheckbox);
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = p.edit();
		e.putBoolean("anonymous", anonymousCheckbox.isChecked());
		
        if (anonymousCheckbox.isChecked()) {
        	e.remove("username");
        	e.remove("password");
        } else {
	        TextView user = (TextView) findViewById(R.id.user);
	        TextView pass = (TextView) findViewById(R.id.pass);
			e.putString("username", user.getText().toString());
			e.putString("password", pass.getText().toString());
        }
        
		e.commit();
	}

	private void testLogin(Runnable callback)
	{
		CheckBox anonymous = (CheckBox) findViewById(R.id.anonymousCheckbox);
		if (isLoggedIn || anonymous.isChecked() || Session.getActiveSession().isOpened()) {
			if (callback != null) callback.run();
			return;
		}
		
		TextView user = (TextView) findViewById(R.id.user);
		TextView pass = (TextView) findViewById(R.id.pass);
		conn.setCredentials(user.getText().toString(), pass.getText().toString());
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

	@Override
	public void onResume() {
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	    	updateUI();
	    }

	    super.onResume();
	    uiHelper.onResume();
	    
        CheckBox anonymousCheckbox = (CheckBox) findViewById(R.id.anonymousCheckbox);
        TextView user = (TextView) findViewById(R.id.user);
        TextView pass = (TextView) findViewById(R.id.pass);
        
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		anonymousCheckbox.setChecked(p.getBoolean("anonymous", true));
		user.setText(p.getString("username", ""));
		pass.setText(p.getString("password", ""));
		
		isLoggedIn = false;
		
		testLogin(null);
		Session.openActiveSession(this, false, callback);
	    
	    updateUI();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	    
	    
	    if (requestCode == REQUEST_TEAM_SELECTION && resultCode == Activity.RESULT_OK) {
	    	final long teamId = data.getLongExtra("teamId", -1);
	    	if (teamId != -1) {
			    final ServiceConnection conn = new ServiceConnection() {
					@Override
					public void onServiceDisconnected(ComponentName name) {}
					
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						SetTeamCommand command = new SetTeamCommand((int) teamId);
						HttpSocketConnection socket = ((SocketService.LocalBinder) service).getService().getSharedConnection();
						socket.sendCommand(command);
						unbindService(this);
					}
				};
				
				Runnable callback = new Runnable() {
					@Override
					public void run() {
						bindService(new Intent(Settings.this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
					}
				};
				
				if (authentication != null) authentication.addCallback(callback); else callback.run();
	    	}
	    }
	}

	@Override
	public void onPause() {
		storeSettings();
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