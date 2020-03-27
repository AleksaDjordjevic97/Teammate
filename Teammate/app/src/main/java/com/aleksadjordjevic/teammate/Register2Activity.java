package com.aleksadjordjevic.teammate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

public class Register2Activity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
