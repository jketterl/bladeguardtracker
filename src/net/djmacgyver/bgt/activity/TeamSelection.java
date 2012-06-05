package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.widget.TextView;

public class TeamSelection extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
	
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.teamselection);
        addPreferencesFromResource(R.xml.teamselection);
	}
}
