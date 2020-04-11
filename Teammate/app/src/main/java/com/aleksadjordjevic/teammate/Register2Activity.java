package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register2Activity extends AppCompatActivity
{
    final static int GALLERY_PICK = 1;
    Button btnRegister;
    EditText txtUsername;
    CircleImageView imgProfile;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    StorageReference mStorage;
    FirebaseFirestore mDatabase;
    DocumentReference profileRef;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnRegister = findViewById(R.id.btnRegisterR2);
        txtUsername = findViewById(R.id.txtUsernameR2);
        imgProfile = findViewById(R.id.imgProfileR2);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mDatabase = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    saveUserDetails();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        //.setCropShape(CropImageView.CropShape.OVAL)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(Register2Activity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                mDialog = new ProgressDialog(Register2Activity.this);
                mDialog.setTitle("Profile image");
                mDialog.setMessage("Please wait while the image is uploaded...");
                mDialog.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = mStorage.child(userID+".jpeg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        mDialog.dismiss();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String uriLink = uri.toString();

                                Map<String,Object> userMap = new HashMap<>();
                                userMap.put("profile_image",uriLink);
                                mDatabase.collection("users").document(userID).set(userMap,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        Glide.with(Register2Activity.this)
                                                .load(uriLink)
                                                .placeholder(R.drawable.ic_launcher_background)
                                                .into(imgProfile);
                                    }
                                });
                            }
                        });
                    }
                });

            }
            else Toast.makeText(Register2Activity.this,"There was an error. Try again later.",Toast.LENGTH_SHORT).show();
        }
    }

    protected void saveUserDetails()
    {
        mDialog = new ProgressDialog(Register2Activity.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();

        final String username = txtUsername.getText().toString().trim();

        if (username.isEmpty())
        {
            mDialog.dismiss();
            txtUsername.setError("Please enter your username.");
            txtUsername.requestFocus();
            return;
        }

        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    mDialog.dismiss();
                    Map<String,Object> userMap = new HashMap<>();
                    userMap.put("username",username);
                    mDatabase.collection("users").document(userID).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                            user.sendEmailVerification();
                            setUser();
                        }
                    });
                }
                else
                {
                    mDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "There was an Error. Try again later.", Toast.LENGTH_SHORT).show();
                    sendToMain();
                }
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Register2Activity.this);
        builder.setTitle("Exit");
        builder.setMessage("Please enter your username.");
        builder.setCancelable(false);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void sendToMain()
    {
        Intent mainIntent = new Intent(getApplicationContext(), Main2Activity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    protected void setUser()
    {
        profileRef = mDatabase.collection("users").document(userID);

        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    final UserModel u = task.getResult().toObject(UserModel.class);
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Location> task)
                        {
                            if(task.isSuccessful())
                            {
                                Location location = task.getResult();
                                GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());

                                u.setGeo_point(geoPoint);
                                u.setTimestamp(null);

                                ((UserClient)(getApplicationContext())).setUser(u);
                                sendToMap();

                            }

                        }
                    });

                }
            }
        });
    }

    public void sendToMap()
    {
        Intent mapIntent = new Intent(getApplicationContext(), IndexActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapIntent);
        finish();
    }


}
