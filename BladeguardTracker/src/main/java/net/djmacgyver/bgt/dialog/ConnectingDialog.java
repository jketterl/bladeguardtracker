package net.djmacgyver.bgt.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.djmacgyver.bgt.R;

public class ConnectingDialog extends DialogFragment {
    public ConnectingDialog() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.connect_progress);
        View v = inflater.inflate(R.layout.connectingdialog, container, false);
        TextView t = (TextView) v.findViewById(R.id.message);
        t.setText(R.string.pleasewait);
        return v;
    }
}
