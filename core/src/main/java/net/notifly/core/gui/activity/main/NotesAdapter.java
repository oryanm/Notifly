package net.notifly.core.gui.activity.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.util.GeneralUtils;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.FragmentByTag;
import org.joda.time.format.DateTimeFormat;

@EBean
public class NotesAdapter extends ArrayAdapter<Note> {
    @FragmentByTag("notes")
    NotesMainFragment fragment;

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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_item, parent, false);
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
        viewHolder.location.setText(note.hasLocation() ? GeneralUtils.toString(note.getLocation().address) : "");

        return convertView;
    }

    public void delete(Note note, int position) {
        remove(note);
        notifyDataSetChanged();
        fragment.deleteNote(note, position);
    }

    private static class ViewHolder {
        TextView title;
        TextView time;
        TextView location;
        ImageButton delete;
    }
}
