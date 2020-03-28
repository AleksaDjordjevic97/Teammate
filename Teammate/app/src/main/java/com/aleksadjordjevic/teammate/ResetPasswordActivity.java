package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity
{
    EditText txtEmail;
    Button btnReset;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtEmail = findViewById(R.id.txtEmailRP);
        btnReset = findViewById(R.id.btnResetRP);
        mAuth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendResetLink();
            }
        });

    }


    private void sendResetLink()
    {
        String email = txtEmail.getText().toString().trim();

        if(email.isEmpty())
        {
            txtEmail.setError("Please enter your Email.");
            txtEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            txtEmail.setError("Please enter a valid Email address.");
            txtEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Check your email to reset your password.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"There was an error. Try again later.",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

}
