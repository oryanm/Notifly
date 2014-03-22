package net.notifly.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import net.notifly.core.sql.NotesDAO;

import org.joda.time.LocalDateTime;

public class NewNoteActivity extends ActionBarActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_note);
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

  private void save()
  {
    String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();

    if (!title.isEmpty())
    {
      Note note = new Note(title, LocalDateTime.now());
      note.setDescription(((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
      NotesDAO notes = new NotesDAO(this);
      notes.addNote(note);
      notes.close();

      Intent intent = new Intent();
      setResult(RESULT_OK, intent);
      finish();
    }
  }
}