package com.aleksadjordjevic.teammate;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserModel
{


    private String username;
    private String profile_image;
    private long numOfPosts;
    private String phone;
    private String email;
    private List<String> friends;
    private GeoPoint geo_point;
    private @ServerTimestamp Date timestamp;

    public UserModel()
    {
        this.friends = new ArrayList<>();
    }

    public UserModel(String username, String profile_image, long numOfPosts, String phone, String email)
    {
        this.username = username;
        this.profile_image = profile_image;
        this.numOfPosts = numOfPosts;
        this.phone = phone;
        this.email = email;
        this.friends = new ArrayList<>();
    }

    public UserModel(String username, String profile_image, long numOfPosts, String phone, String email, GeoPoint geo_point, Date timestamp)
    {
        this.username = username;
        this.profile_image = profile_image;
        this.numOfPosts = numOfPosts;
        this.phone = phone;
        this.email = email;
        this.friends = new ArrayList<>();
        this.geo_point = geo_point;
        this.timestamp = timestamp;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getProfile_image()
    {
        return profile_image;
    }

    public void setProfile_image(String profile_image)
    {
        this.profile_image = profile_image;
    }

    public long getNumOfPosts()
    {
        return numOfPosts;
    }

    public void setNumOfPosts(long numOfPosts)
    {
        this.numOfPosts = numOfPosts;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public List<String> getFriends()
    {
        return friends;
    }

    public void setFriends(List<String> friends)
    {
        this.friends = friends;
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
}
