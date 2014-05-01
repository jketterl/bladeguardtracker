package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class GCMAccountNotification extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.gcmaccountnotification);

		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(GCMAccountNotification.this);
		CheckBox dontShowAgain = (CheckBox) findViewById(R.id.dontShowAgain);
        dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				p.edit().putBoolean("showGCMNotification", !isChecked).commit();
			}
		});
        dontShowAgain.setChecked(!p.getBoolean("showGCMNotification", true));
        
        Button settings = (Button) findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
				startActivity(i);
			}
		});
	}

}