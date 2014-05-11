package net.djmacgyver.bgt.socket;

import org.json.JSONObject;

abstract public class AbstractHttpSocketListener implements HttpSocketListener {
    @Override
    public void receiveStateChange(int newState) {}

    @Override
    public void receiveCommand(String command, JSONObject data) {}

    @Override
    public void receiveUpdate(JSONObject data) {}
}
