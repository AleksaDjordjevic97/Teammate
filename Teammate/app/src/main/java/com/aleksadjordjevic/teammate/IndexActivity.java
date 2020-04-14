package com.aleksadjordjevic.teammate;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.clustering.ClusterManager;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class IndexActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final int LOCATION_UPDATE_INTERVAL = 4000;

    ImageButton navButton;
    FloatingActionButton fbtnAddCourt;
    ProgressDialog mDialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;
    DocumentReference profileRef;
    CollectionReference courtRef;
    StorageReference mStorage;

    UserModel userModel;

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    CircleImageView navImage;
    TextView navUsername;
    Switch locationSwitch;
    Boolean sendLocation;

    CircleImageView imgCourt;
    RatingBar courtRatingD;
    RadioButton rbtnSoccer;
    RadioButton rbtnBasketball;
    RadioButton rbtnTennis;
    RadioButton rbtnOther;
    EditText txtCourtNameDialog;
    Button btnAddCourtDialog;
    String urlCourt;
    String courtID;

    private GoogleMap mMap;
    LatLngBounds mMapBoundary;
    FusedLocationProviderClient mFusedLocationClient;
    ArrayList<String> friendsIDList;
    ArrayList<UserModel> friendsList;
    ArrayList<CourtModel> courtList;
    ClusterManager mClusterManager;
    MyClusterManagerRenderer mClusterManagerRenderer;
    ArrayList<ClusterMarker> mClusterMarkers;
    Handler mHandler = new Handler();
    Runnable mRunnable;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navButton = findViewById(R.id.nav_menuButton);
        fbtnAddCourt = findViewById(R.id.fbtnAddCourt);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        profileRef = mDatabase.collection("users").document(userID);
        courtRef = mDatabase.collection("courts");
        mStorage = FirebaseStorage.getInstance().getReference().child("court_images");

        userModel = ((UserClient) (getApplicationContext())).getUser();

        navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header_index);
        actionBarDrawerToggle = new ActionBarDrawerToggle(IndexActivity.this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navImage = navView.findViewById(R.id.nav_ProfileImage);
        navUsername = navView.findViewById(R.id.nav_Username);
        locationSwitch = navView.findViewById(R.id.nav_locationSwitch);
        if (locationSwitch.isActivated())
            sendLocation = true;
        else
            sendLocation = false;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        friendsIDList = new ArrayList<>();
        friendsList = new ArrayList<>();
        courtList = new ArrayList<>();
        mClusterMarkers = new ArrayList<>();


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
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
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
                if (isChecked)
                {
                    sendLocation = true;
                    userModel.setLocationSharing(true);
                    profileRef.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                                startLocationService();
                        }
                    });

                } else
                {
                    sendLocation = false;
                    userModel.setLocationSharing(false);
                    profileRef.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                                stopLocationService();
                        }
                    });

                }
            }
        });

        fbtnAddCourt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                courtID = "";
                urlCourt = "";
                addCourt();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        userModel = ((UserClient) (getApplicationContext())).getUser();
        setUserDetails();
        startUserLocationsRunnable();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopLocationUpdates();
    }

    protected void setUserDetails()
    {
        navUsername.setText(userModel.getUsername());
        Glide.with(getApplicationContext())
                .load(userModel.getProfile_image())
                .placeholder(R.drawable.user)
                .into(navImage);
        sendLocation = userModel.isLocationSharing();
        locationSwitch.setChecked(sendLocation);

        if (sendLocation)
            startLocationService();
    }

    /**
     * NAVIGATION DRAWER PART
     **/


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
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
                Intent logoutIntent = new Intent(IndexActivity.this, Main2Activity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(logoutIntent);
                finish();
            }

        }
    }

    /**
     * ADD COURT DIALOG PART
     **/

    protected void addCourt()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_court_dialog, null);
        imgCourt = mView.findViewById(R.id.imgCourtDialog);
        courtRatingD = mView.findViewById(R.id.courtRatingDialog);
        rbtnSoccer = mView.findViewById(R.id.rbtnSoccer);
        rbtnBasketball = mView.findViewById(R.id.rbtnBasketball);
        rbtnTennis = mView.findViewById(R.id.rbtnTennis);
        rbtnOther = mView.findViewById(R.id.rbtnOther);
        txtCourtNameDialog = mView.findViewById(R.id.txtCourtNameDialog);
        btnAddCourtDialog = mView.findViewById(R.id.btnAddCourtDialog);
        courtID = UUID.randomUUID().toString();

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btnAddCourtDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Location> task)
                    {
                        if (task.isSuccessful())
                        {
                            String courtName = txtCourtNameDialog.getText().toString().trim();
                            String courtType = "";

                            Location courtLocation = task.getResult();
                            GeoPoint courtGeoPoint = new GeoPoint(courtLocation.getLatitude(), courtLocation.getLongitude());
                            float courtRating = courtRatingD.getRating();

                            if (rbtnSoccer.isChecked())
                                courtType = "Soccer";
                            else if (rbtnBasketball.isChecked())
                                courtType = "Basketball";
                            else if (rbtnTennis.isChecked())
                                courtType = "Tennis";
                            else if (rbtnOther.isChecked())
                                courtType = "Other";

                            if (courtName.equals(""))
                            {
                                txtCourtNameDialog.setError("Please enter a name for this court.");
                                txtCourtNameDialog.requestFocus();
                            } else if (courtType.equals(""))
                            {
                                rbtnSoccer.setError("Please select a type.");
                                rbtnSoccer.requestFocus();
                            } else
                            {
                                CourtModel court = new CourtModel();
                                court.setCourtID(courtID);
                                court.setName(courtName);
                                court.setType(courtType);
                                court.setPicture(urlCourt);
                                court.setRating(courtRating);
                                court.setLocation(courtGeoPoint);
                                courtRef.document(courtID).set(court).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                            Toast.makeText(IndexActivity.this, "Court added successfully.", Toast.LENGTH_SHORT).show();

                                        dialog.dismiss();
                                        getCourtLocations();
                                    }
                                });
                            }
                        }
                    }
                });


            }
        });

        imgCourt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(IndexActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                mDialog = new ProgressDialog(IndexActivity.this);
                mDialog.setTitle("Court image");
                mDialog.setMessage("Please wait while the image is uploaded...");
                mDialog.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = mStorage.child(courtID + ".jpeg");


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
                                urlCourt = uriLink;

                                Glide.with(IndexActivity.this)
                                        .load(uriLink)
                                        .placeholder(R.drawable.user)
                                        .into(imgCourt);

                            }
                        });
                    }
                });

            } else
                Toast.makeText(IndexActivity.this, "There was an error. Try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * MAP PART
     **/

    protected void getFriendsLocations()
    {
        friendsIDList = (ArrayList<String>) userModel.getFriends().clone();
        friendsList.clear();

        for (final String fid : friendsIDList)
        {

            DocumentReference friendRef = mDatabase.collection("users").document(fid);

            friendRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    UserModel usr = documentSnapshot.toObject(UserModel.class);
                    if (usr.isLocationSharing())
                        friendsList.add(usr);

                    if (friendsIDList.size() > 1)
                        friendsIDList.remove(fid);
                    else
                    {
                        friendsIDList.remove(fid);
                        addFriendMarkers();
                    }

                }
            });
        }


    }

    protected void getCourtLocations()
    {

        courtRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    if(task.getResult() != null)
                    {
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            CourtModel cm = document.toObject(CourtModel.class);
                            courtList.add(cm);
                        }
                        addCourtMarkers();
                    }
                }
            }
        });
    }





    protected void addMyMapMarker()
    {

        if(mMap != null)
        {
            if(mClusterManager == null)
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);

            if(mClusterManagerRenderer == null)
            {
                mClusterManagerRenderer = new MyClusterManagerRenderer(IndexActivity.this, mMap, mClusterManager);
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            try
            {
                String snippet = "Click here to see user details. " +"\n"
                        + "ID:" + userModel.getUserID();


                ClusterMarker myClusterMarker = new ClusterMarker(
                        new LatLng(userModel.getGeo_point().getLatitude(),userModel.getGeo_point().getLongitude()),
                        userModel.getUsername(),
                        snippet,
                        userModel.getProfile_image(),
                        userModel.getUserID()

                );
                mClusterManager.addItem(myClusterMarker);
                mClusterMarkers.add(myClusterMarker);

            }catch (NullPointerException e)
            { }
            mClusterManager.cluster();

        }
    }

    protected void addFriendMarkers()
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

            for(UserModel userLocation: friendsList)
            {
                try
                {
                    String snippet = "Click here to see user details. " +"\n"
                            + "ID:" + userLocation.getUserID();


                    ClusterMarker friendClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(),userLocation.getGeo_point().getLongitude()),
                            userLocation.getUsername(),
                            snippet,
                            userLocation.getProfile_image(),
                            userLocation.getUserID()
                    );
                    mClusterManager.addItem(friendClusterMarker);
                    mClusterMarkers.add(friendClusterMarker);

                }catch (NullPointerException e)
                { }

            }
            mClusterManager.cluster();

        }
    }

    protected void addCourtMarkers()
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

            for(CourtModel cortLocation: courtList)
            {
                try
                {
                    String snippet = "Click here to see court details. " +"\n"
                                    + "ID:" + cortLocation.getCourtID();

                    ClusterMarker courtClusterMarker = new ClusterMarker(
                            new LatLng(cortLocation.getLocation().getLatitude(),cortLocation.getLocation().getLongitude()),
                            cortLocation.getName(),
                            snippet,
                            cortLocation.getPicture(),
                            cortLocation.getCourtID()
                    );
                    mClusterManager.addItem(courtClusterMarker);
                   // mClusterMarkers.add(courtClusterMarker);

                }catch (NullPointerException e)
                { }

            }
            mClusterManager.cluster();

        }
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

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        addMyMapMarker();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback()
        {
            @Override
            public void onMapLoaded()
            {
                getFriendsLocations();
                getCourtLocations();
                setCameraView();
            }
        });
        

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener()
        {
            @Override
            public void onInfoWindowLongClick(Marker marker)
            {
                if(marker.getSnippet().startsWith("Click here to see user details."))
                    showUserDialog(marker);
                else if(marker.getSnippet().startsWith("Click here to see court details."))
                    showCourtDialog(marker);
            }
        });


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
    }

    protected void showUserDialog(Marker marker)
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.marker_user_details, null);

        final CircleImageView imgUserMU = mView.findViewById(R.id.imgProfileMU);
        final TextView txtUsernameMU = mView.findViewById(R.id.txtUsernameMU);
        final TextView txtEmailMU = mView.findViewById(R.id.txtEmailMU);
        final TextView txtPhoneMU = mView.findViewById(R.id.txtPhoneMU);
        final TextView txtNumOfPostsMU = mView.findViewById(R.id.txtNumOfPostsMU);
        final Button btnCloseMU = mView.findViewById(R.id.btnCloseMU);

        int index = marker.getSnippet().indexOf("ID:") + 3;
        String uID =  marker.getSnippet().substring(index);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

       mDatabase.collection("users").document(uID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    UserModel userMarker = task.getResult().toObject(UserModel.class);

                    txtUsernameMU.setText(userMarker.getUsername());
                    txtEmailMU.setText(userMarker.getEmail());
                    txtPhoneMU.setText(userMarker.getPhone());
                    txtNumOfPostsMU.setText(String.valueOf(userMarker.getNumOfPosts()));
                    Glide.with(getApplicationContext())
                            .load(userMarker.getProfile_image())
                            .placeholder(R.drawable.user)
                            .into(imgUserMU);

                    btnCloseMU.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });

                }
            }
        });




    }

    protected void showCourtDialog(Marker marker)
    {

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

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                IndexActivity.this.startForegroundService(serviceIntent);
            else
                startService(serviceIntent);
        }
    }

    protected void stopLocationService()
    {
        if(isLocationServiceRunning())
        {
            Intent serviceIntent = new Intent(this, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                getApplicationContext().stopService(serviceIntent);
            else
                stopService(serviceIntent);
        }
    }

    private boolean isLocationServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if("com.aleksadjordjevic.teammate.services.LocationService".equals(service.service.getClassName()))
                return true;
        }
        return false;
    }



    private void startUserLocationsRunnable()
    {
        mHandler.postDelayed(mRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates()
    {
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations()
    {

        try
        {
            for(final ClusterMarker clusterMarker: mClusterMarkers)
            {

                DocumentReference userLocationRef = mDatabase.collection("users").document(clusterMarker.getId());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {

                            final UserModel updatedUserLocation = task.getResult().toObject(UserModel.class);

                            for (int i = 0; i < mClusterMarkers.size(); i++)
                            {
                                try
                                {
                                    if (mClusterMarkers.get(i).getId().equals(updatedUserLocation.getUserID()))
                                    {

                                        LatLng updatedLatLng = new LatLng(updatedUserLocation.getGeo_point().getLatitude(), updatedUserLocation.getGeo_point().getLongitude());

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }

                                } catch (NullPointerException e)
                                { }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e)
        { }

    }
}
