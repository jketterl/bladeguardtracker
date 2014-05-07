package net.djmacgyver.bgt.event;

import net.djmacgyver.bgt.event.update.EventMap;
import net.djmacgyver.bgt.event.update.Movement;
import net.djmacgyver.bgt.event.update.Quit;
import net.djmacgyver.bgt.event.update.Stats;

import java.util.List;

abstract public class AbstractEventListener implements EventListener {
    @Override
    public void onMap(EventMap map){}

    @Override
    public void onMovement(List<Movement> movements) {}

    @Override
    public void onQuit(List<Quit> quits) {}

    @Override
    public void onStats(Stats stats) {}
}
