package net.djmacgyver.bgt.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.djmacgyver.bgt.R;

public class ProgressDialog extends DialogFragment {
    private int message;

    public ProgressDialog() {
        this(R.string.connect_progress);
    }

    public ProgressDialog(int message) {
        this.message = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(message);
        View v = inflater.inflate(R.layout.progressdialog, container, false);
        TextView t = (TextView) v.findViewById(R.id.message);
        t.setText(R.string.pleasewait);
        return v;
    }
}
