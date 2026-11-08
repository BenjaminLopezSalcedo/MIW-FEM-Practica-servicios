package es.upm.miw.practicaservicios.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import es.upm.miw.practicaservicios.R;

public class DeleteUserAlertDialog extends DialogFragment {

    public interface OnDeleteUserConfirmedListener {
        void onDeleteUserConfirmed();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final OnDeleteUserConfirmedListener listener =
                (OnDeleteUserConfirmedListener) requireActivity();

        return new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_delete_user_title)
                .setMessage(R.string.dialog_delete_user_message)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> listener.onDeleteUserConfirmed())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}