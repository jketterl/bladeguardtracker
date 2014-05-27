package net.djmacgyver.bgt.event;

import android.content.Context;
import android.widget.TextView;

import net.djmacgyver.bgt.R;

import java.text.DateFormat;

public class BigEventView extends SmallEventView {
    TextView startTime;

    public BigEventView(Context context) {
        super(context);

        startTime = (TextView) root.findViewById(R.id.startTime);
    }

    @Override
    protected int getLayout() {
        return R.layout.eventlistitem_big;
    }

    @Override
    public void setEvent(Event event) {
        super.setEvent(event);
        startTime.setText(getStartTimeText(event));
    }

    @Override
    protected String getMapText(Event event) {
        return event.getMapName();
    }

    public String getStartTimeText(Event event) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(event.getStart());
    }
}
