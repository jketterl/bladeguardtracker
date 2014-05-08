package net.djmacgyver.bgt.event.update;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class Update {
    private int eventId;

    public Update(JSONObject data) throws UpdateException {
        try {
            eventId = data.getInt("eventId");
        } catch (JSONException e) {
            throw new UpdateException("could not get event id", e);
        }
    }

    public int getEventId() {
        return eventId;
    }
}
