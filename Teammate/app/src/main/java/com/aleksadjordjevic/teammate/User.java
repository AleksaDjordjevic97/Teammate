package com.aleksadjordjevic.teammate;

public class User
{
    private String username;
    private String profile_image;
    private String email;
    private int numOfPosts;
    private String phone;

    public String getUsername()
    {
        return username;
    }

    public String getProfile_image()
    {
        return profile_image;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setProfile_image(String profile_image)
    {
        this.profile_image = profile_image;
    }

    public int getNumOfPosts()
    {
        return numOfPosts;
    }

    public void setNumOfPosts(int numOfPosts)
    {
        this.numOfPosts = numOfPosts;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
}
