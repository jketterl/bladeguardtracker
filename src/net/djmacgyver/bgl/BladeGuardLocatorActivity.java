package net.djmacgyver.bgl;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class BladeGuardLocatorActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final LocationManager m = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final LocationListener l = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				Toast.makeText(getApplicationContext(), "Location changed: " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
			}
		};
        
        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					m.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, l);
				} else {
					m.removeUpdates(l);
				}
			}
		});
        
    }
}