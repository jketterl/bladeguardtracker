package net.djmacgyver.bgt.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.djmacgyver.bgt.R;

public class RouteSelectionDialog extends DialogFragment {

    private OnRouteSelectedListener target;

    public interface OnRouteSelectedListener {
        public void onRouteSelected(int id);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        View v = getActivity().getLayoutInflater().inflate(R.layout.fullscreenlist, null);

        MapList list = new MapList(getActivity(), true);
        ListView routeList = (ListView) v.findViewById(R.id.list);
        routeList.setAdapter(list);
        routeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                target.onRouteSelected((int) id);
                dismiss();
            }
        });

        b.setTitle(R.string.map_selection)
                .setView(v);

        return b.create();
    }

    @Override
    public void onAttach(Activity activity) {
        target = (OnRouteSelectedListener) activity;
        super.onAttach(activity);
    }
}
