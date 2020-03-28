package com.aleksadjordjevic.teammate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
{
    CountDownTimer countDownTimer;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        countDownTimer = new CountDownTimer(3500,1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish()
            {
                countDownTimer.cancel();

                Intent nextActivity;
                if(mAuth.getCurrentUser() != null)
                    nextActivity = new Intent(getApplicationContext(), IndexActivity.class);
                else
                    nextActivity = new Intent(getApplicationContext(), Main2Activity.class);


                startActivity(nextActivity);
            }
        }.start();

    }
}
