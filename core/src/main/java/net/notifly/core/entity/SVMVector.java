package net.notifly.core.entity;

/**
 * Created by Barak on 29/08/2014.
 */
public class SVMVector {
    private double estimation;
    private double duration;
    private double distance;
    private int travelMode;
    private int hour;
    private int minutes;
    private int dayInWeek;
    private double late;

    public SVMVector(double estimation, double duration, double distance, int travelMode, int hour, int minutes, int dayInWeek, int late) {
        this.estimation = estimation;
        this.duration = duration;
        this.distance = distance;
        this.travelMode = travelMode;
        this.hour = hour;
        this.minutes = minutes;
        this.dayInWeek = dayInWeek;
        this.late = late;
    }

    public double getEstimation() {
        return estimation;
    }

    public void setEstimation(double estimation) {
        this.estimation = estimation;
    }

    public double getLate() {
        return late;
    }

    public void setLate(double late) {
        this.late = late;
    }

    public Object[] toObjectArray(){
        return new Object[]{estimation, duration, distance, travelMode, hour, minutes, dayInWeek, late};
    }
}
