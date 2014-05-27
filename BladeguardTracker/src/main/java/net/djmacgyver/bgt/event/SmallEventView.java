package net.djmacgyver.bgt.event;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.djmacgyver.bgt.R;

import java.text.DateFormat;

public class SmallEventView extends LinearLayout {
    private TextView text;
    private TextView map;
    private ImageView weatherIcon;
    private LinearLayout weatherLayout;

    protected View root;

    protected int layout = R.layout.eventlistitem;

    public SmallEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(getLayout(), this);

        text = (TextView) root.findViewById(R.id.title);
        map = (TextView) root.findViewById(R.id.map);
        weatherLayout = (LinearLayout) root.findViewById(R.id.weatherLayout);
        weatherIcon = (ImageView) root.findViewById(R.id.weatherIcon);
    }

    public SmallEventView(Context context) {
        this(context, null);
    }

    protected int getLayout() {
        return R.layout.eventlistitem;
    }

    public void setEvent(Event event) {
        text.setText(getTitleText(event));
        map.setText(getMapText(event));
        int weatherDrawable = getWeatherDrawable(event);
        if (weatherDrawable > 0) {
            weatherLayout.setVisibility(View.VISIBLE);
            weatherIcon.setImageResource(weatherDrawable);
        } else {
            weatherLayout.setVisibility(View.GONE);
        }
    }

    protected String getMapText(Event event) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(event.getStart()) + " " + event.getMapName();
    }

    protected String getTitleText(Event event) {
        return event.getTitle();
    }

    protected int getWeatherDrawable(Event event) {
        if (!event.hasWeatherDecision()) return -1;
        if (event.getWeatherDecision()) return R.drawable.ampel_gruen;
        return R.drawable.ampel_rot;
    }
}
