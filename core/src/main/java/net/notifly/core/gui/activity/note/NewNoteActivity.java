package net.notifly.core.gui.activity.note;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.MainActivity;
import net.notifly.core.sql.NotesDAO;

import org.joda.time.LocalDateTime;

public class NewNoteActivity extends ActionBarActivity
{
  Address address;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_note);
    setLocationEditText();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.new_note, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_save_note)
    {
      save();
      return true;
    }
    return super.onOptionsItemSelected(item);
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
      Note note = new Note(title, LocalDateTime.now());
      note.setDescription(((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
      if (address != null) note.setLocation(Location.from(address));

      NotesDAO notes = new NotesDAO(this);
      // todo: return id
      notes.addNote(note);
      notes.close();

      Intent intent = new Intent();
      intent.putExtra(MainActivity.EXTRA_NOTE, note);
      setResult(RESULT_OK, intent);
      finish();
    }
  }
}
