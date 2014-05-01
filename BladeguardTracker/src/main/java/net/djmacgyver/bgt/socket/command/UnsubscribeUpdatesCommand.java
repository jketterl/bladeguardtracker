package net.djmacgyver.bgt.socket.command;

import net.djmacgyver.bgt.event.Event;

public class UnsubscribeUpdatesCommand extends SubscriptionCommand {
	
	public UnsubscribeUpdatesCommand(Event event, String category) {
		this(event, new String[]{category});
	}

	public UnsubscribeUpdatesCommand(Event event, String[] categories) {
		super("unsubscribeUpdates");
		setData(event, categories);
	}
}
