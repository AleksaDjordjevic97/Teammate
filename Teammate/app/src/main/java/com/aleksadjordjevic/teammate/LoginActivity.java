package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity
{

    EditText txtEmail, txtPassword;
    TextView txtResetPassword;
    Button btnLogin;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtEmail = findViewById(R.id.txtEmailL);
        txtPassword = findViewById(R.id.txtPasswordL);
        txtResetPassword = findViewById(R.id.txtPasswordReset);
        btnLogin = findViewById(R.id.btnLoginL);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userLogin();
            }
        });

        txtResetPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent resetPassword = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(resetPassword);
            }
        });

    }

    protected void userLogin()
    {
        mDialog = new ProgressDialog(LoginActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();

        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if(checkInputError(email,password))
        {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    mDialog.dismiss();

                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Login successful!",Toast.LENGTH_SHORT).show();
                        sendToMap();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"There was an error. Try again later.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
            mDialog.dismiss();

    }

    public boolean checkInputError(String email,String password)
    {
        if(email.isEmpty())
        {
            txtEmail.setError("Please enter your Email.");
            txtEmail.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            txtEmail.setError("Please enter a valid Email address.");
            txtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty())
        {
            txtPassword.setError("Please enter your password.");
            txtPassword.requestFocus();
            return false;
        }

        return true;
    }

    public void sendToMap()
    {
        Intent mapIntent = new Intent(getApplicationContext(), IndexActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapIntent);
        finish();
    }
}
