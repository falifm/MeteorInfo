package miculka.jakub.meteorinfo;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jamos on 20.9.2016.
 */

public class Meteor {

    private String name;
    private int mass;
    private int year;
    private float latitude;
    private float longitude;

    public Meteor(String name, int mass, String year, String latitude, String longitude) {
        this.name = name;
        this.mass = mass/1000;
        this.year = Integer.parseInt(year.substring(0, 4));
        this.latitude = Float.parseFloat(latitude);
        this.longitude = Float.parseFloat(longitude);
    }

    public String getName() {
        return name;
    }

    public int getMass() { return mass; }

    public int getYear() {
        return year;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
