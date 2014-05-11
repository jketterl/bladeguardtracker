package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.dialog.CredentialsWrongDialog;
import net.djmacgyver.bgt.dialog.PasswordChangeDialog;
import net.djmacgyver.bgt.dialog.PasswordChangeFailedDialog;
import net.djmacgyver.bgt.dialog.ProgressDialog;
import net.djmacgyver.bgt.socket.CommandExecutor;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.AbstractAuthCommand;
import net.djmacgyver.bgt.socket.command.AuthenticationCommand;
import net.djmacgyver.bgt.socket.command.PasswordChangeCommand;
import net.djmacgyver.bgt.socket.command.SetTeamCommand;
import net.djmacgyver.bgt.team.TeamSelectionDialog;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class Settings extends ActionBarActivity implements TeamSelectionDialog.OnTeamSelectedListener {
    private static final String TAG = "Settings";

	private static final String DIALOG_LOGGING_IN = "dialog_logging_in";
	private static final String DIALOG_CREDENTIALS_WRONG = "dialog_credentials_wrong";
	private static final String DIALOG_PASSWORDCHANGE = "dialog_passwordchange";
	private static final String DIALOG_PASSWORDCHANGE_FAILED = "dialog_passwordchange_failed";
	private static final String DIALOG_PASSWORDCHANGE_RUNNING = "dialog_passwordchange_running";
    private static final String DIALOG_TEAM_SELECTION = "dialog_teamselection";
    private static final String DIALOG_TEAM_CHANGE_RUNNING = "dialog_teamchange_running";

    private SessionState facebookState;

    private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
            Log.d(TAG, "facebook indicates state: " + state);
	    	authentication = null;
            facebookState = state;
            updateUI();
	    }
	};
	
	private UiLifecycleHelper uiHelper;
	
	//private Boolean isLoggedIn = false;
	private AbstractAuthCommand authentication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

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
                                    showCredentialsWrongDialog();
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
                        DialogFragment teamSelection = new TeamSelectionDialog();
                        teamSelection.show(getSupportFragmentManager(), DIALOG_TEAM_SELECTION);
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
                showPasswordChangeDialog();
			}
		});
        
        Button requestButton = (Button) findViewById(R.id.requestButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle params = new Bundle();
				params.putString("message", getResources().getString(R.string.join_me));
				
				WebDialog requestDialog = (new WebDialog.RequestsDialogBuilder(Settings.this,
						                       Session.getActiveSession(),
						                       params))
						                       .setOnCompleteListener(new OnCompleteListener() {
												@Override
												public void onComplete(Bundle values, FacebookException error) {
								                    if (error != null) {
								                        if (!(error instanceof FacebookOperationCanceledException)) {
								                            Toast.makeText(getApplicationContext(), 
								                                "Network Error", 
								                                Toast.LENGTH_SHORT).show();
								                        }
								                    } else {
								                        final String requestId = values.getString("request");
								                        if (requestId != null) {
								                            Toast.makeText(getApplicationContext(), 
								                                "Request sent",  
								                                Toast.LENGTH_SHORT).show();
								                        }
								                    }   
												}
											})
						                       .build();
				requestDialog.show();
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
		View facebookOptions = findViewById(R.id.facebookOptions);
        
        if (anonymousCheckbox.isChecked()) {
        	loginOptions.setVisibility(View.GONE);
        	anonymousInfo.setVisibility(View.VISIBLE);
        	profile.setVisibility(View.GONE);
        	facebookOptions.setVisibility(View.GONE);
        } else {
        	anonymousInfo.setVisibility(View.GONE);
        	loginOptions.setVisibility(View.VISIBLE);
        	
        	if (authentication != null && authentication instanceof AuthenticationCommand ) {
                logout.setVisibility(View.VISIBLE);
                regularLogin.setVisibility(View.GONE);
                facebook.setVisibility(View.GONE);
                facebookOptions.setVisibility(View.GONE);
		        profile.setVisibility(View.VISIBLE);
        		User user = authentication.getUser();
        		if (user != null) team.setText(user.getTeamName());
        	} else if (facebookState != null && facebookState.isOpened()) {
                regularLogin.setVisibility(View.GONE);
                logout.setVisibility(View.GONE);
                facebook.setVisibility(View.VISIBLE);
                facebookOptions.setVisibility(View.VISIBLE);
                profile.setVisibility(View.VISIBLE);
                if (authentication != null) {
                    User user = authentication.getUser();
                    if (user != null) team.setText(user.getTeamName());
                }
            } else {
	            logout.setVisibility(View.GONE);
		        regularLogin.setVisibility(View.VISIBLE);
		        facebook.setVisibility(View.VISIBLE);
		        profile.setVisibility(View.GONE);
		        facebookOptions.setVisibility(View.GONE);
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
                    finish();
				} else {
                    Log.w(TAG, "login failed: " + command.getResponseData());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
                            showCredentialsWrongDialog();
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

        showLoginDialog();

		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();

				SocketCommandCallback c = new SocketCommandCallback() {
					@Override
					public void run(final SocketCommand command) {
                        dismissDialog(DIALOG_LOGGING_IN);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
                                Log.d(TAG, "got authentication: " + command);
								if (command != null && command.wasSuccessful()) authentication = (AbstractAuthCommand) command;
								updateUI();
								//setLoggedIn(command.wasSuccessful());
							}
						});
						if (callback != null) callback.run(command);
					}
				};
				
				s.getSharedConnection().addAuthCallback(c);
                unbindService(this);
			}
		};
		bindService(new Intent(this, SocketService.class), conn, Context.BIND_AUTO_CREATE);
	}

    private void showLoginDialog() {
        DialogFragment f = new ProgressDialog(R.string.logging_in);
        f.show(getSupportFragmentManager(), DIALOG_LOGGING_IN);
    }

    private void showCredentialsWrongDialog() {
        DialogFragment f = new CredentialsWrongDialog();
        f.show(getSupportFragmentManager(), DIALOG_CREDENTIALS_WRONG);
    }

    private void showPasswordChangeFailedDialog(String message) {
        DialogFragment f = new PasswordChangeFailedDialog(message);
        f.show(getSupportFragmentManager(), DIALOG_PASSWORDCHANGE_FAILED);
    }

    private void showPasswordChangeRunningDialog() {
        DialogFragment f = new ProgressDialog(R.string.password_change_running);
        f.show(getSupportFragmentManager(), DIALOG_PASSWORDCHANGE_RUNNING);
    }

    private void dismissDialog(String tag) {
        DialogFragment f = (DialogFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (f != null) f.dismiss();
    }

    private void showTeamChangeRunningDialog() {
        DialogFragment f = new ProgressDialog(R.string.team_change_running);
        f.show(getSupportFragmentManager(), DIALOG_TEAM_CHANGE_RUNNING);
    }

    private void showPasswordChangeDialog() {
        DialogFragment f = new PasswordChangeDialog() {
            @Override
            protected void onPasswordChangeFailed(int message) {
                showPasswordChangeFailedDialog(getResources().getString(message));
            }

            @Override
            protected void changePassword(final String pass) {
                showPasswordChangeRunningDialog();
                PasswordChangeCommand command = new PasswordChangeCommand(pass);
                command.addCallback(new SocketCommandCallback() {
                    @Override
                    public void run(final SocketCommand command) {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String message = "unknown error";
                                    try {
                                        message = command.getResponseData().getJSONObject(0).getString("message");
                                    } catch (JSONException ignored) {
                                    }
                                    showPasswordChangeFailedDialog(message);
                                }
                            });
                        }
                    }
                });
                CommandExecutor e = new CommandExecutor(Settings.this);
                e.execute(command);
            }
        };
        f.show(getSupportFragmentManager(), DIALOG_PASSWORDCHANGE);
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
    public void onTeamSelected(int teamId) {
        showTeamChangeRunningDialog();

        SetTeamCommand command = new SetTeamCommand(teamId);
        command.addCallback(new SocketCommandCallback() {
            @Override
            public void run(final SocketCommand command) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog(DIALOG_TEAM_CHANGE_RUNNING);
                        if (command.wasSuccessful()) {
                            TextView team = (TextView) findViewById(R.id.teamView);
                            try {
                                team.setText(command.getResponseData().getJSONObject(0).getString("name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        CommandExecutor e = new CommandExecutor(this);
        e.execute(command);
    }
}