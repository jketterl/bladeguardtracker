package net.djmacgyver.bgt.event.update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Stats extends Update {
    private static final String KEY_SPEED = "bladeNightSpeed";
    private static final String KEY_LENGTH = "bladeNigthLength";
    private static final String KEY_BETWEEN = "between";

    private double speed = -1;
    private double length = -1;
    private int start = -1;
    private int end = -1;

    public Stats(JSONArray data) {
        try {
            JSONObject so = data.getJSONObject(0);
            if (so.has(KEY_SPEED)) speed = so.getDouble(KEY_SPEED);
            if (so.has(KEY_LENGTH)) length = so.getDouble(KEY_LENGTH);
            if (so.has(KEY_BETWEEN)) {
                JSONArray between = so.getJSONArray(KEY_BETWEEN);
                start = between.getInt(0);
                end = between.getInt(1);
            }
        } catch (JSONException ignored) {}
    }

    public boolean hasSpeed() {
        return speed >= 0;
    }

    public double getSpeed() {
        return speed;
    }

    public  boolean hasLength() {
        return length >= 0;
    }

    public double getLength() {
        return length;
    }

    public boolean hasStart() {
        return start >= 0;
    }

    public int getStart() {
        return start;
    }

    public boolean hasEnd() {
        return end >= 0;
    }

    public int getEnd() {
        return end;
    }
}

/*
[{"tracked":1,"users":12,"bladeNightSpeed":2.6488187362417035,"eventId":16,"speeded":1}]
[{"bladeNightLength":0.20037411144135248,"tracked":3,"users":13,"bladeNightSpeed":3.013643295470819,"eventId":16,"speeded":3,"between":[0,2]}]
 */
