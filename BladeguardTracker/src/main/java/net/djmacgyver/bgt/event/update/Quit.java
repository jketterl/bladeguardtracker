package net.djmacgyver.bgt.event.update;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class Quit extends Update {
    private int userId;

    public Quit(JSONObject data) {
        try {
            JSONObject user = data.getJSONObject("user");
            userId = user.getInt("id");
        } catch (JSONException ignored) {}
    }

    public int getUserId() {
        return userId;
    }
}
