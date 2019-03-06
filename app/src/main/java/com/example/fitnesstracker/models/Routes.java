package com.example.fitnesstracker.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Routes  extends RealmObject {

    @PrimaryKey
    private int id;
    private String distance;
    private String time;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
