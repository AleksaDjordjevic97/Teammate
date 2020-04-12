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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity
{
    EditText txtEmail,txtPassword, txtPassword2, txtPhone;
    Button btnRegister;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtEmail = findViewById(R.id.txtEmailR);
        txtPassword = findViewById(R.id.txtPasswordR);
        txtPassword2 = findViewById(R.id.txtConfirmPasswordR);
        txtPhone = findViewById(R.id.txtPhoneR);
        btnRegister = findViewById(R.id.btnRegisterR);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                registerUser();
            }
        });
    }

    public void registerUser()
    {
        mDialog = new ProgressDialog(RegisterActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();

        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String password2 = txtPassword2.getText().toString().trim();
        final String phone = txtPhone.getText().toString().trim();

        if(checkInputError(email,password,password2,phone))
        {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    mDialog.dismiss();

                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_SHORT).show();

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(task.isSuccessful())
                                {
                                    user = mAuth.getCurrentUser();
                                    userID = user.getUid();
                                    fillDefault();
                                }
                                else
                                    Toast.makeText(getApplicationContext(), "There was an error logging in.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(getApplicationContext(), "User with this Email already exists.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "There was an error. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
            mDialog.dismiss();

    }

    public boolean checkInputError(String email,String password,String password2, String phone)
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

        if(password.length() < 6)
        {
            txtPassword.setError("Password must contain at least 6 characters.");
            txtPassword.requestFocus();
            return false;
        }

        if (password2.isEmpty())
        {
            txtPassword2.setError("Please enter your password.");
            txtPassword2.requestFocus();
            return false;
        }

        if(!password.equals(password2))
        {
            txtPassword2.setError("Passwords must match");
            txtPassword2.requestFocus();
            return false;
        }

        if(phone.isEmpty())
        {
            txtPhone.setError("Please enter your phone number.");
            txtPhone.requestFocus();
            return false;
        }

        if(!phone.startsWith("+"))
        {
            txtPhone.setError("Please enter your area code (+...).");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    protected void fillDefault()
    {
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("userID",user.getUid());
        userMap.put("username","user");
        userMap.put("numOfPosts",0);
        userMap.put("email",user.getEmail());
        userMap.put("phone",txtPhone.getText().toString().trim());
        userMap.put("locationSharing",true);
        mDatabase.collection("users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                sendToRegister2();
            }
        });
    }

    public void sendToRegister2()
    {
        Intent r2Intent = new Intent(getApplicationContext(), Register2Activity.class);
        r2Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(r2Intent);
        finish();
    }
}
