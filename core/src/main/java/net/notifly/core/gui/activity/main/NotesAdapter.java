package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.LocationHandler;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.sql.NotesDAO;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

public class NotesAdapter extends ArrayAdapter<Note> {
  private LocationHandler locationHandler;

  public NotesAdapter(Context context, List<Note> notes) {
        super(context, R.layout.note_item, notes);
      locationHandler = new LocationHandler(context, false);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Note note = getItem(position);

        ViewHolder viewHolder; // view lookup cache stored in tag
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_item, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.note_title);
            viewHolder.time = (TextView) convertView.findViewById(R.id.note_time);
            viewHolder.location = (TextView) convertView.findViewById(R.id.note_location);
            viewHolder.button1 = (Button) convertView.findViewById(R.id.swipe_button4);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.button1.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          delete(note, position);
        }
      });
        // Populate the data into the template view using the data object
        viewHolder.title.setText(note.getTitle());
        viewHolder.time.setText(note.getTime().toString(DateTimeFormat.mediumDateTime()));
        viewHolder.location.setText(String.valueOf(note.getId()));
        // Return the completed view to render on screen
        return convertView;
    }

  public void delete(Note note, int position)
    {
      NotesDAO notesDAO = new NotesDAO(getContext());
      notesDAO.deleteNote(note);
      notesDAO.close();

      remove(note);
      notifyDataSetChanged();

      SwipeListView list = (SwipeListView) ((Activity)getContext()).findViewById(R.id.notes_list_view);
      list.closeAnimate(position);
    }

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView time;
        TextView location;
        Button button1;
    }
}
