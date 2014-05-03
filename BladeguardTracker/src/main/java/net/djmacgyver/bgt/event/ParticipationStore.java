package net.djmacgyver.bgt.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.djmacgyver.bgt.socket.CommandExecutor;
import net.djmacgyver.bgt.socket.command.LeaveEventCommand;
import net.djmacgyver.bgt.socket.command.ParticipateEventCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class ParticipationStore {
    private final static String STORAGE_KEY = "participating";

    private Context context;

    public ParticipationStore(Context context) {
        this.context = context;
    }

    private JSONObject getParticipations() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return new JSONObject(p.getString(STORAGE_KEY, "{}"));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private void storeParticipations(JSONObject participations) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        p.edit().putString(STORAGE_KEY, participations.toString()).commit();
    }

    public void participate(Event event, boolean value) {
        String id = Integer.toString(event.getId());
        if (doesParticipate(event) == value) return;
        JSONObject participating = getParticipations();
        try {
            CommandExecutor e = new CommandExecutor(context);
            if (value) {
                participating.put(id, true);
                e.execute(new ParticipateEventCommand(event));
            } else {
                participating.remove(id);
                e.execute(new LeaveEventCommand(event));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        storeParticipations(participating);
    }

    public boolean doesParticipate(Event event) {
        JSONObject participations = getParticipations();
        try {
            return participations.getBoolean(Integer.toString(event.getId()));
        } catch (JSONException e) {
            return false;
        }
    }
}
