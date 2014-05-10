package net.notifly.core.gui.activity.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.notifly.core.R;
import net.notifly.core.entity.Location;

import org.androidannotations.annotations.EBean;

@EBean
public class LocationAdapter extends ArrayAdapter<Location> {
    public LocationAdapter(Context context) {
        super(context, R.layout.location_item);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Location location = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.titleTextView);
            viewHolder.address = (TextView) convertView.findViewById(R.id.addressTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(location.getTitle());
        viewHolder.address.setText(location.toString());
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView address;
    }
}