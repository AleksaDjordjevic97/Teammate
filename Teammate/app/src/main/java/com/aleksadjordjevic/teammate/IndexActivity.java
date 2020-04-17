package com.aleksadjordjevic.teammate;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aleksadjordjevic.teammate.services.LocationService;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class IndexActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final int LOCATION_UPDATE_INTERVAL = 4000;
    private static final double WGS84_RADIUS = 6370997.0;
    private static double EarthCircumFence = 2* WGS84_RADIUS * Math.PI;

    ImageButton navButton;
    FloatingActionButton fbtnAddCourt;
    FloatingActionButton fbtnSearch;
    FloatingActionButton fbtnFilter;
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

    RecyclerView rcvCourtReviews;
    RecyclerView rcvSearchResults;

    boolean personFilter;
    boolean courtFilter;
    boolean basketballFilter;
    boolean soccerFilter;
    boolean tennisFilter;
    boolean otherFilter;

    ArrayList<String> notifiedAbout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navButton = findViewById(R.id.nav_menuButton);
        fbtnAddCourt = findViewById(R.id.fbtnAddCourt);
        fbtnSearch = findViewById(R.id.fbtnSearch);
        fbtnFilter = findViewById(R.id.fbtnFilter);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        profileRef = mDatabase.collection("users").document(userID);
        courtRef = mDatabase.collection("courts");
        mStorage = FirebaseStorage.getInstance().getReference().child("court_images");

        personFilter = true;
        courtFilter = true;
        basketballFilter = true;
        soccerFilter = true;
        tennisFilter = true;
        otherFilter = true;

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

        notifiedAbout = new ArrayList<String>();


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

        fbtnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSearchDialog();
            }
        });

        fbtnFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showFilterDialog();
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


     //////////////////////////////////// NAVIGATION DRAWER PART //////////////////////////////////////////////



    //region NAVIGATION DRAWER
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
    //endregion

    //////////////////////////////////// ADD COURT DIALOG //////////////////////////////////////////////

    //region ADD COURT
    protected void addCourt()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_court_dialog, null);
        imgCourt = mView.findViewById(R.id.imgCourtDialog);
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
    //endregion

    //////////////////////////////////// GET LOCATIONS PART //////////////////////////////////////////////

    //region GET LOCATIONS
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
    //endregion


    //////////////////////////////////// ADD MARKERS PART //////////////////////////////////////////////

    //region ADD MARKERS
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

                boolean advance = false;
                switch (cortLocation.getType())
                {
                    case "Basketball":
                        if(basketballFilter)
                            advance = true;
                        else
                            advance = false;
                        break;

                    case "Soccer":
                        if(soccerFilter)
                            advance = true;
                        else
                            advance = false;
                        break;

                    case "Tennis":
                        if(tennisFilter)
                            advance = true;
                        else
                            advance = false;
                        break;

                    case "Other":
                        if(otherFilter)
                            advance = true;
                        else
                            advance = false;
                        break;
                }

                if(advance)
                {
                    try
                    {
                        String snippet = "Click here to see court details. " + "\n"
                                + "ID:" + cortLocation.getCourtID();

                        ClusterMarker courtClusterMarker = new ClusterMarker(
                                new LatLng(cortLocation.getLocation().getLatitude(), cortLocation.getLocation().getLongitude()),
                                cortLocation.getName(),
                                snippet,
                                cortLocation.getPicture(),
                                cortLocation.getCourtID()
                        );
                        mClusterManager.addItem(courtClusterMarker);
                        mClusterMarkers.add(courtClusterMarker);

                    } catch (NullPointerException e)
                    { }
                }

            }
            mClusterManager.cluster();

        }
    }
    //endregion

    //////////////////////////////////// MAP PART //////////////////////////////////////////////

    //region MAP


    protected void setUserCameraView(UserModel model)
    {

        double bottomBoundary = model.getGeo_point().getLatitude() - .1;
        double leftBoundary = model.getGeo_point().getLongitude() - .1;
        double topBoundary = model.getGeo_point().getLatitude() + .1;
        double rightBoundary = model.getGeo_point().getLongitude() + .1;


        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    protected void setCourtCameraView(CourtModel model)
    {

        double bottomBoundary = model.getLocation().getLatitude() - .1;
        double leftBoundary = model.getLocation().getLongitude() - .1;
        double topBoundary = model.getLocation().getLatitude() + .1;
        double rightBoundary = model.getLocation().getLongitude() + .1;


        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
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
//                getFriendsLocations();
  //              getCourtLocations();

                if(personFilter)
                    getFriendsLocations();

                if(courtFilter)
                    getCourtLocations();

                setUserCameraView(userModel);
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
    //endregion

    //////////////////////////////////// SHOW USER DIALOG PART //////////////////////////////////////////////

    //region SHOW USER DIALOG
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
    //endregion

    //////////////////////////////////// SHOW COURT DIALOG PART //////////////////////////////////////////////

    //region SHOW COURT DIALOG
    protected void showCourtDialog(final Marker marker)
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.marker_court_details, null);

        final CircleImageView imgCourtMC = mView.findViewById(R.id.imgProfileMC);
        final TextView txtCourtNameMC = mView.findViewById(R.id.txtCourtNameMC);
        final ImageView imgCourtTypeMC = mView.findViewById(R.id.imgCourtTypeMC);
        final RatingBar courtRatingMarkerMC = mView.findViewById(R.id.courtRatingMarker);
        rcvCourtReviews = mView.findViewById(R.id.rcvCourtReviews);
        final EditText txtReviewMC = mView.findViewById(R.id.txtMarkerReviewMC);
        final RatingBar courtRatingReviewMC = mView.findViewById(R.id.courtRatingReviewMC);
        final ImageButton btnReviewMC = mView.findViewById(R.id.btnReviewMC);
        final Button btnCloseMC = mView.findViewById(R.id.btnCloseMC);
        LinearLayoutManager mLayoutManager;

        rcvCourtReviews.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(IndexActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        rcvCourtReviews.setLayoutManager(mLayoutManager);

        int index = marker.getSnippet().indexOf("ID:") + 3;
        final String cID =  marker.getSnippet().substring(index);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        mDatabase.collection("courts").document(cID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                final CourtModel court = task.getResult().toObject(CourtModel.class);

                txtCourtNameMC.setText(court.getName());
                Glide.with(getApplicationContext())
                        .load(court.getPicture())
                        .placeholder(R.drawable.photo_blue)
                        .into(imgCourtMC);

                if(court.getUserRatings() == null)
                    courtRatingMarkerMC.setRating(0);
                else
                {
                    float avgRating = calculateAverage(court.getUserRatings());
                    courtRatingMarkerMC.setRating(avgRating);
                }

                if(court.getType().equals("Basketball"))
                    imgCourtTypeMC.setImageResource(R.drawable.basketball);
                else if(court.getType().equals("Soccer"))
                    imgCourtTypeMC.setImageResource(R.drawable.soccer);
                else if(court.getType().equals("Tennis"))
                    imgCourtTypeMC.setImageResource(R.drawable.tennis);
                else if(court.getType().equals("Other"))
                    imgCourtTypeMC.setImageResource(R.drawable.other);

                final ArrayList<String> userReviews = new ArrayList<>();

                if(!court.getReviews().isEmpty())
                {
                    for (String ur : court.getReviews().keySet())
                        userReviews.add(ur);

                    showReviews(userReviews, court.getReviews(), court.getUserRatings());
                }

                btnReviewMC.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(nearCourt(court.getLocation()))
                        {
                            if (!txtReviewMC.getText().toString().trim().equals(""))
                            {
                                Map<String, Object> reviewMap = new HashMap<>();
                                Map<String, String> reviewMapText = new HashMap<>();
                                Map<String, Float> reviewMapRating = new HashMap<>();

                                reviewMapText.put(userModel.getUserID(), txtReviewMC.getText().toString());
                                reviewMapRating.put(userModel.getUserID(), courtRatingReviewMC.getRating());
                                reviewMap.put("reviews", reviewMapText);
                                reviewMap.put("userRatings", reviewMapRating);

                                mDatabase.collection("courts").document(cID).set(reviewMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(!court.getReviews().containsKey(userModel.getUserID()))
                                        {
                                            HashMap<String, Object> reviewUpdateMap = new HashMap<>();
                                            reviewUpdateMap.put("numOfPosts", userModel.getNumOfPosts() + 1);
                                            profileRef.set(reviewUpdateMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        userModel.setNumOfPosts(userModel.getNumOfPosts() + 1);
                                                        Toast.makeText(IndexActivity.this, "Review added successfully.", Toast.LENGTH_SHORT).show();
                                                        showCourtDialog(marker);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            Toast.makeText(IndexActivity.this, "Review added successfully.", Toast.LENGTH_SHORT).show();
                                            showCourtDialog(marker);
                                            dialog.dismiss();
                                        }

                                    }
                                });
                            } else
                            {
                                txtReviewMC.setError("Please write a review first.");
                                txtReviewMC.requestFocus();
                            }
                        }
                        else
                            Toast.makeText(IndexActivity.this, "You need to be near the court to add a review.", Toast.LENGTH_SHORT).show();
                    }
                });


                btnCloseMC.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });


            }
        });

    }

    protected float calculateAverage(HashMap<String,Float> userRatings)
    {
        ArrayList<Float> urList = new ArrayList<>();

        for(float ur:userRatings.values())
            urList.add(ur);

        float sum = 0;

        for(float num:urList)
            sum+=num;

        return sum/urList.size();
    }

    protected boolean nearCourt(GeoPoint courtGeoPoint)
    {
        Location userLocation = new Location("userLocation");
        userLocation.setLatitude(userModel.getGeo_point().getLatitude());
        userLocation.setLongitude(userModel.getGeo_point().getLongitude());

        Location courtLocation = new Location("courtLocation");
        courtLocation.setLatitude(courtGeoPoint.getLatitude());
        courtLocation.setLongitude(courtGeoPoint.getLongitude());

        float distance = userLocation.distanceTo(courtLocation);

        if(distance > 100.0)
            return false;
        else
            return true;

    }

    protected void showReviews(ArrayList<String> usersList, final HashMap<String,String> userReviews, final HashMap<String, Float> userRatings)
    {
        Query showReviews = mDatabase.collection("users").whereIn(FieldPath.documentId(),usersList);
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(showReviews, UserModel.class)
                .build();


        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<UserModel, ReviewViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull UserModel model)
            {
                holder.username.setText(model.getUsername());
                Glide.with(IndexActivity.this)
                        .load(model.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(holder.profileImage);
                holder.review.setText(userReviews.get(model.getUserID()));
                holder.rating.setText(userRatings.get(model.getUserID()).toString());
            }

            @NonNull
            @Override
            public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.marker_review_rcv,parent,false);
                ReviewViewHolder viewHolder = new ReviewViewHolder(view);
                return viewHolder;
            }
        };

        rcvCourtReviews.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        CircleImageView profileImage;
        TextView review;
        TextView rating;

        public ReviewViewHolder(View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.txtUsernameMRCV);
            profileImage = itemView.findViewById(R.id.imgMRCV);
            review = itemView.findViewById(R.id.txtUserReviewMRCV);
            rating = itemView.findViewById(R.id.txtNumOfStarsMRCV);
        }
    }
    //endregion

    //////////////////////////////////// SHOW SEARCH DIALOG PART //////////////////////////////////////////////

    //region SHOW SEARCH DIALOG
    protected void showSearchDialog()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.search_dialog, null);

        final EditText txtSearchSD = mView.findViewById(R.id.txtSearchSD);
        ImageButton btnSearchSD = mView.findViewById(R.id.btnSearchSD);
        ImageButton btnSearchDistance = mView.findViewById(R.id.btnSearchDistanceSD);
        final RadioButton radioPersonSD = mView.findViewById(R.id.radioPersonSD);
        final RadioButton radioCourtSD = mView.findViewById(R.id.radioCourtSD);
        final CheckBox checkBasketballSD = mView.findViewById(R.id.checkBasketballSD);
        final CheckBox checkSoccerSD = mView.findViewById(R.id.checkSoccerSD);
        final CheckBox checkTennisSD = mView.findViewById(R.id.checkTennisSD);
        final CheckBox checkOtherSD = mView.findViewById(R.id.checkOtherSD);
        final EditText txtRadiusSD = mView.findViewById(R.id.txtRadiusSD);
        rcvSearchResults = mView.findViewById(R.id.rcvSearchResults);
        Button btnCloseSD = mView.findViewById(R.id.btnCloseSD);

        LinearLayoutManager mLayoutManager;
        rcvSearchResults.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(IndexActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        rcvSearchResults.setLayoutManager(mLayoutManager);



        radioCourtSD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    checkBasketballSD.setClickable(true);
                    checkSoccerSD.setClickable(true);
                    checkTennisSD.setClickable(true);
                    checkOtherSD.setClickable(true);
                }
                else
                {
                    checkBasketballSD.setClickable(false);
                    checkBasketballSD.setChecked(false);
                    checkSoccerSD.setClickable(false);
                    checkSoccerSD.setChecked(false);
                    checkTennisSD.setClickable(false);
                    checkTennisSD.setChecked(false);
                    checkOtherSD.setClickable(false);
                    checkOtherSD.setChecked(false);
                }
            }
        });

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btnSearchSD.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                if(radioPersonSD.isChecked())
                {
                    searchUsers(false,0,txtSearchSD.getText().toString().trim(), dialog);
                }
                else if(radioCourtSD.isChecked())
                {
                    ArrayList<String> types = new ArrayList<>();
                    boolean searchTypes;

                    if(checkBasketballSD.isChecked())
                        types.add("Basketball");

                    if(checkSoccerSD.isChecked())
                        types.add("Soccer");

                    if(checkTennisSD.isChecked())
                        types.add("Tennis");

                    if(checkOtherSD.isChecked())
                        types.add("Other");

                    if(types.isEmpty())
                        searchTypes = false;
                    else
                        searchTypes = true;

                    searchCourts(searchTypes,types,false,0,txtSearchSD.getText().toString().trim(),dialog);

                }
            }
        });

        btnSearchDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int radius;

                if (radioPersonSD.isChecked())
                {

                    if (txtRadiusSD.getText().toString().trim().equals(""))
                    {
                        searchUsers(false, 0, "", dialog);
                    }
                    else
                    {
                        radius = Integer.parseInt(txtRadiusSD.getText().toString().trim());
                        searchUsers(true, radius, "", dialog);
                    }

                } else if (radioCourtSD.isChecked())
                {
                    ArrayList<String> types = new ArrayList<>();
                    boolean searchTypes;

                    if (checkBasketballSD.isChecked())
                        types.add("Basketball");

                    if (checkSoccerSD.isChecked())
                        types.add("Soccer");

                    if (checkTennisSD.isChecked())
                        types.add("Tennis");

                    if (checkOtherSD.isChecked())
                        types.add("Other");

                    if (types.isEmpty())
                        searchTypes = false;
                    else
                        searchTypes = true;

                    if (txtRadiusSD.getText().toString().trim().equals(""))
                    {
                        searchCourts(searchTypes, types, false, 0,"", dialog);
                    } else
                    {
                        radius = Integer.parseInt(txtRadiusSD.getText().toString().trim());
                        searchCourts(searchTypes, types, true, radius,"", dialog);
                    }

                }
            }
        });



        btnCloseSD.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
    }


    protected void searchUsers(boolean searchRadius, int radius, String name, final AlertDialog dialog)
    {
        Query showUserSearch;
        GeoPoint northEast = getNewGeoPoint(userModel.getGeo_point(),radius,radius);
        GeoPoint southWest = getNewGeoPoint(userModel.getGeo_point(),-radius,-radius);

        if(searchRadius)
        {
            showUserSearch = mDatabase.collection("users").orderBy("geo_point")
                    .whereLessThanOrEqualTo("geo_point",northEast)
                    .whereGreaterThanOrEqualTo("geo_point",southWest);
        }
        else
        {
            showUserSearch = mDatabase.collection("users").orderBy("username")
                    .startAt(name).endAt(name + "\uf8ff");
        }

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(showUserSearch, UserModel.class)
                .build();


        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<UserModel, UserSearchViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final UserSearchViewHolder holder, int position, @NonNull final UserModel model)
            {
                holder.username.setText(model.getUsername());
                Glide.with(IndexActivity.this)
                        .load(model.getProfile_image())
                        .placeholder(R.drawable.user)
                        .into(holder.profileImage);

                holder.btnPinpoint.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setUserCameraView(model);
                        dialog.dismiss();
                    }
                });
            }

            @NonNull
            @Override
            public UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_rcv,parent,false);
                UserSearchViewHolder viewHolder = new UserSearchViewHolder(view);
                return viewHolder;
            }
        };

        rcvSearchResults.setAdapter(adapter);
        adapter.startListening();
    }

    public static class UserSearchViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        CircleImageView profileImage;
        ImageButton btnPinpoint;

        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.txtUsernameSURCV);
            profileImage = itemView.findViewById(R.id.imgSURCV);
            btnPinpoint = itemView.findViewById(R.id.btnPinpointSURCV);
        }
    }

    private static GeoPoint getNewGeoPoint(GeoPoint sourcePosition, double mEastWest, double mNorthSouth)
    {
        double degreesPerMeterForLat = EarthCircumFence/360.0;
        double shrinkFactor = Math.cos((sourcePosition.getLatitude()*Math.PI/180));
        double degreesPerMeterForLon = degreesPerMeterForLat * shrinkFactor;
        double newLat = sourcePosition.getLatitude() + mNorthSouth * (1/degreesPerMeterForLat);
        double newLng = sourcePosition.getLongitude() + mEastWest * (1/degreesPerMeterForLon);
        return new GeoPoint(newLat, newLng);
    }

    protected void searchCourts(boolean searchTypes, ArrayList<String> types, boolean searchRadius, int radius, String name, final AlertDialog dialog)
    {
        Query showCourtSearch;
        GeoPoint northEast = getNewGeoPoint(userModel.getGeo_point(),radius,radius);
        GeoPoint southWest = getNewGeoPoint(userModel.getGeo_point(),-radius,-radius);

        if(searchRadius)
        {

            if(searchTypes)
                showCourtSearch = mDatabase.collection("courts").orderBy("location")
                        .whereLessThanOrEqualTo("location",northEast)
                        .whereGreaterThanOrEqualTo("location",southWest)
                        .whereIn("type",types);
            else
                showCourtSearch = mDatabase.collection("courts").orderBy("location")
                        .whereLessThanOrEqualTo("location",northEast)
                        .whereGreaterThanOrEqualTo("location",southWest);

        }
        else
        {
            if(searchTypes)
                showCourtSearch = mDatabase.collection("courts").orderBy("name")
                        .startAt(name).endAt(name + "\uf8ff").whereIn("type",types);
            else
                showCourtSearch = mDatabase.collection("courts").orderBy("name")
                        .startAt(name).endAt(name + "\uf8ff");
        }

        FirestoreRecyclerOptions<CourtModel> options = new FirestoreRecyclerOptions.Builder<CourtModel>()
                .setQuery(showCourtSearch, CourtModel.class)
                .build();


        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<CourtModel, CourtSearchViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull CourtSearchViewHolder holder, int position, @NonNull final CourtModel model)
            {
                holder.name.setText(model.getName());
                Glide.with(IndexActivity.this)
                        .load(model.getPicture())
                        .placeholder(R.drawable.user)
                        .into(holder.courtImage);
                holder.type.setText(model.getType());

                holder.btnPinpoint.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setCourtCameraView(model);
                        dialog.dismiss();
                    }
                });
            }

            @NonNull
            @Override
            public CourtSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_court_rcv,parent,false);
                CourtSearchViewHolder viewHolder = new CourtSearchViewHolder(view);
                return viewHolder;
            }
        };

        rcvSearchResults.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CourtSearchViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        TextView type;
        CircleImageView courtImage;
        ImageButton btnPinpoint;

        public CourtSearchViewHolder(View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.txtCourtNameSCRCV);
            type = itemView.findViewById(R.id.txtCourtTypeSCRCV);
            courtImage = itemView.findViewById(R.id.imgSCRCV);
            btnPinpoint = itemView.findViewById(R.id.btnPinpointSCRCV);
        }
    }
    //endregion

    //////////////////////////////////// FILTER DIALOG PART //////////////////////////////////////////////

    //region FILTER DIALOG
    protected void showFilterDialog()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.filter_dialog, null);

        Switch switchPersonFD = mView.findViewById(R.id.switchPersonFD);
        final Switch switchCourtFD = mView.findViewById(R.id.switchCourtFD);
        final Switch switchBasketballFD = mView.findViewById(R.id.switchBasketballFD);
        final Switch switchSoccerFD = mView.findViewById(R.id.switchSoccerFD);
        final Switch switchTennisFD = mView.findViewById(R.id.switchTennisFD);
        final Switch switchOtherFD = mView.findViewById(R.id.switchOtherFD);
        Button btnApplyFD = mView.findViewById(R.id.btnApplyFD);

        if(personFilter)
            switchPersonFD.setChecked(true);
        else
            switchPersonFD.setChecked(false);

        if(courtFilter)
            switchCourtFD.setChecked(true);
        else
            switchCourtFD.setChecked(false);

        if(basketballFilter)
            switchBasketballFD.setChecked(true);
        else
            switchBasketballFD.setChecked(false);

        if(soccerFilter)
            switchSoccerFD.setChecked(true);
        else
            switchSoccerFD.setChecked(false);

        if(tennisFilter)
            switchTennisFD.setChecked(true);
        else
            switchTennisFD.setChecked(false);

        if(otherFilter)
            switchOtherFD.setChecked(true);
        else
            switchOtherFD.setChecked(false);

        switchPersonFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    personFilter = true;
                else
                    personFilter = false;
            }
        });

        switchCourtFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    courtFilter = true;
                    switchBasketballFD.setClickable(true);
                    switchBasketballFD.setChecked(true);
                    switchSoccerFD.setClickable(true);
                    switchSoccerFD.setChecked(true);
                    switchTennisFD.setClickable(true);
                    switchTennisFD.setChecked(true);
                    switchOtherFD.setClickable(true);
                    switchOtherFD.setChecked(true);
                }
                else
                {
                    courtFilter = false;
                    switchBasketballFD.setClickable(false);
                    switchBasketballFD.setChecked(false);
                    switchSoccerFD.setClickable(false);
                    switchSoccerFD.setChecked(false);
                    switchTennisFD.setClickable(false);
                    switchTennisFD.setChecked(false);
                    switchOtherFD.setClickable(false);
                    switchOtherFD.setChecked(false);
                }
            }
        });

        switchBasketballFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if(isChecked)
                basketballFilter = true;
            else
                basketballFilter = false;
        }
    });

        switchSoccerFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    soccerFilter = true;
                else
                    soccerFilter = false;
            }
        });

        switchTennisFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    tennisFilter = true;
                else
                    tennisFilter = false;
            }
        });

        switchOtherFD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    otherFilter = true;
                else
                    otherFilter = false;
            }
        });


        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();


        btnApplyFD.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mClusterManager.clearItems();
                mClusterMarkers.clear();

                addMyMapMarker();

                if(personFilter)
                    getFriendsLocations();

                if(courtFilter)
                    getCourtLocations();

                dialog.dismiss();
            }
        });
    }
    //endregion


    //////////////////////////////////// BACK BUTTON PART //////////////////////////////////////////////

    //region BACK BUTTON
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
    //endregion

    //////////////////////////////////// LOCATION SERVICE PART //////////////////////////////////////////////

    //region LOCATION SERVICE
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
                retrieveCourtLocations();
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
                                        if(!updatedUserLocation.getUserID().equals(userModel.getUserID()))
                                        {
                                            Location userLocation = new Location("userLocation");
                                            userLocation.setLatitude(userModel.getGeo_point().getLatitude());
                                            userLocation.setLongitude(userModel.getGeo_point().getLongitude());

                                            Location otherLocation = new Location("otherLocation");
                                            otherLocation.setLatitude(updatedUserLocation.getGeo_point().getLatitude());
                                            otherLocation.setLongitude(updatedUserLocation.getGeo_point().getLongitude());

                                            float distance = userLocation.distanceTo(otherLocation);

                                            if(!notifiedAbout.contains(updatedUserLocation.getUserID()) && distance < 100.0)
                                            {
                                                notifiedAbout.add(updatedUserLocation.getUserID());
                                                sendNotification(updatedUserLocation.getUsername());
                                            }
                                            else if(notifiedAbout.contains(updatedUserLocation.getUserID()) && distance > 100.0)
                                                notifiedAbout.remove(updatedUserLocation.getUserID());
                                        }

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


    private void retrieveCourtLocations()
    {

        try
        {
            for(final ClusterMarker clusterMarker: mClusterMarkers)
            {

                DocumentReference courtLocationRef = mDatabase.collection("courts").document(clusterMarker.getId());

                courtLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {

                            final CourtModel updatedCourtLocation = task.getResult().toObject(CourtModel.class);

                            for (int i = 0; i < mClusterMarkers.size(); i++)
                            {
                                try
                                {
                                    if (mClusterMarkers.get(i).getId().equals(updatedCourtLocation.getCourtID()))
                                    {

                                        Location userLocation = new Location("userLocation");
                                        userLocation.setLatitude(userModel.getGeo_point().getLatitude());
                                        userLocation.setLongitude(userModel.getGeo_point().getLongitude());

                                        Location otherLocation = new Location("otherLocation");
                                        otherLocation.setLatitude(updatedCourtLocation.getLocation().getLatitude());
                                        otherLocation.setLongitude(updatedCourtLocation.getLocation().getLongitude());

                                        float distance = userLocation.distanceTo(otherLocation);


                                        if(!notifiedAbout.contains(updatedCourtLocation.getCourtID()) && distance < 100.0)
                                        {
                                            notifiedAbout.add(updatedCourtLocation.getCourtID());
                                            sendNotification(updatedCourtLocation.getName());
                                        }
                                        else if(notifiedAbout.contains(updatedCourtLocation.getCourtID()) && distance > 100.0)
                                            notifiedAbout.remove(updatedCourtLocation.getCourtID());

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
    //endregion

    //////////////////////////////////// NOTIFICATION PART //////////////////////////////////////////////


    //region NOTIFICATION
    protected void sendNotification(String name)
    {
        Notification.Builder builder = new Notification.Builder(this)
                                        .setSmallIcon(R.mipmap.launcher_icon)
                                        .setContentTitle(name)
                                        .setContentText("Is near you.");
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,IndexActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(),notification);
    }
    //endregion
}
