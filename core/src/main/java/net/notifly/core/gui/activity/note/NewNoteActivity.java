package net.notifly.core.gui.activity.note;

import android.content.Intent;
import android.location.Address;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.NotesMainFragment;
import net.notifly.core.gui.activity.map.SelectLocationActivity_;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;
import net.notifly.core.util.adapters.TextWatcherAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

@EActivity(R.layout.activity_new_note)
@OptionsMenu(R.menu.new_note)
public class NewNoteActivity extends ActionBarActivity implements
        CalendarDatePickerDialog.OnDateSetListener,
        RadialTimePickerDialog.OnTimeSetListener
{
    private static final int LOCATION_SELECT_CODE = 2;
    public static final String EXTRA_LOCATION = "net.notifly.core.location";

    Address address = LocationHandler.ERROR_ADDRESS;
    LocalDateTime dateTime = null;

    @ViewById(R.id.timeEditText)
    EditText time;
    @ViewById(R.id.dateEditText)
    EditText date;
    @ViewById(R.id.titleEditText)
    EditText title;

    @Click(R.id.timeEditText)
    void setTimePicker() {
        LocalDateTime now = LocalDateTime.now();
        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog.newInstance(
                NewNoteActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                DateFormat.is24HourFormat(NewNoteActivity.this));
        timePickerDialog.show(getSupportFragmentManager(), "timePickerDialogFragment");
    }

    @Click(R.id.dateEditText)
    void setDatePicker() {
        LocalDateTime now = LocalDateTime.now();
        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(NewNoteActivity.this, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
        calendarDatePickerDialog.show(getSupportFragmentManager(), "fragment_date_picker_name");
    }

    @AfterViews
    void setLocationEditText() {
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.locationTextView);
        final AddressAdapter adapter = new AddressAdapter(this, android.R.layout.simple_list_item_1);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                address = adapter.getAddress(position);
            }
        });
        // todo: find a better way?
        textView.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!GeneralUtils.toString(address).equals(s.toString())) {
                    address = LocationHandler.ERROR_ADDRESS;
                }
            }
        });
    }

    @OptionsItem(R.id.action_save_note)
    void save() {
        String titleString = title.getText().toString();

        if (!titleString.isEmpty()) {
            Note note = new Note(titleString, dateTime);
            note.setDescription(((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
            if (LocationHandler.isValid(address)) note.setLocation(Location.from(address));

            NotesDAO notes = new NotesDAO(this);
            note.setId((int) notes.addNote(note));
            notes.close();

            Intent intent = new Intent();
            intent.putExtra(NotesMainFragment.EXTRA_NOTE, note);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast toast = Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        LocalDate localDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        date.setText(localDate.toString(DateTimeFormat.mediumDate()));
        if (dateTime == null) dateTime = LocalDateTime.now();
        dateTime = dateTime.withDate(year, monthOfYear + 1, dayOfMonth);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        LocalTime localTime = new LocalTime(hourOfDay, minute);
        time.setText(localTime.toString(DateTimeFormat.shortTime()));
        if (dateTime == null) dateTime = LocalDateTime.now();
        dateTime = dateTime.withTime(hourOfDay, minute, 0, 0);
    }

    public void clear(View view) {
        date.setText("");
        time.setText("");
        dateTime = null;
    }

    @Click
    public void selectLocation(View view) {
        Intent intent = new Intent(this, SelectLocationActivity_.class);
        if (LocationHandler.isValid(address)) intent.putExtra(EXTRA_LOCATION, address);
        startActivityForResult(intent, LOCATION_SELECT_CODE);
    }

    @OnActivityResult(LOCATION_SELECT_CODE)
    void afterSelectLocation(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            address = intent.getParcelableExtra(EXTRA_LOCATION);
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.locationTextView);
            textView.setText(GeneralUtils.toString(address));
        }
    }

    public void saveLocation(View view) {
        if (LocationHandler.isValid(address)) {
            Location location = Location.from(address);

            LocationDAO locationDAO = new LocationDAO(this);
            // todo: add or update + the title should be what?
            locationDAO.addLocation(location.asFavorite(GeneralUtils.toString(address)));
            locationDAO.close();
        } else {
            Toast toast = Toast.makeText(this, "Select location to love", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
