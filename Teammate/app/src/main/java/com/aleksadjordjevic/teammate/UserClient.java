package com.aleksadjordjevic.teammate;

import android.app.Application;

public class UserClient extends Application
{
    private UserModel user = null;
    public UserModel getUser() {return user;}
    public void setUser(UserModel user) {this.user = user;}
}
