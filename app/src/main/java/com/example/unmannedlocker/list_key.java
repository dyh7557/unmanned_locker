package com.example.unmannedlocker;

import android.app.Activity;

public class list_key extends Activity {
    private String keyName;
    private String place;
    private String available;

    public list_key() {
        this.keyName = "";
        this.place = "";
        this.available = "";
    }

    public list_key(String keyName, String place, String available){
        this.keyName = keyName;
        this.place = place;
        this.available = available;
    }

    public String getKeyName(){
        return keyName;
    }

    public String getPlace(){
        return place;
    }

    public String getAvailable(){
        return available;
    }
}