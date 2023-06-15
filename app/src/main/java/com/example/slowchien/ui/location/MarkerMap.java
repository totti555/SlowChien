package com.example.slowchien.ui.location;

import androidx.annotation.NonNull;

public class MarkerMap {

    private final double latitude;

    private final double longitude;

    private final String titre;

    private final String desc;


    public MarkerMap(double latitude, double longitude, String titre, String desc) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.titre = titre;
        this.desc = desc;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTitre() {
        return titre;
    }

    public String getDesc() {
        return desc;
    }


    @NonNull
    @Override
    public String toString() {
        return "MarkerMap{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", titre='" + titre + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
