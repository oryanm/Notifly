package net.notifly.core.gui.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.notifly.core.R;

public class TagsTokenView extends TokenCompleteTextView {

    public TagsTokenView(Context context) {
        super(context);
    }

    public TagsTokenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagsTokenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View getViewForObject(Object object) {
        String p = (String) object;

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout) l.inflate(R.layout.tags_token, (ViewGroup) this.getParent(), false);
        ((TextView) view.findViewById(R.id.name)).setText(p);

        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        return completionText;
    }

}
