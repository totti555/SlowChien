package com.example.slowchien.ui.location;

import androidx.annotation.NonNull;

public class MarkerMap {

    private double latitude;

    private double longitude;

    private String titre;

    private String desc;


    public MarkerMap(double latitude, double longitude, String titre, String desc) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.titre = titre;
        this.desc = desc;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
