package com.aleksadjordjevic.teammate;

import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;

public class CourtModel
{

    private String courtID;
    private String name;
    private String picture;
    private String type;
    private GeoPoint location;
    private HashMap<String,String> reviews;
    private HashMap<String,Float> userRatings;

    public CourtModel()
    {
    }

    public CourtModel(String courtID, String name, String picture, String type, GeoPoint location, HashMap<String, String> reviews, HashMap<String, Float> userRatings)
    {
        this.courtID = courtID;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.location = location;
        reviews = new HashMap<String,String>();
        userRatings = new HashMap<String,Float>();
        this.reviews = reviews;
        this.userRatings = userRatings;
    }

    public String getCourtID()
    {
        return courtID;
    }

    public void setCourtID(String courtID)
    {
        this.courtID = courtID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPicture()
    {
        return picture;
    }

    public void setPicture(String picture)
    {
        this.picture = picture;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }


    public GeoPoint getLocation()
    {
        return location;
    }

    public void setLocation(GeoPoint location)
    {
        this.location = location;
    }

    public HashMap<String, String> getReviews()
    {
        return reviews;
    }

    public void setReviews(HashMap<String, String> reviews)
    {
        this.reviews = reviews;
    }

    public HashMap<String, Float> getUserRatings()
    {
        return userRatings;
    }

    public void setUserRatings(HashMap<String, Float> userRatings)
    {
        this.userRatings = userRatings;
    }
}
