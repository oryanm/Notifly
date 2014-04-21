package net.notifly.core.util;

public enum TravelMode
{
    DRIVING("DRIVING"),WALKING("walking"),BICYCLING("bicycling"),TRANSIT("transit");

    private String mode;

    TravelMode(String mode){
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }

    public static TravelMode getMode(String mode){
        for (TravelMode travelMode: values())
        {
            if (travelMode.mode.equals(mode)) return travelMode;
        }
        return null;
    }
}