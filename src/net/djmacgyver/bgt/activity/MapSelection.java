package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class MapSelection extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mapselection);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.map_selection);
	}
	
}
