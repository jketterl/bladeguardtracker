package net.djmacgyver.bgt.team;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.djmacgyver.bgt.R;

public class TeamSelectionDialog extends DialogFragment {

    private OnTeamSelectedListener target;

    public interface OnTeamSelectedListener {
        public void onTeamSelected(int id);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        View v = getActivity().getLayoutInflater().inflate(R.layout.fullscreenlist, null);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                dismiss();
                target.onTeamSelected((int) id);
            }
        });

        listView.setAdapter(new TeamList(getActivity(), true));

        b.setTitle(R.string.teamselection)
                .setView(v);

        return b.create();
    }

    @Override
    public void onAttach(Activity activity) {
        target = (OnTeamSelectedListener) activity;
        super.onAttach(activity);
    }
}
