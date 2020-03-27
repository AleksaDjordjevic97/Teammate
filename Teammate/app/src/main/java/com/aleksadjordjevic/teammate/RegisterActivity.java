package com.aleksadjordjevic.teammate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity
{
    EditText txtEmail,txtPassword, txtPassword2;
    Button btnRegister;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtEmail = findViewById(R.id.txtEmailR);
        txtPassword = findViewById(R.id.txtPasswordR);
        txtPassword2 = findViewById(R.id.txtConfirmPasswordR);
        btnRegister = findViewById(R.id.btnRegisterR);
        mAuth = FirebaseAuth.getInstance();


    }
}
