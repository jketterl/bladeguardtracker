package net.djmacgyver.bgt.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.control.ControlService;
import net.djmacgyver.bgt.dialog.ProgressDialog;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.ParticipationStore;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.CommandExecutor;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketCommandCallback;
import net.djmacgyver.bgt.socket.command.PauseEventCommand;
import net.djmacgyver.bgt.socket.command.ShutdownEventCommand;
import net.djmacgyver.bgt.socket.command.StartEventCommand;
import net.djmacgyver.bgt.socket.command.UpdateEventCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

public class EventDetail extends ActionBarActivity {
	private Event event;

    private static final String DIALOG_CONFIRM = "dialog_confirm";
	private static final String DIALOG_PERFORMING_COMMAND = "dialog_performing";
	private static final String DIALOG_ERROR = "dialog_error";
	private static final String DIALOG_WEATHER_DECISION = "dialog_weather";
    private static final String DIALOG_GPS_WARNING = "dialog_gps_warning";

    private interface CommandProvider {
        public SocketCommand buildCommand();
    }
	
	abstract private class EventBoundOnClickListener implements View.OnClickListener, CommandProvider {
		private int message;
		
		private EventBoundOnClickListener(int message) {
			this.message = message;
		}
		
		@Override
		public void onClick(View v) {
            ConfirmDialog d = new ConfirmDialog(message, this);
            d.show(getSupportFragmentManager(), DIALOG_CONFIRM);
		}
	}

    private class ConfirmDialog extends DialogFragment {
        private final int message;
        private CommandProvider provider;

        public ConfirmDialog(int message, CommandProvider p) {
            this.message = message;
            provider = p;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

            b.setTitle(R.string.are_you_sure)
             .setMessage(message)
             .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface arg0, int arg1) {
                     ProgressDialog d = new ProgressDialog(R.string.command_executing);
                     d.show(getSupportFragmentManager(), DIALOG_PERFORMING_COMMAND);

                     SocketCommand c = provider.buildCommand();
                     c.addCallback(new SocketCommandCallback() {
                         @Override
                         public void run(SocketCommand command) {
                             DialogFragment d = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_PERFORMING_COMMAND);
                             if (d != null) d.dismiss();
                             if (!command.wasSuccessful()) {
                                 String message = "unknown error";
                                 try {
                                     message = command.getResponseData().getJSONObject(0).getString("message");
                                 } catch (JSONException ignored) {}
                                 ErrorDialog ed = new ErrorDialog(message);
                                 ed.show(getSupportFragmentManager(), DIALOG_ERROR);
                             }
                         }
                     });
                     new CommandExecutor(EventDetail.this).execute(c);
                 }
             })
             .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface arg0, int arg1) {
                     arg0.dismiss();
                 }
             });
            return b.create();
        }
    }

    private class WeatherDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            CharSequence[] items = {
                getResources().getString(R.string.no_cancelled),
                getResources().getString(R.string.yes_rolling)
            };
            b.setTitle(R.string.weather_decision)
             .setItems(items, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, final int which) {
                     ConfirmDialog d = new ConfirmDialog(R.string.update_weather, new CommandProvider() {
                         @Override
                         public SocketCommand buildCommand() {
                             try {
                                 JSONObject data = new JSONObject();
                                 data.put("weather", which);
                                 return new UpdateEventCommand(event, data);
                             } catch (JSONException ignored) {}
                             return null;
                         }
                     });
                     d.show(getSupportFragmentManager(), DIALOG_CONFIRM);
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
    }

    private class ErrorDialog extends DialogFragment {
        private String message;

        public ErrorDialog(String message) {
            this.message = message;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(R.string.servererror)
             .setMessage(getResources().getString(R.string.command_error) + ":\n\n" + message)
             .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface arg0, int arg1) {
                     arg0.dismiss();
                 }
             });
            return b.create();
        }
    }

    private class GPSWarningDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            View v = getActivity().getLayoutInflater().inflate(R.layout.gpswarning, null);
            Button lsb = (Button) v.findViewById(R.id.gotolocationsettings);
            lsb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    dismiss();
                }
            });
            b.setTitle(R.string.gpswarning)
             .setView(v);
            return b.create();
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
                ParticipationStore ps = new ParticipationStore(EventDetail.this);
                ps.participate(event, isChecked);

                if (isChecked) {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        DialogFragment d = new GPSWarningDialog();
                        d.show(getSupportFragmentManager(), DIALOG_GPS_WARNING);
                    }
                    Intent i = new Intent(EventDetail.this, ControlService.class);
                    i.putExtra("event", event);
                    startService(i);
                } else {
                    stopService(new Intent(EventDetail.this, ControlService.class));
                }
			}
		});
        
        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new EventBoundOnClickListener(R.string.activating_trackers){
            @Override
            public SocketCommand buildCommand() {
                return new StartEventCommand(event);
            }
        });
        
        Button pause = (Button) findViewById(R.id.pauseButton);
        pause.setOnClickListener(new EventBoundOnClickListener(R.string.deactivating_trackers){
            @Override
            public SocketCommand buildCommand() {
                return new PauseEventCommand(event);
            }
        });
        
        Button shutdown = (Button) findViewById(R.id.shutdownButton);
        shutdown.setOnClickListener(new EventBoundOnClickListener(R.string.terminating_event){
            @Override
            public SocketCommand buildCommand() {
                return new ShutdownEventCommand(event);
            }
        });

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
                WeatherDialog d = new WeatherDialog();
                d.show(getSupportFragmentManager(), DIALOG_WEATHER_DECISION);
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
        DateFormat df = DateFormat.getDateTimeInstance();
        start.setText(df.format(event.getStart()));
        
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

        ParticipationStore store = new ParticipationStore(this);
        CheckBox c = (CheckBox) findViewById(R.id.participateCheckbox);
		c.setChecked(store.doesParticipate(event));
		
		TextView mapView = (TextView) findViewById(R.id.mapView);
		mapView.setText(event.getMapName());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("event", event);
	}
}
