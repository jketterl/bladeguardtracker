package net.djmacgyver.bgt.event.update;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class EventMap extends Update {
    private List<LatLng> points;
    private LatLngBounds bounds;

    public EventMap(JSONObject data) throws UpdateException {
        super(data);

        points = new LinkedList<LatLng>();
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        try {
            JSONArray p = data.getJSONArray("points");

            for (int i = 0; i < p.length(); i++) {
                JSONObject o = p.getJSONObject(i);
                LatLng point = new LatLng(o.getDouble("lat"), o.getDouble("lon"));
                b.include(point);
                points.add(point);
            }

            bounds = b.build();
        } catch (JSONException ignored) {}
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public List<LatLng> getPoints(int start, int end) {
        if (start <= end) return points.subList(start, end);

        List<LatLng> a = points.subList(start, points.size());
        List<LatLng> b = points.subList(0, end);
        List<LatLng> result = new LinkedList<LatLng>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public List<LatLng> getPoints(Stats s) {
        return getPoints(s.getStart(), s.getEnd());
    }

    public LatLngBounds getBounds() {
        return bounds;
    }
}
