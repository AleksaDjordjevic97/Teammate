package com.aleksadjordjevic.teammate;

public class RangModule
{
    public String username;
    public String profile_image;
    public long numOfPosts;
    public String phone;
    public String email;

    public RangModule()
    {
    }

    public RangModule(String username, String profile_image, long numOfPosts, String phone, String email)
    {
        this.username = username;
        this.profile_image = profile_image;
        this.numOfPosts = numOfPosts;
        this.phone = phone;
        this. email = email;
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
}
