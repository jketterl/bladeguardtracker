package net.djmacgyver.bgt.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import net.djmacgyver.bgt.R;

abstract public class PasswordChangeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setView(getActivity().getLayoutInflater().inflate(R.layout.passworddialog, null))
                .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText passView = (EditText) d.findViewById(R.id.password);
                        EditText confirmView = (EditText) d.findViewById(R.id.password_confirm);
                        final String pass = passView.getText().toString();
                        String confirm = confirmView.getText().toString();

                        if (!pass.equals(confirm)) {
                            onPasswordChangeFailed(R.string.password_mismatch);
                            return;
                        }

                        if (pass.equals("")) {
                            onPasswordChangeFailed(R.string.password_must_not_be_empty);
                            return;
                        }

                        changePassword(pass);
                    }
                })
                .setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return b.create();
    }

    abstract protected void onPasswordChangeFailed(int message);
    abstract protected void changePassword(String password);
}
