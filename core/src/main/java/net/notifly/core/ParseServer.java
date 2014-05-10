package net.notifly.core;

import android.content.Context;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.notifly.core.entity.Note;

import java.util.Arrays;

/**
 * Created by Barak on 10/05/2014.
 */
public class ParseServer{
    public static final String PARSE_APPLICATION_ID = "BTHJOKX3dCsiZhSGPoHnH7hWPu2LZXaHVsQGmxqy";
    public static final String PARSE_CLIENT_KEY = "4ZAQIi5x1bJbtj7Yd1suMwqEil4u8aFFVvjX3fnS";
    public static final String NOTE_OBJECT = "Note";

    protected static ParseServer instance;
    protected Context appContext;

    public static ParseServer getInstance(Context context){
        if (instance == null) {
            instance = new ParseServer(context);
        }

        return instance;
    }

    protected ParseServer(Context context){
        appContext = context.getApplicationContext();

        Parse.initialize(appContext, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
    }

    public void submitNoteToServer(final Note note)
    {
        ParseQuery query = ParseQuery.getQuery(NOTE_OBJECT);
        query.whereEqualTo("noteId", note.getId());
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null)
                {
                    parseObject = new ParseObject(NOTE_OBJECT);
                    parseObject.put("noteId", note.getId());
                }

                parseObject.put("title", note.getTitle());
                parseObject.put("dayOfWeek", note.getTime().dayOfWeek().getAsShortText());
                parseObject.put("time", note.getTime().toLocalTime().toString());
                parseObject.put("location", note.getLocation().getName());
                parseObject.put("type", (note.hasTime() ? 1 : 0) + (note.hasLocation() ? 2 : 0));
                parseObject.put("timeDelay", 0);
                parseObject.put("travelMode", note.getTravelMode().toString());
                parseObject.put("tags", Arrays.toString(note.getTags().toArray()));
                parseObject.saveInBackground();
            }
        });
    }

    public void lateForNote(Note note, final int timeDelay)
    {
        ParseQuery query = ParseQuery.getQuery(NOTE_OBJECT);
        query.whereEqualTo("noteId", note.getId());
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject != null)
                {
                    parseObject.put("timeDelay", timeDelay);
                    parseObject.saveInBackground();
                }
            }
        });
    }
}
