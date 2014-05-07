package net.djmacgyver.bgt.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.AbstractEventListener;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventListener;
import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.event.update.Movement;
import net.djmacgyver.bgt.event.update.Quit;
import net.djmacgyver.bgt.event.update.Stats;
import net.djmacgyver.bgt.gps.AbstractGPSTrackingListener;
import net.djmacgyver.bgt.gps.GPSTrackingListener;
import net.djmacgyver.bgt.gps.GPSTrackingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BladeMapFragment extends SupportMapFragment {
    private static final String TAG = "BladeMapFragment";

    private final Event event;

    public BladeMapFragment(Event event) {
        this.event = event;
    }

    // GPSListener Service connection
    private ServiceConnection gpsServiceConnection = new ServiceConnection() {
        GPSTrackingService service;

        GPSTrackingListener listener = new AbstractGPSTrackingListener() {
            @Override
            public void trackingEnabled() {
                getMap().setMyLocationEnabled(true);
            }

            @Override
            public void trackingDisabled() {
                getMap().setMyLocationEnabled(false);
            }
        };

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service.removeListener(listener);
            getMap().setMyLocationEnabled(false);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((GPSTrackingService.LocalBinder) binder).getService();
            service.addListener(listener);
            if (!service.isEnabled()) return;
            getMap().setMyLocationEnabled(true);
        }
    };

    private EventListener eventListener = new AbstractEventListener() {
        @Override
        public Context getContext() {
            return getActivity();
        }

        private EventMap currentMap;
        private Polyline currentMapLine;

        @Override
        public void onMap(final EventMap map) {
            currentMap = map;
            final PolylineOptions o = new PolylineOptions();
            int colorBase = Color.BLUE;
            o.addAll(map.getPoints())
                    .color(Color.argb(64, Color.red(colorBase), Color.green(colorBase), Color.blue(colorBase)))
                    .width(getActivity().getResources().getDimensionPixelSize(R.dimen.mapStrokeWidth))
                    .zIndex(2);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GoogleMap gmap = getMap();
                    if (currentMapLine != null) currentMapLine.remove();
                    currentMapLine = gmap.addPolyline(o);
                    gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(map.getBounds(), 10));
                }
            });

            buildTrack();
        }

        private Map<Integer, Marker> markers = new HashMap<Integer, Marker>();

        @Override
        public void onMovement(final List<Movement> movements) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GoogleMap map = getMap();

                    for (Movement m : movements) {
                        int userId = m.getUserId();
                        if (markers.containsKey(userId)) {
                            markers.get(userId).setPosition(m.getNewLocation());
                        } else {
                            MarkerOptions o = new MarkerOptions();
                            o.position(m.getNewLocation())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_common));
                            markers.put(userId, map.addMarker(o));
                        }
                    }
                }
            });
        }

        @Override
        public void onQuit(final List<Quit> quits) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GoogleMap map = getMap();

                    for (Quit q : quits) {
                        int userId = q.getUserId();
                        if (!markers.containsKey(userId)) continue;
                        markers.get(userId).remove();
                        markers.remove(userId);
                    }
                }
            });
        }

        private Stats currentStats;

        @Override
        public void onStats(Stats stats) {
            currentStats = stats;
            buildTrack();
        }

        private Polyline currentTrackLine;

        private void buildTrack() {
            if (currentMap == null || currentStats == null || !currentStats.hasStart() || !currentStats.hasEnd()) {
                if (currentTrackLine != null) {
                    final Polyline localLine = currentTrackLine;
                    currentTrackLine = null;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            localLine.remove();;
                        }
                    });
                }
                return;
            }
            final List<LatLng> points = currentMap.getPoints(currentStats);

            Log.d(TAG, "now between " + currentStats.getStart() + " and " + currentStats.getEnd());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GoogleMap map = getMap();
                    if (currentTrackLine == null) {
                        PolylineOptions o = new PolylineOptions();
                        o.addAll(points)
                                .color(Color.rgb(255, 192, 0))
                                .width(getResources().getDimension(R.dimen.trackStrokeWidth))
                                .zIndex(1);
                        currentTrackLine = map.addPolyline(o);
                    } else {
                        currentTrackLine.setPoints(points);
                    }
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        Intent gi = new Intent(getActivity(), GPSTrackingService.class);
        getActivity().bindService(gi, gpsServiceConnection, Context.BIND_AUTO_CREATE);

        event.subscribeUpdates(eventListener, Event.MOVEMENTS, Event.MAP, Event.STATS, Event.QUIT);

        return v;
    }

    @Override
    public void onDestroyView() {
        event.unsubscribeUpdates(eventListener);
        gpsServiceConnection.onServiceDisconnected(null);
        getActivity().unbindService(gpsServiceConnection);
        super.onDestroyView();
    }
}
