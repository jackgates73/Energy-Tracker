package com.example.user.projectfinale;

/**
 * Created by User on 16/04/2018.
 */

public class userDetails {
    //this class is simply used for getting and setting user details in the database

    private int _id;
    private String Details;

    public userDetails(String uDetails){

        this.Details = uDetails;
    }
    public void set_id(int _id) {

        this._id = _id;
    }
    public void setDetails(String details) {
        Details = details;
    }

    public int get_id() {
        return _id;
    }
    public String getDetails() {
        return Details;
    }
}
