package net.notifly.core.gui.activity.note;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import net.notifly.core.gui.activity.main.MainActivity;
import net.notifly.core.gui.activity.map.SelectLocationActivity;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

public class NewNoteActivity extends ActionBarActivity implements
        CalendarDatePickerDialog.OnDateSetListener,
        RadialTimePickerDialog.OnTimeSetListener
{
    private static final int LOCATION_SELECT_CODE = 2;
    public static final String EXTRA_LOCATION = "net.notifly.core.location";

    Address address = LocationHandler.ERROR_ADDRESS;
    LocalDateTime dateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        setLocationEditText();
        setDatePicker();
        setTimePicker();
    }

    private void setTimePicker() {
        EditText time = (EditText) findViewById(R.id.timeEditText);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime now = LocalDateTime.now();
                RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog.newInstance(
                        NewNoteActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                        DateFormat.is24HourFormat(NewNoteActivity.this));
                timePickerDialog.show(getSupportFragmentManager(), "timePickerDialogFragment");
            }
        });
    }

    private void setDatePicker() {
        EditText date = (EditText) findViewById(R.id.dateEditText);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime now = LocalDateTime.now();
                CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                        .newInstance(NewNoteActivity.this, now.getYear(), now.getMonthOfYear(), now.getDayOfMonth());
                calendarDatePickerDialog.show(getSupportFragmentManager(), "fragment_date_picker_name");
            }
        });
    }

  private void setLocationEditText()
  {
    AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.locationTextView);
    final AddressAdapter adapter = new AddressAdapter(this, android.R.layout.simple_list_item_1);
    textView.setAdapter(adapter);
    textView.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        address = adapter.getAddress(position);
      }
    });
  }

  private void save()
  {
    String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();

    if (!title.isEmpty())
    {
      Note note = new Note(title, dateTime);
      note.setDescription(((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
      if (LocationHandler.isValid(address)) note.setLocation(Location.from(address));

      NotesDAO notes = new NotesDAO(this);
      note.setId((int)notes.addNote(note));
      notes.close();

      Intent intent = new Intent();
      intent.putExtra(MainActivity.EXTRA_NOTE, note);
      setResult(RESULT_OK, intent);
      finish();
    }  else {
        Toast toast = Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
  }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        LocalDate localDate = new LocalDate(year, monthOfYear, dayOfMonth);
        EditText date = (EditText) findViewById(R.id.dateEditText);
        date.setText(localDate.toString(DateTimeFormat.mediumDate()));
        dateTime = LocalDateTime.now().withDate(year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        LocalTime localTime = new LocalTime(hourOfDay, minute);
        EditText time = (EditText) findViewById(R.id.timeEditText);
        time.setText(localTime.toString(DateTimeFormat.shortTime()));
        dateTime = LocalDateTime.now().withTime(hourOfDay, minute, 0, 0);
    }

    public void clear(View view) {
        ((EditText) findViewById(R.id.dateEditText)).setText("");
        ((EditText) findViewById(R.id.timeEditText)).setText("");
        dateTime = LocalDateTime.now();
    }

    public void selectLocation(View view) {
        Intent intent = new Intent(this, SelectLocationActivity.class);
        if (LocationHandler.isValid(address)) intent.putExtra(EXTRA_LOCATION, address);
        startActivityForResult(intent, LOCATION_SELECT_CODE);
    }

    private void afterSelectLocation(Intent intent) {
        address = intent.getParcelableExtra(EXTRA_LOCATION);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.locationTextView);
        textView.setText(GeneralUtils.toString(address));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save_note) {
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case LOCATION_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    afterSelectLocation(intent);
                }
        }
    }
}
