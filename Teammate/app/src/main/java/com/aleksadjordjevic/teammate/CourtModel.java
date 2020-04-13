package com.aleksadjordjevic.teammate;

import com.google.firebase.firestore.GeoPoint;

public class CourtModel
{

    private String courtID;
    private String name;
    private String picture;
    private String type;
    private float rating;
    private GeoPoint location;

    public CourtModel()
    {
    }

    public CourtModel(String courtID, String name, String picture, String type, float rating, GeoPoint location)
    {
        this.courtID = courtID;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.rating = rating;
        this.location = location;
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

    public float getRating()
    {
        return rating;
    }

    public void setRating(float rating)
    {
        this.rating = rating;
    }

    public GeoPoint getLocation()
    {
        return location;
    }

    public void setLocation(GeoPoint location)
    {
        this.location = location;
    }
}
