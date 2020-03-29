package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private static final int GALLERY_PICK = 1;
    ImageButton btnBack;
    Button btnFriends;
    TextView txtUsername,txtEmail,txtNumOfPosts,txtPhone;
    CircleImageView imgProfile;
    ProgressDialog mDialog;

    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;
    StorageReference mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnBack = findViewById(R.id.btnBackP);
        btnFriends = findViewById(R.id.btnFriendsP);
        txtUsername = findViewById(R.id.txtUsernameP);
        txtEmail = findViewById(R.id.txtEmailP);
        txtNumOfPosts = findViewById(R.id.txtNumOfPostsP);
        txtPhone = findViewById(R.id.txtPhoneP);
        imgProfile = findViewById(R.id.imgProfileP);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images");


        fillText();

        btnFriends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent friendsIntent = new Intent(getApplicationContext(),FriendsActivity.class);
                startActivity(friendsIntent);
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
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(ProfileActivity.this);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent indexIntent = new Intent(getApplicationContext(),IndexActivity.class);
                startActivity(indexIntent);
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        fillText();
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
                mDialog = new ProgressDialog(ProfileActivity.this);
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
                                mDatabase.collection("users").document(userID).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        Glide.with(ProfileActivity.this)
                                                .load(uriLink)
                                                .placeholder(R.drawable.user)
                                                .into(imgProfile);
                                    }
                                });
                            }
                        });
                    }
                });

            }
            else Toast.makeText(ProfileActivity.this,"There was an error. Try again later.",Toast.LENGTH_SHORT).show();
        }
    }

    protected void fillText()
    {
        DocumentReference profileRef = mDatabase.collection("users").document(userID);

        profileRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                User usr = documentSnapshot.toObject(User.class);

                txtEmail.setText(usr.getEmail());
                txtUsername.setText(usr.getUsername());
                txtNumOfPosts.setText(String.valueOf(usr.getNumOfPosts()));
                txtPhone.setText(usr.getPhone());

                Glide.with(getApplicationContext())
                        .load(usr.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(imgProfile);

            }
        });
    }

    @Override
    public void onBackPressed()
    {
        Intent exit = new Intent(getApplicationContext(), IndexActivity.class);
        exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exit);
        finish();
    }
}
