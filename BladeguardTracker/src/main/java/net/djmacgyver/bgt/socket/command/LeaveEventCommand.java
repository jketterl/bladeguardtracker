package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.SocketCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaveEventCommand extends SocketCommand {
    public LeaveEventCommand(Event event) {
        super("leaveEvent");
        try {
            data = new JSONObject();
            data.put("eventId", event.getId());
        } catch (JSONException ignored) {}
    }
}
