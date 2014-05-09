package net.djmacgyver.bgt.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.event.AbstractEventListener;
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.event.EventListener;
import net.djmacgyver.bgt.event.update.Stats;

import java.text.DecimalFormat;

public class InfoFragment extends Fragment {
    private static final String NOT_AVAILABLE = "n/a";
    private Event event;

    private TextView length;
    private TextView speed;
    private TextView time;

    private EventListener updater = new AbstractEventListener() {
        @Override
        public void onStats(final Stats stats) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (stats.hasLength()) {
                        length.setText(new DecimalFormat("0.#").format(stats.getLength()) + " km");
                    } else {
                        length.setText(NOT_AVAILABLE);
                    }
                    if (stats.hasSpeed()) {
                        speed.setText(new DecimalFormat("0.#").format(stats.getSpeed() * 3.6) + " km/h");
                    } else {
                        speed.setText(NOT_AVAILABLE);
                    }
                    if (stats.hasLength() && stats.hasSpeed()) {
                        double cycleTime = (stats.getLength() * 1000 / stats.getSpeed()) / 60;
                        time.setText(new DecimalFormat("0").format(cycleTime) + " min");
                    } else {
                        time.setText(NOT_AVAILABLE);
                    }
                }
            });
        }

        @Override
        public Context getContext() {
            return getActivity();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        event = getArguments().getParcelable("event");
        Log.d("InfoFragment", "event is: " + event);

        View v = inflater.inflate(R.layout.infofragment, container, false);

        length = (TextView) v.findViewById(R.id.bladeNightLength);
        speed = (TextView) v.findViewById(R.id.bladeNightSpeed);
        time = (TextView) v.findViewById(R.id.bladeNightCycleTime);

        event.subscribeUpdates(updater, Event.STATS);

        return v;
    }

    @Override
    public void onDestroyView() {
        event.unsubscribeUpdates(updater);
        super.onDestroyView();
    }
}
