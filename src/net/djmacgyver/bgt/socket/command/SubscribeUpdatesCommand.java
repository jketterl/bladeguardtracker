package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;

public class SubscribeUpdatesCommand extends SubscriptionCommand {

	public SubscribeUpdatesCommand(Event event, String category) {
		this(event, new String[]{category});
	}

	public SubscribeUpdatesCommand(Event event, String[] categories) {
		super("subscribeUpdates");
		setData(event, categories);
	}
}
