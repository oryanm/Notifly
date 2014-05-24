package net.notifly.test;

import android.test.InstrumentationTestCase;

import net.notifly.core.entity.Repetition;

import org.joda.time.LocalDate;

import java.util.Random;

public class RepetitionTest extends InstrumentationTestCase {
    public static final int MONTHS = new Random(System.nanoTime()).nextInt(50000);
    public static final int INTERVAL = Math.min(MONTHS / 10, new Random(System.nanoTime()).nextInt(50000));

    Repetition repetition;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repetition = Repetition
                .repeatEvery(INTERVAL, Repetition.TYPE.MONTHLY)
                .from(LocalDate.now())
                .until(LocalDate.now().plusMonths(MONTHS));
    }

    public void testOccursAtStartDate() {
        assertEquals(true, repetition.occursAt(repetition.getStart()));
    }

    public void testOccursAtEndDate() {
        assertEquals(true, repetition.occursAt(repetition.getStart().plusMonths(repetition.getInterval() * (MONTHS / INTERVAL))));
    }

    public void testNotOccursAfterEndDate() {
        assertEquals(false, repetition.occursAt(repetition.getStart().plusMonths(repetition.getInterval() * (MONTHS / INTERVAL + 1))));
    }

    public void testNotOccursAfterIntervalMinusOne() {
        assertEquals(false, repetition.occursAt(repetition.getStart().plusMonths(repetition.getInterval() - 1)));
    }

    public void testOccursAfterInterval() {
        assertEquals(true, repetition.occursAt(repetition.getStart().plusMonths(repetition.getInterval())));
    }

    public void testOccursAfterIntervalX3() {
        assertEquals(true, repetition.occursAt(repetition.getStart().plusMonths(3 * repetition.getInterval())));
    }

    public void testNotOccursAfterIntervalX3MinusOne() {
        assertEquals(false, repetition.occursAt(repetition.getStart().plusMonths((3 * repetition.getInterval()) - 1)));
    }

    public void testNotOccursAtStartDatePlusSomeDays() {
        assertEquals(false, repetition.occursAt(repetition.getStart().plusDays(4)));
    }

    public void testNotOccursAfterIntervalPlusSomeDays() {
        assertEquals(false, repetition.occursAt(repetition.getStart().plusMonths(repetition.getInterval()).plusDays(4)));
    }
}
