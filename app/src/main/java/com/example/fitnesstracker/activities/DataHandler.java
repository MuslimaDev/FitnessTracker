package com.example.fitnesstracker.activities;

import java.util.Observable;

public class DataHandler extends Observable {
    private static DataHandler handler = new DataHandler();
    boolean newValue = true;
    private ConnectionThread reader;
    private DeviceConnectionThread H7;
    private int position, value, minimum, maximum, data, total = 0;
    private int id;

    private DataHandler() {
    }

    static DataHandler getInstance() {
        return handler;
    }

    void acqui(int i) {
        if (i == 254) {
            position = 0;
        } else if (position == 5) {
            cleanInput(i);
        }
        position++;
    }

    void cleanInput(int i) {
        value = i;
        if (value != 0) {
            data += value;
            total++;
        }
        if (value < minimum || minimum == 0)
            minimum = value;
        else if (value > maximum)
            maximum = value;
        setChanged();
        notifyObservers();
    }

    String getLastValue() {
        return value + " BPM";
    }

    public int getLastIntValue() {
        return value;
    }

    String getMin() {
        return minimum + " BPM";
    }

    String getMax() {
        return maximum + " BPM";
    }

    String getAvg() {
        if (total == 0)
            return 0 + " BPM";
        return data / total + " BPM";
    }

    public void setNewValue(boolean newValue) {
        this.newValue = newValue;
    }

    ConnectionThread getReader() {
        return reader;
    }

    void setReader(ConnectionThread reader) {
        this.reader = reader;
    }

    int getID() {
        return id;
    }

    void setID(int id) {
        this.id = id;
    }

    void setH7(DeviceConnectionThread H7) {
        this.H7 = H7;
    }

    DeviceConnectionThread getH7() {
        return H7;
    }
}
