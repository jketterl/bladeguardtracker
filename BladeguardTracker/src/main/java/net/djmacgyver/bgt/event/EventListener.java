package net.djmacgyver.bgt.event;

import android.content.Context;

import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.event.update.Movement;
import net.djmacgyver.bgt.event.update.Quit;
import net.djmacgyver.bgt.event.update.Stats;

import java.util.List;

public interface EventListener {
    public Context getContext();

    public void onMap(EventMap map);

    public void onMovement(List<Movement> movements);

    public void onQuit(List<Quit> quits);

    public void onStats(Stats stats);
}
