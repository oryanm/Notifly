package net.notifly.core.gui.activity.note;

import android.content.Intent;
import android.location.Address;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import net.notifly.core.Notifly;
import net.notifly.core.ParseServer;
import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.map.SelectLocationActivity_;
import net.notifly.core.gui.view.TagsTokenView;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.sql.TagsDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;
import net.notifly.core.util.TravelMode;
import net.notifly.core.util.adapters.TextWatcherAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Set;

@EActivity(R.layout.activity_new_note)
@OptionsMenu(R.menu.new_note)
public class NewNoteActivity extends ActionBarActivity implements
        CalendarDatePickerDialog.OnDateSetListener,
        RadialTimePickerDialog.OnTimeSetListener,
        FavoriteLocationDialogFragment.FavoriteLocationDialogListener
{
    public static final int NEW_NOTE_CODE = 1;
    private static final int LOCATION_SELECT_CODE = 2;
    public static final String EXTRA_NOTE = "net.notifly.core.note";
    public static final String EXTRA_LOCATION = "net.notifly.core.location";

    Address address = LocationHandler.ERROR_ADDRESS;

    @App
    Notifly notifly;
    @Extra(EXTRA_NOTE)
    Note note = new Note();
    @Bean
    LocationHandler locationHandler;
    @ViewById(R.id.timeEditText)
    EditText time;
    @ViewById(R.id.dateEditText)
    EditText date;
    @ViewById(R.id.titleEditText)
    EditText title;
    @ViewById(R.id.descriptionEditText)
    EditText description;
    @ViewById(R.id.locationTextView)
    AutoCompleteTextView locationTextView;
    @ViewById(R.id.driving)
    ImageButton drivingImageButton;
    @ViewById(R.id.walking)
    ImageButton walkingImageButton;
    @ViewById(R.id.tagsView)
    TagsTokenView tagsView;
    @ViewById(R.id.repetitionText)
    TextView repetition;


    @AfterViews
    void loadNote() {
        title.setText(note.getTitle());
        description.setText(note.getDescription());

        if (note.hasTime()) {
            date.setText(note.getTime().toString(DateTimeFormat.mediumDate()));
            time.setText(note.getTime().toString(DateTimeFormat.shortTime()));

            if (note.repeats()) {
                repetition.setVisibility(View.VISIBLE);
                repetition.setText(note.getRepetition().toString());
            }
        }

        if (note.hasLocation()) {
            setAddress(notifly.get(note.getLocation()));
        }

        setTravelMode(note.getTravelMode());
    }

    void setAddress(Address address) {
        this.address = address;
        locationTextView.setText(GeneralUtils.toString(this.address));
    }

    private void setTravelMode(TravelMode travelMode) {
        if (travelMode.equals(TravelMode.DRIVING)) {
            setDrivingTravelMode();
        } else if (travelMode.equals(TravelMode.WALKING)) {
            setWalkingTravelMode();
        }
    }

    @AfterViews
    void setLocationEditText() {
        final AddressAdapter adapter = new AddressAdapter(this, android.R.layout.simple_list_item_1);
        locationTextView.setAdapter(adapter);
        locationTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                address = adapter.getAddress(position);
            }
        });
        // todo: find a better way?
        locationTextView.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!GeneralUtils.toString(address).equals(s.toString())) {
                    address = LocationHandler.ERROR_ADDRESS;
                }
            }
        });
    }

    @AfterViews
    void setTagsAdapter() {
        TagsDAO tagsDAO = new TagsDAO(this);
        Set<String> tags = tagsDAO.getTags();
        tagsDAO.close();
        tagsView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                tags.toArray(new String[tags.size()])));

        for (String tag : note.getTags()) {
            tagsView.addObject(tag);
        }
    }

    @Click(R.id.timeEditText)
    void setTimePicker() {
        LocalDateTime now = getDateTimeForPicker();
        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog.newInstance(
                NewNoteActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                DateFormat.is24HourFormat(NewNoteActivity.this));
        timePickerDialog.show(getSupportFragmentManager(), "timePickerDialogFragment");
    }

    @Click(R.id.dateEditText)
    void setDatePicker() {
        LocalDateTime now = getDateTimeForPicker();
        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(NewNoteActivity.this, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
        calendarDatePickerDialog.show(getSupportFragmentManager(), "fragment_date_picker_name");
    }

    private LocalDateTime getDateTimeForPicker() {
        return note.hasTime() ? note.getTime() : LocalDateTime.now();
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        LocalDate localDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);
        date.setText(localDate.toString(DateTimeFormat.mediumDate()));
        if (note.getTime() == null) note.setTime(LocalDateTime.now());
        note.setTime(note.getTime().withDate(year, monthOfYear + 1, dayOfMonth));
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        LocalTime localTime = new LocalTime(hourOfDay, minute);
        time.setText(localTime.toString(DateTimeFormat.shortTime()));
        if (note.getTime() == null) note.setTime(LocalDateTime.now());
        note.setTime(note.getTime().withTime(hourOfDay, minute, 0, 0));
    }

    public void clear(View view) {
        date.setText("");
        time.setText("");
        note.setTime(null);
    }

    @OptionsItem(R.id.action_save_note)
    void save() {
        String titleString = title.getText().toString();

        if (!titleString.isEmpty()) {
            note.setTitle(titleString);
            note.setDescription(description.getText().toString());
            if (LocationHandler.isValid(address)) note.setLocation(Location.from(address));
            List<Object> objects = tagsView.getObjects();
            note.setTags(Sets.newHashSet(Iterables.filter(objects, String.class)));

            NotesDAO notes = new NotesDAO(this);
            note.setId((int) notes.addOrUpdateNote(note));
            notes.close();

            if (note.hasLocation()) ParseServer.getInstance(this).submitNoteToServer(note);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_NOTE, note);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            toast("Title is required");
        }
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
            setAddress(intent.<Address>getParcelableExtra(EXTRA_LOCATION));
        }
    }

    public void saveLocation(View view) {
        if (LocationHandler.isValid(address)) {
            new FavoriteLocationDialogFragment()
                    .show(getFragmentManager(), "FavoriteLocationDialogFragment");
        } else {
            toast("Select location to love");
        }
    }

    @Override
    public void onTitleSelected(String title) {
        LocationDAO locationDAO = new LocationDAO(this);
        locationDAO.addLocation(Location.from(address).asFavorite(title));
        locationDAO.close();
        toast("Location saved to My Locations");
    }

    private void toast(CharSequence text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Click(R.id.driving)
    void setDrivingTravelMode()
    {
        note.setTravelMode(TravelMode.DRIVING);
        drivingImageButton.setImageResource(R.drawable.ic_driving_selected);
        walkingImageButton.setImageResource(R.drawable.ic_walking);
    }

    @Click(R.id.walking)
    void setWalkingTravelMode()
    {
        note.setTravelMode(TravelMode.WALKING);
        drivingImageButton.setImageResource(R.drawable.ic_driving);
        walkingImageButton.setImageResource(R.drawable.ic_walking_selected);
    }
}
