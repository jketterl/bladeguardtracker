package net.djmacgyver.bgt.activity;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.alarm.AlarmReceiver;
import net.djmacgyver.bgt.control.ControlService;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.PauseEventCommand;
import net.djmacgyver.bgt.socket.command.ShutdownEventCommand;
import net.djmacgyver.bgt.socket.command.StartEventCommand;
import net.djmacgyver.bgt.socket.command.UpdateEventCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventDetail extends Activity {
	private Event event;
	
	public static final int DIALOG_CONFIRM = 1;
	public static final int DIALOG_PERFORMING_COMMAND = 2;
	public static final int DIALOG_ERROR = 3;
	public static final int DIALOG_WEATHER_DECISION = 4;
	
	private class SingleCommandConnection implements ServiceConnection {
		private SocketCommand command;
		
		private SingleCommandConnection(SocketCommand c) {
			command = c;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			SocketService s = ((SocketService.LocalBinder) arg1).getService();
			command.addCallback(new SocketCommandCallback() {
				@Override
				public void run(SocketCommand command) {
					dismissDialog(DIALOG_PERFORMING_COMMAND);
					if (!command.wasSuccessful()) {
						String message = "unknown error";
						try {
							message = command.getResponseData().getJSONObject(0).getString("message");
						} catch (JSONException e) {}
						Message m = new Message();
						m.obj = message;
						h.sendMessage(m);
					}
				}
			});
			s.getSharedConnection().sendCommand(command);
			unbindService(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	}
	
	private Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle b = new Bundle();
			b.putString("message", (String) msg.obj);
			showDialog(DIALOG_ERROR, b);
		}
	};
	
	private class EventBoundOnClickListener implements View.OnClickListener {
		private Class <? extends SocketCommand> command;
		private int message;
		
		private EventBoundOnClickListener(Class <? extends SocketCommand> command, int message) {
			this.command = command;
			this.message = message;
		}
		
		@Override
		public void onClick(View v) {
			Bundle b = new Bundle();
			b.putString("command", command.getCanonicalName());
			b.putInt("message", message);
			showDialog(DIALOG_CONFIRM, b);
		}
		
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventdetail);

        if (savedInstanceState != null) event = savedInstanceState.getParcelable("event");
        
        CheckBox c = (CheckBox) findViewById(R.id.participateCheckbox);
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				participate(isChecked);
		        
				Date start = event.getControlConnectionStartTime();
				if (start.before(new Date())) {
					if (isChecked) {
						Intent i = new Intent(EventDetail.this, ControlService.class);
						i.putExtra("event", event);
						startService(i);
					} else {
						stopService(new Intent(EventDetail.this, ControlService.class));
					}
				} else {
					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					Intent i = new Intent(EventDetail.this, AlarmReceiver.class);
					i.putExtra("event", event);
					PendingIntent sender = PendingIntent.getBroadcast(EventDetail.this, 113124, i, PendingIntent.FLAG_UPDATE_CURRENT);
					// set up a system alarm that will wake us up when the time has come
					if (isChecked) {
						am.set(AlarmManager.RTC_WAKEUP, start.getTime(), sender);
					} else {
						am.cancel(sender);
					}
				}
			}
		});
        
        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new EventBoundOnClickListener(StartEventCommand.class, R.string.activating_trackers));
        
        Button pause = (Button) findViewById(R.id.pauseButton);
        pause.setOnClickListener(new EventBoundOnClickListener(PauseEventCommand.class, R.string.deactivating_trackers));
        
        Button shutdown = (Button) findViewById(R.id.shutdownButton);
        shutdown.setOnClickListener(new EventBoundOnClickListener(ShutdownEventCommand.class, R.string.terminating_event));

        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Map.class);
				i.putExtra("event", event);
				startActivity(i);
			}
		});
        
        Button weatherButton = (Button) findViewById(R.id.weatherButton);
        weatherButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_WEATHER_DECISION);
			}
		});
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		// show the admin section, if the user is an admin
        User user = Session.getUser();
        LinearLayout adminLayout = (LinearLayout) findViewById(R.id.adminArea);
        if (user != null && user.isAdmin()) {
	        adminLayout.setVisibility(View.VISIBLE);
        } else {
        	adminLayout.setVisibility(View.GONE);
        }		
		
        event = getIntent().getExtras().getParcelable("event");
        
        TextView title = (TextView) findViewById(R.id.titleView);
        title.setText(event.getTitle());
        
        TextView start = (TextView) findViewById(R.id.startView);
        start.setText(event.getStart().toLocaleString());
        
        TextView weatherView = (TextView) findViewById(R.id.weatherView);
        if (event.hasWeatherDecision()) {
        	Drawable ampel;
        	if (event.getWeatherDecision()) {
        		weatherView.setText(R.string.yes_rolling);
        		ampel = getResources().getDrawable(R.drawable.ampel_gruen);
        	} else {
        		weatherView.setText(R.string.no_cancelled);
        		ampel = getResources().getDrawable(R.drawable.ampel_rot);
        	}
        	int dips = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    		ampel.setBounds(0, 0, dips, dips);
    		weatherView.setCompoundDrawablePadding(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics())));
    		weatherView.setCompoundDrawables(ampel, null, null, null);
        } else {
        	weatherView.setText(R.string.undecided);
        	weatherView.setCompoundDrawables(null, null, null, null);
        }
        
        CheckBox c = (CheckBox) findViewById(R.id.participateCheckbox);
		c.setChecked(isParticipating());
		
		TextView mapView = (TextView) findViewById(R.id.mapView);
		mapView.setText(event.getMapName());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("event", event);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_CONFIRM:
				b.setTitle(R.string.are_you_sure)
				// seems like we have to set some message & listener here... otherwise the corresponding
				// dialog components will not be shown, even if we configure them lateer on...
				 .setMessage(R.string.are_you_sure)
				 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				})
				 .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dismissDialog(DIALOG_CONFIRM);
					}
				});
				return b.create();
			case DIALOG_PERFORMING_COMMAND:
				Dialog d = new ProgressDialog(this);
				d.setTitle(R.string.command_executing);
				return d;
			case DIALOG_ERROR:
				b.setMessage(R.string.command_error)
				 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				return b.create();
			case DIALOG_WEATHER_DECISION:
				CharSequence[] items = {
					getResources().getString(R.string.no_cancelled),
					getResources().getString(R.string.yes_rolling)
				};
				System.out.println(items);
				b.setTitle(R.string.weather_decision)
				 .setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Bundle b = new Bundle();
						b.putInt("message", R.string.update_weather);
						b.putString("command", UpdateEventCommand.class.getCanonicalName());
						b.putInt("weather", which);
						showDialog(DIALOG_CONFIRM, b);
					}
				})
				 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
			case DIALOG_CONFIRM:
				AlertDialog d = (AlertDialog) dialog;
				d.setMessage(getResources().getString(args.getInt("message")));
			
				final SocketCommand command;
				try {
					Class<?> commandClass = getClassLoader().loadClass(args.getString("command"));
					if (args.containsKey("weather")) {
						JSONObject data = new JSONObject();
						try {
							data.put("weather", args.getInt("weather"));
						} catch (JSONException e) {}
						command = (SocketCommand) commandClass.getConstructor(new Class[]{Event.class, JSONObject.class}).newInstance(new Object[]{event, data});
					} else {
						command = (SocketCommand) commandClass.getConstructor(new Class[]{Event.class}).newInstance(new Object[]{event});
					}
					d.setButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							new Thread(){
								@Override
								public void run() {
									showDialog(DIALOG_PERFORMING_COMMAND);
									bindService(
											new Intent(EventDetail.this, SocketService.class),
											new SingleCommandConnection(command),
											Context.BIND_AUTO_CREATE
									);
								}
							}.run();
						}
					});
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case DIALOG_ERROR:
				AlertDialog a = (AlertDialog) dialog;
				a.setMessage(getResources().getString(R.string.command_error) + ":\n\n" + args.getString("message"));
				break;
		}
		super.onPrepareDialog(id, dialog, args);
	}
	
	private boolean isParticipating() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(EventDetail.this);
    	JSONObject participating;
        try {
        	participating = new JSONObject(p.getString("participating", "{}"));
        } catch (JSONException e) {
        	participating = new JSONObject();
        }
    	String id = Integer.toString(event.getId());
    	try {
			return participating.has(id) && participating.getBoolean(id);
		} catch (JSONException e) {
			return false;
		} 
	}
	
	private void participate(boolean value) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(EventDetail.this);
        JSONObject participating;
        try {
        	participating = new JSONObject(p.getString("participating", "{}"));
        } catch (JSONException e) {
        	participating = new JSONObject();
        }
        String id = Integer.toString(event.getId());
    	try {
    		if (value) {
    			participating.put(id, true);
    		} else {
    			participating.remove(id);
    		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	p.edit().putString("participating", participating.toString()).commit();
	}
}
