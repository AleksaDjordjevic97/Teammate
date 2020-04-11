package com.aleksadjordjevic.teammate;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocationModel
{
    private GeoPoint geo_point;
    private @ServerTimestamp Date timestamp;
    private UserModel user;

    public UserLocationModel()
    {
    }

    public UserLocationModel(GeoPoint geo_point, Date timestamp, UserModel user)
    {
        this.geo_point = geo_point;
        this.timestamp = timestamp;
        this.user = user;
    }

    public GeoPoint getGeo_point()
    {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point)
    {
        this.geo_point = geo_point;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public UserModel getUser()
    {
        return user;
    }

    public void setUser(UserModel user)
    {
        this.user = user;
    }
}
