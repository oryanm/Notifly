package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

@EBean
public class NotesAdapter extends ArrayAdapter<Note> {
    public NotesAdapter(Context context) {
        super(context, R.layout.note_item);
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
            viewHolder.delete = (ImageButton) convertView.findViewById(R.id.deleteNoteButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(note, position);
            }
        });
        // Populate the data into the template view using the data object
        viewHolder.title.setText(note.getTitle());
        viewHolder.time.setText(note.getTime() == null ?
                "" : note.getTime().toString(DateTimeFormat.shortDateTime()));
        viewHolder.location.setText(note.getLocation() == null ? "" : note.getLocation().address);

        return convertView;
    }

    public void delete(Note note, int position) {
        NotesDAO notesDAO = new NotesDAO(getContext());
        notesDAO.deleteNote(note);
        notesDAO.close();

        remove(note);
        notifyDataSetChanged();

        SwipeListView list = (SwipeListView) ((Activity) getContext()).findViewById(R.id.notes_list_view);
        list.closeAnimate(position);
    }

    private static class ViewHolder {
        TextView title;
        TextView time;
        TextView location;
        ImageButton delete;
    }
}
