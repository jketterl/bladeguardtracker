package net.djmacgyver.bgt.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import net.djmacgyver.bgt.user.Team;

import java.util.List;

public class BladeMapFragment extends SupportMapFragment {
    @SuppressWarnings("unused")
    private static final String TAG = "BladeMapFragment";

    private Event event;

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
        private EventMap currentMap;
        private Polyline currentMapLine;

        @Override
        public Context getContext() {
            return getActivity();
        }

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

        private SparseArray<Marker> markers = new SparseArray<Marker>();

        @Override
        public void onMovement(final List<Movement> movements) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GoogleMap map = getMap();

                    for (Movement m : movements) {
                        int userId = m.getUserId();
                        Marker marker = markers.get(userId);
                        if (marker != null) {
                            marker.setPosition(m.getNewLocation());
                        } else {
                            MarkerOptions o = new MarkerOptions();
                            o.position(m.getNewLocation())
                                    .anchor(.5f, .5f)
                                    .icon(getIcon(m.getTeam(getContext())));
                            markers.put(userId, map.addMarker(o));
                        }
                    }
                }
            });
        }

        private BitmapDescriptor getIcon(Team team) {
            Drawable d = team.getPin();
            Log.d(TAG, "width: " + d.getIntrinsicWidth() + "; height: " + d.getIntrinsicHeight());
            Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.draw(c);
            return BitmapDescriptorFactory.fromBitmap(b);
        }

        @Override
        public void onQuit(final List<Quit> quits) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Quit q : quits) {
                        int userId = q.getUserId();
                        Marker marker = markers.get(userId);
                        if (marker == null) continue;
                        marker.remove();
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
                            localLine.remove();
                        }
                    });
                }
                return;
            }
            final List<LatLng> points = currentMap.getPoints(currentStats);

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

        @Override
        public void onReset() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentMapLine != null) {
                        currentMapLine.remove();
                        currentMapLine = null;
                    }
                    for (int i = 0; i < markers.size(); i++) {
                        Marker m = markers.valueAt(i);
                        m.remove();
                    }
                    markers.clear();
                    if (currentTrackLine != null) {
                        currentTrackLine.remove();
                        currentTrackLine = null;
                    }
                    currentStats = null;
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        event = getArguments().getParcelable("event");

        View v = super.onCreateView(inflater, container, savedInstanceState);

        Intent gi = new Intent(getActivity(), GPSTrackingService.class);
        getActivity().bindService(gi, gpsServiceConnection, Context.BIND_AUTO_CREATE);

        return v;
    }

    @Override
    public void onDestroyView() {
        gpsServiceConnection.onServiceDisconnected(null);
        getActivity().unbindService(gpsServiceConnection);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        event.subscribeUpdates(eventListener, Event.MOVEMENTS, Event.MAP, Event.STATS, Event.QUIT);
    }

    @Override
    public void onPause() {
        event.unsubscribeUpdates(eventListener);
        super.onPause();
    }
}
