package net.djmacgyver.bgt.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import net.djmacgyver.bgt.R;

public class PasswordChangeFailedDialog extends DialogFragment{
    private final String message;

    public PasswordChangeFailedDialog(String message) {
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        String m = getResources().getString(R.string.passwordchange_failed);

        b.setMessage(m.concat("\n\n" + message))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return b.create();
    }
}
