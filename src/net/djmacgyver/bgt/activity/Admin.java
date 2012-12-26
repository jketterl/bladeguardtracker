package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.SocketService;
import net.djmacgyver.bgt.socket.command.BridgeCommand;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Admin extends Activity {
	public static final int DIALOG_PERFORMING_COMMAND = 1;
	public static final int DIALOG_ERROR = 2;

	private Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle b = new Bundle();
			b.putString("message", (String) msg.obj);
			showDialog(DIALOG_ERROR, b);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.admin);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.admin_commands);
        
        Button disableButton = (Button) findViewById(R.id.disableButton);
        disableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireCommand(new BridgeCommand(false));
			}
		});
        
        Button enableButton = (Button) findViewById(R.id.enableButton);
        enableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fireCommand(new BridgeCommand(true));
			}
		});
	}
	
	protected void fireCommand(final SocketCommand command) {
		showDialog(DIALOG_PERFORMING_COMMAND);
		ServiceConnection conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				SocketService s = ((SocketService.LocalBinder) service).getService();
				command.addCallback(new Runnable() {
					@Override
					public void run() {
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
		};
		
		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		switch (id) {
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
		}
		return super.onCreateDialog(id);
	}

	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
			case DIALOG_ERROR:
				AlertDialog a = (AlertDialog) dialog;
				a.setMessage(getResources().getString(R.string.command_error) + ":\n\n" + args.getString("message"));
				break;
		}
	}
}