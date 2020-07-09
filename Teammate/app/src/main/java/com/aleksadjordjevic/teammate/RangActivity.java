package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class RangActivity extends AppCompatActivity
{
    ImageButton btnBack;
    RecyclerView rcvRang;
    LinearLayoutManager mLayoutManager;
    FirestoreRecyclerAdapter adapter;
    FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnBack = findViewById(R.id.btnBackRNG);

        rcvRang = findViewById(R.id.rcvRang);
        rcvRang.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(RangActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        rcvRang.setLayoutManager(mLayoutManager);
        mDatabase = FirebaseFirestore.getInstance();

        showRang();

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
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    protected void showRang()
    {
        Query showRang = mDatabase.collection("users").orderBy("numOfPosts",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
               .setQuery(showRang, UserModel.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<UserModel, RangViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull RangViewHolder holder, int position, @NonNull UserModel model)
            {
                holder.username.setText(model.getUsername());
                Glide.with(RangActivity.this)
                        .load(model.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(holder.profileImage);
                long num = model.getNumOfPosts();
                holder.numOfPosts.setText(String.valueOf(num));
            }

            @NonNull
            @Override
            public RangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rang_rcv_layout,parent,false);
                RangViewHolder viewHolder = new RangViewHolder(view);
                return viewHolder;
            }
        };

        rcvRang.setAdapter(adapter);

    }

    public static class RangViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        CircleImageView profileImage;
        TextView numOfPosts;

        public RangViewHolder(View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.txtUsernameRRCV);
            profileImage = itemView.findViewById(R.id.imgRRCV);
            numOfPosts = itemView.findViewById(R.id.txtNumOfPostsRRCV);
        }
    }
}
