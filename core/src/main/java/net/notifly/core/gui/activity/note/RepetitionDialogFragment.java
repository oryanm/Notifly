package net.notifly.core.gui.activity.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;

import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.entity.Repetition;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

public class RepetitionDialogFragment extends DialogFragment implements CalendarDatePickerDialog.OnDateSetListener {
    public static final String FRAGMENT_TAG = "note_repetition";
    public static final String NOTE_KEY = "net.notifly.core.repetition.note";

    RepetitionDialogListener listener;

    Note note;

    Spinner typeSpinner;
    ArrayAdapter<Repetition.TYPE> adapter;
    EditText startDateEditText;
    EditText intervalEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        note = getArguments().getParcelable(NOTE_KEY);

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_repeat_note, null);
        intervalEditText = (EditText) view.findViewById(R.id.intervalEditText);
        setAdapter(view);
        setStartDate(view);
        setNote();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveRepetition();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                }).create();
    }

    private void setNote() {
        if (note.repeats()) {
            Repetition repetition = note.getRepetition();
            intervalEditText.setText(String.valueOf(repetition.getInterval()));
            typeSpinner.setSelection(repetition.getType().ordinal());
            startDateEditText.setText(repetition.getStart().toString(DateTimeFormat.mediumDate()));
        } else {
            startDateEditText.setText(note.getTime().toLocalDate().toString(DateTimeFormat.mediumDate()));
        }
    }

    private void setStartDate(View view) {
        startDateEditText = (EditText) view.findViewById(R.id.startDateEditText);
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDatePicker();
            }
        });
    }

    private void setAdapter(View view) {
        typeSpinner = (Spinner) view.findViewById(R.id.typeSpinner);
        adapter = new ArrayAdapter<Repetition.TYPE>(getActivity(),
                android.R.layout.simple_spinner_item, Repetition.TYPE.values());
        typeSpinner.setAdapter(adapter);
    }

    private void saveRepetition() {
        Spinner type = (Spinner) getDialog().findViewById(R.id.typeSpinner);
        EditText interval = (EditText) getDialog().findViewById(R.id.intervalEditText);

        listener.onRepetitionSave(Repetition.repeat(note)
                .every(Integer.parseInt(interval.getText().toString()),
                        adapter.getItem(type.getSelectedItemPosition()))
                .from(LocalDate.parse(startDateEditText.getText().toString(),
                        DateTimeFormat.mediumDate())));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (RepetitionDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RepetitionDialogListener");
        }
    }

    void setDatePicker() {
        LocalDate now = getDateTimeForPicker();
        CalendarDatePickerDialog datePicker = CalendarDatePickerDialog
                .newInstance(RepetitionDialogFragment.this, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
        datePicker.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "fragment_date_picker_name");
    }

    private LocalDate getDateTimeForPicker() {
        String string = startDateEditText.getText().toString();
        return string.isEmpty() ? LocalDate.now() :
                LocalDate.parse(string, DateTimeFormat.mediumDate());
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        LocalDate localDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        startDateEditText.setText(localDate.toString(DateTimeFormat.mediumDate()));
    }

    public interface RepetitionDialogListener {
        public void onRepetitionSave(Repetition repetition);
    }
}
