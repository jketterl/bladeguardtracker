package net.djmacgyver.bgt.event.update;

import org.json.JSONException;
import org.json.JSONObject;

public class Quit extends Update {
    private int userId;

    public Quit(JSONObject data) throws UpdateException {
        super(data);
        try {
            JSONObject user = data.getJSONObject("user");
            userId = user.getInt("id");
        } catch (JSONException ignored) {}
    }

    public int getUserId() {
        return userId;
    }
}
