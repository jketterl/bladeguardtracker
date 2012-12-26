package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.socket.SocketCommand;

public class BridgeCommand extends SocketCommand {

	public BridgeCommand(boolean enable) {
		super((enable ? "enable" : "disable") + "Bridges");
	}

}
