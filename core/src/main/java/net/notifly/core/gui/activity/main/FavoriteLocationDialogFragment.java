package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import net.notifly.core.R;

public class FavoriteLocationDialogFragment extends DialogFragment {
    FavoriteLocationDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.save_location)
                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_fav_location, null))
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText title = (EditText) getDialog().findViewById(R.id.locationTitleEditText);
                        listener.onTitleSelected(title.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                }).create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (FavoriteLocationDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FavoriteLocationDialogListener");
        }
    }

    public interface FavoriteLocationDialogListener {
        public void onTitleSelected(String title);
    }
}
