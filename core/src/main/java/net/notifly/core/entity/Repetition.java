package net.notifly.core.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.Years;

public class Repetition implements Parcelable {
    Note note;
    TYPE type;
    int interval;
    LocalDate start;
    LocalDate end;

    boolean[] days;

    public Repetition(Note note) {
        this.note = note;
    }

    public static Repetition repeat(Note note) {
        return new Repetition(note);
    }

    public Repetition every(int interval, TYPE type) {
        this.type = type;
        this.interval = interval;
        return this;
    }

    public Repetition from(LocalDate start) {
        this.start = start;
        return this;
    }

    public Repetition until(LocalDate end) {
        this.end = end;
        return this;
    }

    @Override
    public String toString() {
        String s = String.format("Repeat every %d %s, start on %s",
                interval, type.desc, start.toString());

        if (hasEnd()) {
            s += String.format(" and end on %s", end.toString());
        }

        return s;
    }

    public boolean occursAt(LocalDate date) {
        if (date.isBefore(start) ||
                hasEnd() && date.isAfter(end)) return false;

        LocalDate temp = start;
        LocalDate end = getEndDate(date);

        while (!temp.isAfter(end)) {
            if (temp.isEqual(date)) {
                return true;
            }

            temp = temp.plus(type.getPeriod(interval));
        }

        return false;
    }

    private LocalDate getEndDate(LocalDate date) {
        return end == null ? date : end;
    }

    boolean accursAtDaysOfAWeek(LocalDate temp, LocalDate date) {
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                temp = temp.withDayOfWeek(i + 1);

                if (temp.isEqual(date)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasEnd() {
        return end != null;
    }

    public Note getNote() {
        return note;
    }

    public TYPE getType() {
        return type;
    }

    public int getInterval() {
        return interval;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean[] getDays() {
        return days;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /* we don't parcel the note since that would cause an infinite loop
        when the note would try to parcel it's repetition*/

        dest.writeInt(type.ordinal());
        dest.writeInt(interval);
        dest.writeLong(start.toDate().getTime());
        dest.writeLong(hasEnd() ? end.toDate().getTime() : 0);
    }

    public static final Parcelable.Creator<Repetition> CREATOR = new Parcelable.Creator<Repetition>() {
        public Repetition createFromParcel(Parcel in) {
            return new Repetition(in);
        }

        public Repetition[] newArray(int size) {
            return new Repetition[size];
        }
    };

    public Repetition(Parcel in) {
        this.type = TYPE.values()[in.readInt()];
        this.interval = in.readInt();
        this.start = new LocalDate(in.readLong());
        long timeLong = in.readLong();
        this.end = timeLong == 0 ? null : new LocalDate(timeLong);
    }

    public enum TYPE {
        DAILY("days") {
            @Override
            ReadablePeriod getPeriod(int interval) {
                return Days.days(interval);
            }
        },
        WEEKLY("weeks") {
            @Override
            ReadablePeriod getPeriod(int interval) {
                return Weeks.weeks(interval);
            }
        },
        MONTHLY("months") {
            @Override
            ReadablePeriod getPeriod(int interval) {
                return Months.months(interval);
            }
        },
        YEARLY("years") {
            @Override
            ReadablePeriod getPeriod(int interval) {
                return Years.years(interval);
            }
        };

        private final String desc;

        TYPE(String desc) {
            this.desc = desc;
        }

        abstract ReadablePeriod getPeriod(int interval);

        @Override
        public String toString() {
            return desc;
        }
    }
}
