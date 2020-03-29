package com.aleksadjordjevic.teammate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

public class FriendsActivity extends AppCompatActivity
{
    ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);



        btnBack = findViewById(R.id.btnBackF);


        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(getApplicationContext(),ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
    }
}
