package net.djmacgyver.bgt.event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.djmacgyver.bgt.socket.ServerList;
import net.djmacgyver.bgt.socket.SocketCommand;
import net.djmacgyver.bgt.socket.command.GetEventsCommand;

import org.json.JSONException;

public class EventList extends ServerList {
    private static final int ITEM_VIEW_TYPE_SMALL = 0;
    private static final int ITEM_VIEW_TYPE_LARGE = 1;

	public EventList(Context context) {
		super(context);
	}

    public EventList(Context context, boolean autoload) {
        super(context, autoload);
    }
	
	@Override
	protected Class <? extends SocketCommand> getServerCommand() {
		return GetEventsCommand.class;
	}

	@Override
	public int getItemViewType(int index) {
        if (index == 0) return ITEM_VIEW_TYPE_LARGE;
		return ITEM_VIEW_TYPE_SMALL;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
        Event event = getEvent(index);
        SmallEventView eventView;
        // BigEventView inherits from SmallEventView, so casting to SmallEventView should always
        // work and produce the expected result.
        if (view != null) {
            eventView = ((SmallEventView) view);
        } else {
            if (index == 0) {
                eventView = new BigEventView(getContext());
            } else {
                eventView = new SmallEventView(getContext());
            }
        }
        eventView.setEvent(event);
        return eventView;
	}

    private Event getEvent(int index) {
        try {
            return new Event(getData().getJSONObject(index));
        } catch (JSONException e) {
            return null;
        }
    }

    public Event findById(int id) throws EventNotFoundException {
        for (int i = 0; i < getCount(); i++) {
            try {
                Event e = new Event(getData().getJSONObject(i));
                if (e.getId() == id) return e;
            } catch (JSONException ignored) {}
        }
        throw new EventNotFoundException();
    }

	@Override
	public int getViewTypeCount() {
		return 2;
	}
}
