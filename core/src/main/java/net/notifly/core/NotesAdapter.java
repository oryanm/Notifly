package net.notifly.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

public class NotesAdapter extends ArrayAdapter<Note> {
    public NotesAdapter(Context context, List<Note> notes) {
        super(context, R.layout.note_item, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Note note = getItem(position);

        ViewHolder viewHolder; // view lookup cache stored in tag
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_item, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.note_title);
            viewHolder.time = (TextView) convertView.findViewById(R.id.note_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.title.setText(note.title);
        viewHolder.time.setText(note.time.toString(DateTimeFormat.fullDateTime()));
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView time;
    }
}
