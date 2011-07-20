package net.djmacgyver.bgt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        LocationManager m = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		        GPSListener l = GPSListener.getSharedInstance();
		        l.setContext(getApplicationContext());
				if (((ToggleButton) v).isChecked()) {
					m.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, l);
				} else {
					m.removeUpdates(l);
					l.disable();
				}
			}
		});
        
        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Map.class);
				startActivity(i);
			}
		});
    }
}