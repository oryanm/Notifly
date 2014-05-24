package net.notifly.core.entity;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.Years;

public class Repetition {
    TYPE type;
    int interval;
    LocalDate start;
    LocalDate end;

    boolean[] days;

    public Repetition(TYPE type, int interval) {
        this.type = type;
        this.interval = interval;
    }

    public static Repetition repeatEvery(int interval, TYPE type) {
        return new Repetition(type, interval);
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
        return String.format("Repeat every %d %s, start on %s and end on %s",
                interval, type.desc, start.toString(), end.toString());
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

    private boolean hasEnd() {
        return end != null;
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
    }
}
