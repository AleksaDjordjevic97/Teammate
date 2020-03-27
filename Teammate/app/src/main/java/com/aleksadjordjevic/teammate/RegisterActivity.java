package com.aleksadjordjevic.teammate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity
{
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnRegister = findViewById(R.id.btnRegisterR);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent reg = new Intent(getApplicationContext(),Register2Activity.class);
                startActivity(reg);
            }
        });
    }
}
