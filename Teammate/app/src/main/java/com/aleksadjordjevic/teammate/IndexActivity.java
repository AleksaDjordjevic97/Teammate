package com.aleksadjordjevic.teammate;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.aleksadjordjevic.teammate.services.LocationService;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class IndexActivity extends AppCompatActivity implements OnMapReadyCallback
{

    ImageButton navButton;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;
    DocumentReference profileRef;

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    CircleImageView navImage;
    TextView navUsername;
    Switch locationSwitch;
    Boolean sendLocation;

    private GoogleMap mMap;
    LatLngBounds mMapBoundary;

    FusedLocationProviderClient mFusedLocationClient;
    UserModel userModel;
    List<String> friendsIDList;
    List<UserModel> friendsList;
    ClusterManager mClusterManager;
    MyClusterManagerRenderer mClusterManagerRenderer;
    ArrayList<ClusterMarker> mClusterMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navButton = findViewById(R.id.nav_menuButton);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        profileRef = mDatabase.collection("users").document(userID);


        navigationView=findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header_index);
        actionBarDrawerToggle = new ActionBarDrawerToggle(IndexActivity.this,mDrawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navImage = navView.findViewById(R.id.nav_ProfileImage);
        navUsername = navView.findViewById(R.id.nav_Username);
        locationSwitch = navView.findViewById(R.id.nav_locationSwitch);
        if(locationSwitch.isActivated())
            sendLocation = true;
        else
            sendLocation = false;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userModel = new UserModel();
        friendsIDList = new ArrayList<>();
        friendsList = new ArrayList<>();
        mClusterMarkers = new ArrayList<>();

        //setUserDetails();

        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        navImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(getApplicationContext(),ProfileActivity.class);
                startActivity(profileIntent);
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

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    sendLocation = true;
                    userModel.setLocationSharing(true);
                    profileRef.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                                startLocationService();
                        }
                    });

                }
                else
                {
                    sendLocation = false;
                    userModel.setLocationSharing(false);
                    profileRef.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                                stopLocationService();
                        }
                    });

                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    protected void setUserDetails()
    {

        profileRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                userModel = documentSnapshot.toObject(UserModel.class);

                navUsername.setText(userModel.getUsername());
                Glide.with(getApplicationContext())
                        .load(userModel.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(navImage);
                sendLocation = userModel.isLocationSharing();
                locationSwitch.setChecked(sendLocation);

                //getLastKnownLocation();
                if(sendLocation)
                    startLocationService();

                getFriendsList();

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setUserDetails();
       // getLastKnownLocation();
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
            case R.id.nav_AddFriends:
            {
                Intent addFriendsIntent = new Intent(getApplicationContext(), FriendMenuActivity.class);
                startActivity(addFriendsIntent);
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

    /**
        MAP PART
    */


//    protected void getLastKnownLocation()
//    {
//        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
//        {
//            @Override
//            public void onComplete(@NonNull Task<Location> task)
//            {
//                if(task.isSuccessful())
//                {
//                    Location location = task.getResult();
//                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
//
//                    userModel.setGeo_point(geoPoint);
//                    userModel.setTimestamp(null);
//                    saveUserLocation();
//
//                }
//
//            }
//        });
//    }
//
//    protected void saveUserLocation()
//    {
//        if(userModel != null)
//        {
//            DocumentReference locationRef = mDatabase.collection("users").document(userID);
//            locationRef.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>()
//            {
//                @Override
//                public void onComplete(@NonNull Task<Void> task)
//                {
//                    if(sendLocation)
//                        startLocationService();
//
//                    getFriendsList();
//                }
//            });
//        }
//    }


    protected void getFriendsList()
    {
        DocumentReference userRef = mDatabase.collection("users").document(userID);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
               UserModel usr = documentSnapshot.toObject(UserModel.class);
                if(usr.getFriends() != null)
                {
                    friendsIDList = usr.getFriends();
                    getFriendsLocations();
                }
            }
        });
    }

    protected void getFriendsLocations()
    {
        for (String fid:friendsIDList)
        {

            DocumentReference friendRef = mDatabase.collection("users").document(fid);

            friendRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    UserModel usr = documentSnapshot.toObject(UserModel.class);
                    if(!friendsList.contains(usr) && usr.isLocationSharing())
                        friendsList.add(usr);
                }
            });
        }
        setCameraView();
        addMapMarkers();
    }

    protected void setCameraView()
    {
        double bottomBoundary = userModel.getGeo_point().getLatitude() - .1;
        double leftBoundary = userModel.getGeo_point().getLongitude() - .1;
        double topBoundary = userModel.getGeo_point().getLatitude() + .1;
        double rightBoundary = userModel.getGeo_point().getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary,leftBoundary),
                                        new LatLng(topBoundary,rightBoundary));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,0));
    }

    protected void addMapMarkers()
    {

        if(mMap != null)
        {
            if(mClusterManager == null)
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);

            if(mClusterManagerRenderer == null)
            {
                mClusterManagerRenderer = new MyClusterManagerRenderer(getApplicationContext(), mMap, mClusterManager);
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            try
            {
                String snippet = "Email: " + userModel.getEmail() + "\n"
                        +"Number of posts: " + userModel.getNumOfPosts();


                ClusterMarker myClusterMarker = new ClusterMarker(
                        new LatLng(userModel.getGeo_point().getLatitude(),userModel.getGeo_point().getLongitude()),
                        userModel.getUsername(),
                        snippet,
                        userModel.getProfile_image(),
                        userModel
                );
                mClusterManager.addItem(myClusterMarker);
                mClusterMarkers.add(myClusterMarker);

            }catch (NullPointerException e)
            { }
            mClusterManager.cluster();

            for(UserModel userLocation: friendsList)
            {
                try
                {
                    String snippet2 = "Email: " + userLocation.getEmail() + "\n"
                            +"Number of posts: " + userLocation.getNumOfPosts();

                    ClusterMarker friendClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(),userLocation.getGeo_point().getLongitude()),
                            userLocation.getUsername(),
                            snippet2,
                            userLocation.getProfile_image(),
                            userLocation
                    );
                    mClusterManager.addItem(friendClusterMarker);
                    mClusterMarkers.add(friendClusterMarker);

                }catch (NullPointerException e)
                { }

            }
            mClusterManager.cluster();

            setCameraView();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon());
//       mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

      //  setCameraView();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent exit = new Intent(Intent.ACTION_MAIN);
                exit.addCategory(Intent.CATEGORY_HOME);
                exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(exit);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void startLocationService()
    {
        if(!isLocationServiceRunning())
        {
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                IndexActivity.this.startForegroundService(serviceIntent);
            else
                startService(serviceIntent);
        }
    }

    protected void stopLocationService()
    {
//        if(isLocationServiceRunning())
//        {
            Intent serviceIntent = new Intent(this, LocationService.class);
            //this.stopService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                getApplicationContext().stopService(serviceIntent);
            else
                stopService(serviceIntent);
        //}
    }

    private boolean isLocationServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if("package com.aleksadjordjevic.teammate.services.LocationService".equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
}
