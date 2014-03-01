package net.notifly.core;

import org.joda.time.LocalDateTime;

public class Note {
    public Note(String title, LocalDateTime time) {
        this.title = title;
        this.time = time;
    }

    String title;
    LocalDateTime time;
    // todo: what type is location?
    Object location;
}
