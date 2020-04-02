package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    ImageButton btnBack;
    RecyclerView rcvFriends;
    LinearLayoutManager mLayoutManager;
    FirestoreRecyclerAdapter adapter;

    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    UserModel usr;
    FirebaseFirestore mDatabase;
    DocumentReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnBack = findViewById(R.id.btnBackF);
        rcvFriends = findViewById(R.id.rcvFriends);
        rcvFriends.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(FriendsActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        rcvFriends.setLayoutManager(mLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        userRef = mDatabase.collection("users").document(userID);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                usr = documentSnapshot.toObject(UserModel.class);
                if(usr.getFriends() != null)
                    showFriends(usr.getFriends());
            }
        });

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

    @Override
    protected void onStart() {
        super.onStart();
        //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
       // adapter.stopListening();
    }

    protected void showFriends(List<String> friendsList)
    {
        Query showFriends = mDatabase.collection("users").whereIn(FieldPath.documentId(),friendsList);
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(showFriends, UserModel.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<UserModel, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull UserModel model)
            {
                holder.username.setText(model.getUsername());
                Glide.with(FriendsActivity.this)
                        .load(model.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(holder.profileImage);
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_rcv_layout,parent,false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        rcvFriends.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        CircleImageView profileImage;

        public FriendsViewHolder(View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.txtUsernameFRCV);
            profileImage = itemView.findViewById(R.id.imgFRCV);
        }
    }
}
