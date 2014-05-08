package net.djmacgyver.bgt.event.update;

import android.util.Log;
import android.util.SparseArray;

import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.socket.HttpSocketListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateParser implements HttpSocketListener {
    private interface UpdateReceiver {
        public List<Update> receiveUpdate(JSONArray data) throws JSONException, UpdateException;
    }

    private static Map<String, UpdateReceiver> receivers = new HashMap<String, UpdateReceiver>();
    static {
        receivers.put(Event.MAP, new UpdateReceiver() {
            @Override
            public List<Update> receiveUpdate(JSONArray data) throws JSONException, UpdateException {
                List<Update> result = new LinkedList<Update>();
                result.add(new EventMap(data.getJSONObject(0)));
                return result;
            }
        });

        receivers.put(Event.MOVEMENTS, new UpdateReceiver() {
            @Override
            public List<Update> receiveUpdate(JSONArray data) throws JSONException, UpdateException {
                List<Update> result = new LinkedList<Update>();
                for (int i = 0; i < data.length(); i++) {
                    result.add(new Movement(data.getJSONObject(i)));
                }
                return result;
            }
        });

        receivers.put(Event.QUIT, new UpdateReceiver() {
            @Override
            public List<Update> receiveUpdate(JSONArray data) throws JSONException, UpdateException {
                List<Update> result = new LinkedList<Update>();
                for (int i = 0; i < data.length(); i++) {
                    result.add(new Quit(data.getJSONObject(i)));
                }
                return result;
            }
        });

        receivers.put(Event.STATS, new UpdateReceiver() {
            @Override
            public List<Update> receiveUpdate(JSONArray data) throws JSONException, UpdateException {
                List<Update> result = new LinkedList<Update>();
                result.add(new Stats(data.getJSONObject(0)));
                return result;
            }
        });
    }

    private static final String TAG = "UpdateParser";

    @Override
    public void receiveUpdate(JSONObject data) {
        Log.d(TAG, "received update: " + data);
        for (Map.Entry<String, UpdateReceiver> entry : receivers.entrySet()) {
            String key = entry.getKey();
            if (data.has(key)) try {
                dispatchUpdates(entry.getValue().receiveUpdate(data.getJSONArray(key)));
            } catch (JSONException e) {
                Log.e(TAG, "error parsing update", e);
            } catch (UpdateException e) {
                Log.e(TAG, "error parsing update", e);
            }
        }
    }

    private void dispatchUpdates(List<Update> updates) {
        SparseArray<List<Update>> sorted = new SparseArray<List<Update>>();
        for (Update u : updates) {
            int eventId = u.getEventId();
            List<Update> ul = sorted.get(eventId);
            if (ul == null) {
                ul = new LinkedList<Update>();
                sorted.put(eventId, ul);
            }
            ul.add(u);
        }

        for (int i = 0; i < sorted.size(); i++) {
            int eventId = sorted.keyAt(i);
            Event e = events.get(eventId);
            e.receiveUpdate(sorted.get(eventId));
        }
    }

    // todo: this has some error potential since we have no mechanism to prevent event copies
    private final SparseArray<Event> events = new SparseArray<Event>();

    public void registerEvent(Event e) {
        Log.d(TAG, "registered event: " + e.getId());
        synchronized (events) { events.put(e.getId(), e); }
    }

    public void removeEvent(Event e) {
        synchronized (events) { events.remove(e.getId()); }
    }

    @Override
    public void receiveCommand(String command, JSONObject data) {}

    @Override
    public void receiveStateChange(int newState) {}
}
