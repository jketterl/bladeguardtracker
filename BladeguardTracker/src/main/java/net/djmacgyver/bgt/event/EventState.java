package net.djmacgyver.bgt.event;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventState {
    protected static SparseArray<EventState> states = new SparseArray<EventState>();

    protected static EventState getState(int eventId) {
        EventState es = states.get(eventId);
        if (es == null) {
            es = new EventState();
            states.put(eventId, es);
        }
        return es;
    }

    protected Map<String, List<EventListener>> listeners = new HashMap<String, List<EventListener>>();
}
