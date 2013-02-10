package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.HttpSocketConnection;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.AbstractAuthCommand;
import net.djmacgyver.bgt.socket.command.AuthenticationCommand;
import net.djmacgyver.bgt.socket.command.FacebookLoginCommand;
import net.djmacgyver.bgt.socket.command.PasswordChangeCommand;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class Settings extends Activity {
	public static final int DIALOG_LOGGING_IN = 1;
	public static final int DIALOG_CREDENTIALS_WRONG = 2;
	public static final int DIALOG_PASSWORDCHANGE = 3;
	public static final int DIALOG_PASSWORDCHANGE_FAILED = 4;
	public static final int DIALOG_PASSWORDCHANGE_RUNNING = 5;
	
	public static final int REQUEST_TEAM_SELECTION = 1;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	    	authentication = null;
	    	updateUI();
	    }
	};
	
	private UiLifecycleHelper uiHelper;
	
	//private Boolean isLoggedIn = false;
	private AbstractAuthCommand authentication;
	
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
				if (isChecked) Session.getActiveSession().closeAndClearTokenInformation();
				authentication = null;
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
				testLogin(new SocketCommandCallback() {
					@Override
					public void run(SocketCommand command) {
						if (!command.wasSuccessful()) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									showDialog(DIALOG_CREDENTIALS_WRONG);
								}
							});
						}
					}
				});
			}
		});
        
        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        TextView pass = (TextView) findViewById(R.id.pass);
				pass.setText("");
				authentication = null;
				updateUI();
			}
		});
        
        
        Button team = (Button) findViewById(R.id.teamButton);
        team.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testLogin(new SocketCommandCallback() {
					@Override
					public void run(SocketCommand command) {
						startActivityForResult(new Intent(Settings.this, TeamSelection.class), REQUEST_TEAM_SELECTION);
					}
				});
			}
		});
        
        
        // this does nothing but invalidate the authentication object when an input field
        // changes its value.
        TextWatcher invalidateWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				authentication = null;
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
        
        EditText user = (EditText) findViewById(R.id.user);
        user.addTextChangedListener(invalidateWatcher);
        
        EditText pass = (EditText) findViewById(R.id.pass);
        pass.addTextChangedListener(invalidateWatcher);
        
        Button passwordButton = (Button) findViewById(R.id.passwordButton);
        passwordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_PASSWORDCHANGE);
			}
		});
        
        Session.openActiveSession(this, false, callback);
	}
	
	private void updateUI() {
        View regularLogin = findViewById(R.id.regularLogin);
        View anonymousInfo = findViewById(R.id.anonymousInfoText);
        CheckBox anonymousCheckbox = (CheckBox) findViewById(R.id.anonymousCheckbox);
        View loginOptions = findViewById(R.id.loginOptions);
        View logout = findViewById(R.id.regularLogout);
        View facebook = findViewById(R.id.facbookLogin);
        View profile = findViewById(R.id.profileOptions);
		TextView team = (TextView) findViewById(R.id.teamView);
        
        if (anonymousCheckbox.isChecked()) {
        	loginOptions.setVisibility(View.GONE);
        	anonymousInfo.setVisibility(View.VISIBLE);
        	profile.setVisibility(View.GONE);
        } else {
        	anonymousInfo.setVisibility(View.GONE);
        	loginOptions.setVisibility(View.VISIBLE);
        	
        	if (authentication != null) {
        		if (authentication instanceof AuthenticationCommand) {
		            logout.setVisibility(View.VISIBLE);
			        regularLogin.setVisibility(View.GONE);
			        facebook.setVisibility(View.GONE);
        		} else if (authentication instanceof FacebookLoginCommand) {
    		        regularLogin.setVisibility(View.GONE);
    		        logout.setVisibility(View.GONE);
    		        facebook.setVisibility(View.VISIBLE);
        		}
		        profile.setVisibility(View.VISIBLE);
        		User user = authentication.getUser();
        		if (user != null) team.setText(user.getTeamName());
        	} else {
	            logout.setVisibility(View.GONE);
		        regularLogin.setVisibility(View.VISIBLE);
		        facebook.setVisibility(View.VISIBLE);
		        profile.setVisibility(View.GONE);
        	}
        }
	}
	
	/*
	private void setLoggedIn(Boolean loggedIn) {
		isLoggedIn = loggedIn;
		updateUI();
	}
	*/
	
	@Override
	public void onBackPressed() {
		testLogin(new SocketCommandCallback() {
			@Override
			public void run(SocketCommand command) {
				if (command == null || command.wasSuccessful()) {
					Settings.super.onBackPressed();
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showDialog(DIALOG_CREDENTIALS_WRONG);
						}
					});
				}
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

	private void testLogin(final SocketCommandCallback callback)
	{
		storeSettings();
		
		if (authentication != null) {
			if (callback != null) authentication.addCallback(callback);
			return;
		}

		CheckBox anonymous = (CheckBox) findViewById(R.id.anonymousCheckbox);
		if (anonymous.isChecked()) {
			if (callback != null) callback.run(null);
			return;
		}
		
		showDialog(DIALOG_LOGGING_IN);
		
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();
				final ServiceConnection conn = this;
				
				SocketCommandCallback c = new SocketCommandCallback() {
					@Override
					public void run(final SocketCommand command) {
						unbindService(conn);
						dismissDialog(DIALOG_LOGGING_IN);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (command != null && command.wasSuccessful()) authentication = (AbstractAuthCommand) command;
								updateUI();
								//setLoggedIn(command.wasSuccessful());
							}
						});
						if (callback != null) callback.run(command);
					}
				};
				
				s.getSharedConnection().addAuthCallback(c);
			}
		};
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_LOGGING_IN:
				d = new ProgressDialog(this);
				d.setCancelable(false);
				d.setTitle(R.string.logging_in);
				return d;
			case DIALOG_CREDENTIALS_WRONG:
				b.setMessage(R.string.credentials_wrong)
			     .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return b.create();
			case DIALOG_PASSWORDCHANGE:
				b.setView(getLayoutInflater().inflate(R.layout.passworddialog, null))
				 .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Dialog d = (Dialog) dialog;
						EditText passView = (EditText) d.findViewById(R.id.password);
						EditText confirmView = (EditText) d.findViewById(R.id.password_confirm);
						final String pass = passView.getText().toString();
						String confirm = confirmView.getText().toString();
						
						Bundle b = new Bundle();
						if (!pass.equals(confirm)) {
							b.putString("message", getResources().getString(R.string.password_mismatch));
							showDialog(DIALOG_PASSWORDCHANGE_FAILED, b);
							return;
						}
						
						if (pass.equals("")) {
							b.putString("message", getResources().getString(R.string.password_must_not_be_empty));
							showDialog(DIALOG_PASSWORDCHANGE_FAILED, b);
							return;
						}
						
						showDialog(DIALOG_PASSWORDCHANGE_RUNNING);
						ServiceConnection conn = new ServiceConnection() {
							@Override
							public void onServiceDisconnected(ComponentName name) {
							}
							
							@Override
							public void onServiceConnected(ComponentName name, IBinder service) {
								SocketService s = ((SocketService.LocalBinder) service).getService();
								PasswordChangeCommand command = new PasswordChangeCommand(pass);
								final ServiceConnection conn = this;
								command.addCallback(new SocketCommandCallback() {
									@Override
									public void run(SocketCommand command) {
										dismissDialog(DIALOG_PASSWORDCHANGE_RUNNING);
										if (command.wasSuccessful()) {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													EditText passView = (EditText) findViewById(R.id.pass);
													passView.setText(pass);
													testLogin(null);
												}
											});
										} else {
											final Bundle b = new Bundle();
											String message = "unknown error";
											try {
												message = command.getResponseData().getJSONObject(0).getString("message");
											} catch (JSONException e) {}
											b.putString("message", message);
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													showDialog(DIALOG_PASSWORDCHANGE_FAILED, b);
												}
											});
										}
										unbindService(conn);
									}
								});
								s.getSharedConnection().sendCommand(command);
							}
						};
						
						bindService(new Intent(Settings.this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
					}
				})
				 .setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
				});
				return b.create();
			case DIALOG_PASSWORDCHANGE_FAILED:
				b.setMessage(R.string.passwordchange_failed)
				 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					 	@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
				});
				return b.create();
			case DIALOG_PASSWORDCHANGE_RUNNING:
				d = new ProgressDialog(this);
				d.setCancelable(false);
				d.setTitle(R.string.password_change_running);
				return d;
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
		
		authentication = null;
		
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
						command.addCallback(new SocketCommandCallback() {
							@Override
							public void run(final SocketCommand command) {
								if (command.wasSuccessful()) runOnUiThread(new Runnable() {
									@Override
									public void run() {
										TextView team = (TextView) findViewById(R.id.teamView);
										try {
											team.setText(command.getResponseData().getJSONObject(0).getString("name"));
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								});
							}
						});
						HttpSocketConnection socket = ((SocketService.LocalBinder) service).getService().getSharedConnection();
						socket.sendCommand(command);
						unbindService(this);
					}
				};
				
				bindService(new Intent(Settings.this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
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

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
			case DIALOG_PASSWORDCHANGE_FAILED:
				AlertDialog d = (AlertDialog) dialog;
				String message = getResources().getString(R.string.passwordchange_failed);
				d.setMessage(message.concat("\n\n" + args.getString("message")));
		}
		super.onPrepareDialog(id, dialog, args);
	}
}