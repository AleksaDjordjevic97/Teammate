package com.aleksadjordjevic.teammate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity
{
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int ERROR_DIALOG_REQUEST = 3;
    boolean mLocationPermissionGranted = false;

    CountDownTimer countDownTimer;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;
    DocumentReference profileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (checkMapServices())
        {
            getLocationPermission();

            if(mLocationPermissionGranted)
                startTimer();


        }
    }

    protected void setUser()
    {
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();
        profileRef = mDatabase.collection("users").document(userID);

        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    UserModel u = task.getResult().toObject(UserModel.class);
                    ((UserClient)(getApplicationContext())).setUser(u);
                    Intent nextActivity = new Intent(getApplicationContext(), IndexActivity.class);
                    startActivity(nextActivity);
                }
            }
        });
    }

    private boolean checkMapServices()
    {
        if(isServicesOK())
            if(isMapsEnabled())
                return true;

        return false;
    }

    public boolean isServicesOK()
    {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS)
            return true;
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();

        return false;
    }

    public boolean isMapsEnabled()
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
        {
            buildAlertMessageNoGps();
            return false;
        }

        return true;
    }

    private void buildAlertMessageNoGps()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }



    private void getLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationPermissionGranted = true;
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mLocationPermissionGranted = true;
                    startTimer();
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
            {
                if (!mLocationPermissionGranted)
                    getLocationPermission();
            }
        }

    }

    protected void startTimer()
    {
        countDownTimer = new CountDownTimer(2500, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish()
            {
                countDownTimer.cancel();


                if (mAuth.getCurrentUser() != null)
                    setUser();
                else
                {
                    Intent nextActivity = new Intent(getApplicationContext(), Main2Activity.class);
                    startActivity(nextActivity);
                }

            }
        }.start();

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
}
