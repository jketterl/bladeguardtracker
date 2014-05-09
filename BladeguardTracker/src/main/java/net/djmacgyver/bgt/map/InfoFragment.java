package net.djmacgyver.bgt.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.AbstractEventListener;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventListener;
import net.djmacgyver.bgt.event.update.Stats;
import net.djmacgyver.bgt.gps.AbstractGPSTrackingListener;
import net.djmacgyver.bgt.gps.GPSTrackingListener;
import net.djmacgyver.bgt.gps.GPSTrackingService;

import java.text.DecimalFormat;

public class InfoFragment extends Fragment {
    private static final String NOT_AVAILABLE = "n/a";
    private Event event;

    private TextView lengthView;
    private TextView speedView;
    private TextView timeView;
    private TextView timeToEndView;

    private double speed = -1;
    private double distanceToEnd = -1;

    private EventListener updater = new AbstractEventListener() {
        @Override
        public void onStats(final Stats stats) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (stats.hasLength()) {
                        lengthView.setText(new DecimalFormat("0.#").format(stats.getLength()) + " km");
                    } else {
                        lengthView.setText(NOT_AVAILABLE);
                    }
                    if (stats.hasSpeed()) {
                        speed = stats.getSpeed();
                        speedView.setText(new DecimalFormat("0.#").format(speed * 3.6) + " km/h");
                    } else {
                        speed = -1;
                        speedView.setText(NOT_AVAILABLE);
                    }
                    if (stats.hasLength() && stats.hasSpeed()) {
                        double cycleTime = (stats.getLength() * 1000 / stats.getSpeed()) / 60;
                        timeView.setText(new DecimalFormat("0").format(cycleTime) + " min");
                    } else {
                        timeView.setText(NOT_AVAILABLE);
                    }
                    updateDistaneToEnd();
                }
            });
        }

        @Override
        public Context getContext() {
            return getActivity();
        }
    };

    private String getDistanceToEndText() {
        if (speed < 0 || distanceToEnd < 0) return NOT_AVAILABLE;
        DecimalFormat df = new DecimalFormat("0");
        return df.format((distanceToEnd * 1000 / speed) / 60) + " min";
    }

    private void updateDistaneToEnd() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeToEndView.setText(getDistanceToEndText());
            }
        });
    }

    // GPSListener Service connection
    private ServiceConnection gpsServiceConnection = new ServiceConnection() {
        GPSTrackingService service;

        GPSTrackingListener listener = new AbstractGPSTrackingListener() {
            @Override
            public void onDistanceToEndLost() {
                distanceToEnd = -1;
                updateDistaneToEnd();
            }

            @Override
            public void onDistanceToEnd(double distance) {
                distanceToEnd = distance;
                updateDistaneToEnd();
            }
        };

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service.removeListener(listener);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((GPSTrackingService.LocalBinder) binder).getService();
            service.addListener(listener);
        }
    };


    @Override
    public void onDestroy() {
        gpsServiceConnection.onServiceDisconnected(null);
        getActivity().unbindService(gpsServiceConnection);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        event = getArguments().getParcelable("event");
        Log.d("InfoFragment", "event is: " + event);

        View v = inflater.inflate(R.layout.infofragment, container, false);

        lengthView = (TextView) v.findViewById(R.id.bladeNightLength);
        speedView = (TextView) v.findViewById(R.id.bladeNightSpeed);
        timeView = (TextView) v.findViewById(R.id.bladeNightCycleTime);
        timeToEndView = (TextView) v.findViewById(R.id.timeToEnd);

        Intent gi = new Intent(getActivity(), GPSTrackingService.class);
        getActivity().bindService(gi, gpsServiceConnection, Context.BIND_AUTO_CREATE);

        return v;
    }

    @Override
    public void onPause() {
        event.unsubscribeUpdates(updater);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        event.subscribeUpdates(updater, Event.STATS);
    }
}
