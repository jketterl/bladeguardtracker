package net.djmacgyver.bgt.activity;

import java.io.IOException;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.http.HttpClient;
import net.djmacgyver.bgt.map.MapList;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class MapSelection extends ListActivity {
	private final static int DIALOG_CONFIRM = 0;
	private final static int DIALOG_PROGRESS = 1;
	private MapList maps;
	private long selected;
	
	private MapList getMaps() {
		if (maps == null) {
			maps = new MapList(getApplicationContext());
		}
		return maps;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mapselection);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.map_selection);
        
        setListAdapter(getMaps());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		selected = id;
		showDialog(DIALOG_CONFIRM);
	}
	
	private void switchMap() {
		showDialog(DIALOG_PROGRESS);
		
		final Handler h = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				dismissDialog(DIALOG_PROGRESS);
				finish();
			}
		};
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpGet req = new HttpGet(getResources().getString(R.string.base_url) + "setmap?id=" + selected);
				HttpClient c = new HttpClient(getApplicationContext());
				try {
					HttpEntity e = c.execute(req).getEntity();
					e.consumeContent();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				h.sendEmptyMessage(0);
			}
		}).start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONFIRM:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.are_you_sure);
				builder.setMessage(R.string.map_will_be_reset);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switchMap();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return builder.create();
			case DIALOG_PROGRESS:
				Dialog d = new ProgressDialog(this);
				d.setTitle(R.string.switching_map);
				return d;
		}
		return super.onCreateDialog(id);
	}
}
