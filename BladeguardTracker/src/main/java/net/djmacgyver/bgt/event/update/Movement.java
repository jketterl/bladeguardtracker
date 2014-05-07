package net.djmacgyver.bgt.event.update;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class Movement extends Update {
    private LatLng newLocation;
    private int userId;

    public Movement(JSONObject data) {
        try {
            JSONObject location = data.getJSONObject("location");
            newLocation = new LatLng(location.getDouble("lat"), location.getDouble("lon"));

            JSONObject user = data.getJSONObject("user");
            userId = user.getInt("id");
        } catch (JSONException ignored) {}
    }

    public LatLng getNewLocation() {
        return newLocation;
    }

    public int getUserId() {
        return userId;
    }
}
