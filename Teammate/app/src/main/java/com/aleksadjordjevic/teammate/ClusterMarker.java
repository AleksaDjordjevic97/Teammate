package com.aleksadjordjevic.teammate;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem
{
    private LatLng position;
    private String title;
    private String snippet;

    private String iconPicture;
    private UserModel user;

    public ClusterMarker()
    {
    }

    public ClusterMarker(LatLng position, String title, String snippet, String iconPicture, UserModel user)
    {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
        this.user = user;
    }


    @Override
    public LatLng getPosition()
    {
        return position;
    }

    public void setPosition(LatLng position)
    {
        this.position = position;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public String getSnippet()
    {
        return snippet;
    }

    public void setSnippet(String snippet)
    {
        this.snippet = snippet;
    }

    public String getIconPicture()
    {
        return iconPicture;
    }

    public void setIconPicture(String iconPicture)
    {
        this.iconPicture = iconPicture;
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
