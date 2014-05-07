package net.djmacgyver.bgt.event.update;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.djmacgyver.bgt.user.Team;

import org.json.JSONException;
import org.json.JSONObject;

public class Movement extends Update {
    private LatLng newLocation;
    private int userId;
    private String team;

    public Movement(JSONObject data) {
        Log.d("Movement", data.toString());
        try {
            JSONObject location = data.getJSONObject("location");
            newLocation = new LatLng(location.getDouble("lat"), location.getDouble("lon"));

            JSONObject user = data.getJSONObject("user");
            userId = user.getInt("id");
            team = user.getString("team");
        } catch (JSONException ignored) {}
    }

    public LatLng getNewLocation() {
        return newLocation;
    }

    public int getUserId() {
        return userId;
    }

    public Team getTeam(Context c) {
        return Team.getTeam(team, c);
    }
}
