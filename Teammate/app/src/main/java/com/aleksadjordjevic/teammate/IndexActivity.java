package com.aleksadjordjevic.teammate;


import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class IndexActivity extends AppCompatActivity
{

    ImageButton navButton;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    CircleImageView navImage;
    TextView navUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
//                .setDrawerLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        navButton = findViewById(R.id.nav_menuButton);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        DocumentReference profileRef = mDatabase.collection("users").document(userID);

        navigationView=findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header_index);
        actionBarDrawerToggle = new ActionBarDrawerToggle(IndexActivity.this,mDrawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navImage = navView.findViewById(R.id.nav_ProfileImage);
        navUsername = navView.findViewById(R.id.nav_Username);


        profileRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                User usr = documentSnapshot.toObject(User.class);

                navUsername.setText(usr.getUsername());
                Glide.with(getApplicationContext())
                        .load(usr.getProfile_image())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(navImage);

            }
        });

        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                userMenuSelector(menuItem);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void userMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_topContributors:
            {
                Intent rangIntent = new Intent(getApplicationContext(), RangActivity.class);
                startActivity(rangIntent);
                break;
            }
            case R.id.nav_logout:
            {
                mAuth.signOut();
                Intent logoutIntent = new Intent(IndexActivity.this,Main2Activity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(logoutIntent);
                finish();
            }

        }
    }
}
