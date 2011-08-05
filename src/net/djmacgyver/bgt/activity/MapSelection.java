package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.map.MapList;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class MapSelection extends ListActivity {
	private MapList maps;
	
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
		System.out.println("clicked on map: " + id);
	}
}
