package net.notifly.core;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.sql.NotesDAO;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

public class NotesAdapter extends ArrayAdapter<Note> {
    public NotesAdapter(Context context, List<Note> notes) {
        super(context, R.layout.note_item, notes);
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
        viewHolder.title.setText(note.title);
        viewHolder.time.setText(note.time.toString(DateTimeFormat.fullDateTime()));
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
        Button button1;
    }
}
