package com.team17.gsbts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference ref;
    private DatabaseReference refLat;
    private DatabaseReference refLon;
    private FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private ListView offBoardLV;
    private ListView onBoardLV;
    private Adapter nBoard;
    private Adapter fBoard;

    private String routeNumber;
    private String driverID;
    private ArrayList<String> stops;
    private Map<String, ArrayList<String>> sIDall;
    private ArrayList<String> sIDcurrent;
    private ArrayList<String> sIDoffBoard;
    private ArrayList<String> sOffNames;
    private ArrayList<String> sOnNames;
    private Double originLat;
    private Double originLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Bundle bundle = this.getIntent().getExtras();

        if(bundle != null) {
            routeNumber = bundle.getString("routeNum");
            driverID = bundle.getString("driverID");
            stops = (ArrayList<String>) bundle.getSerializable("allStops");
        }

        db = FirebaseDatabase.getInstance();

        refLat = db.getReference("Bus/" + routeNumber + "/currentLocation/Latitude");
        refLon = db.getReference("Bus/" + routeNumber + "/currentLocation/Longitude");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }else{
                checkLocationPermission();
            }
        }

        sIDall = new LinkedHashMap<>();
        sIDcurrent = new ArrayList<>();
        sIDoffBoard = new ArrayList<>();
        sOffNames = new ArrayList<>();
        sOnNames = new ArrayList<>();
        onBoardLV = findViewById(R.id.onBoardList);
        offBoardLV = findViewById(R.id.offBoardList);

        fBoard = new ArrayAdapter<>(RouteActivity.this, android.R.layout.simple_list_item_1, sOffNames);
        offBoardLV.setAdapter((ArrayAdapter) fBoard);

        nBoard = new ArrayAdapter<>(RouteActivity.this, android.R.layout.simple_list_item_1, sOnNames);
        onBoardLV.setAdapter((ArrayAdapter) nBoard);

        ref = db.getReference("Stops/1/StopInfo");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                originLat = (Double) dataSnapshot.child("Latitude").getValue();
                originLon = (Double) dataSnapshot.child("Longitude").getValue();
                Log.d("OrigLat", originLat.toString());
                Log.d("OrigLon", originLon.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref = db.getReference("Stops");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("firstRef", "CALLED");
                sIDall.clear();
                sIDcurrent.clear();
                sIDoffBoard.clear();
                sOffNames.clear();
                sOnNames.clear();

                initialStudents();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    public void initialStudents()
    {
        for(int i = 0; i < stops.size(); i++)
        {
            ref = db.getReference("Stops/" + stops.get(i) + "/Students");

            sIDall.put(stops.get(i), new ArrayList<String>());

            final int z = i;

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("initialStudents", "CALLED");
                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                        String t = postSnapshot.getKey();
                        sIDall.get(stops.get(z)).add(t);
                        Log.d("Input Student", t);
                    }
                    if(z == stops.size() - 1)
                    {
                        getStudents();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: ", databaseError.getMessage());
                }
            });
        }
    }

    public void getStudents()
    {
        final ArrayList<String> tmp = new ArrayList<>(sIDall.keySet());

        Log.d("Stops Size", String.valueOf(sIDall.size()));

        for(int i = 0; i < sIDall.size(); i++)
        {
            Log.d("All Students Size", String.valueOf(sIDall.get(tmp.get(i)).size()));

            for(int x = 0; x < sIDall.get(tmp.get(i)).size(); x++)
            {
                ref = db.getReference("Stops/" + tmp.get(i) + "/Students/" + sIDall.get(tmp.get(i)).get(x));

                final int z = x;
                final int c = i;

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("getStudents", "CALLED");

                        if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12) // Morning
                        {
                            boolean morning = (Boolean) dataSnapshot.child("morning").getValue();
                            boolean MonBoard = (Boolean) dataSnapshot.child("onBoard").getValue();

                            if(morning == true && MonBoard == false)
                            {
                                sIDoffBoard.add(dataSnapshot.getKey());
                                Log.d("Off Morning Student", dataSnapshot.getKey());
                            }
                            else if(morning == true && MonBoard == true)
                            {
                                sIDcurrent.add(dataSnapshot.getKey());
                                Log.d("On Morning Student", dataSnapshot.getKey());
                            }
                        }
                        else if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 12) // Afternoon
                        {
                            boolean afternoon = (Boolean) dataSnapshot.child("afternoon").getValue();
                            boolean AonBoard = (Boolean) dataSnapshot.child("onBoard").getValue();

                            if(afternoon == true && AonBoard == false)
                            {
                                sIDoffBoard.add(dataSnapshot.getKey());
                                Log.d("Off Afternoon Student", dataSnapshot.getKey());
                            }
                            else if(afternoon == true && AonBoard == true)
                            {
                                sIDcurrent.add(dataSnapshot.getKey());
                                Log.d("On Afternoon Student", dataSnapshot.getKey());
                            }

                        }

                        if(c == sIDall.size() - 1 && z == sIDall.get(tmp.get(c)).size() - 1)
                        {
                            getOffNames();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("The read failed: ", databaseError.getMessage());
                    }
                });
            }
        }
    }

    public void getOffNames()
    {
        Log.d("Current Students Size", String.valueOf(sIDoffBoard.size()));
        for(int i = 0; i < sIDoffBoard.size(); i++)
        {
            ref = db.getReference("Students/" + sIDoffBoard.get(i));

            final int z = i;
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("getOffNames", "CALLED");

                    String fName = (String) dataSnapshot.child("fName").getValue();
                    String lName = (String) dataSnapshot.child("lName").getValue();
                    String fullName = fName + " " + lName;
                    sOffNames.add(fullName);
                    Log.d("Student Name", fullName);

                    if(z == sIDoffBoard.size() - 1)
                    {
                        ((ArrayAdapter) fBoard).notifyDataSetChanged();
                        getOnNames();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: ", databaseError.getMessage());
                }
            });
        }
    }

    public void getOnNames()
    {
        Log.d("Current Students Size", String.valueOf(sIDcurrent.size()));
        for(int i = 0; i < sIDcurrent.size(); i++)
        {
            ref = db.getReference("Students/" + sIDcurrent.get(i));

            final int z = i;
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("getOnNames", "CALLED");

                    String fName = (String) dataSnapshot.child("fName").getValue();
                    String lName = (String) dataSnapshot.child("lName").getValue();
                    String fullName = fName + " " + lName;
                    sOnNames.add(fullName);
                    Log.d("Student Name", fullName);

                    if(z == sIDcurrent.size() - 1)
                    {
                        ((ArrayAdapter) nBoard).notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: ", databaseError.getMessage());
                }
            });
        }
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    if(location != null)
                    {
                        refLat.setValue(location.getLatitude());
                        refLon.setValue(location.getLongitude());
                        Log.d("LatLng", String.valueOf(latLng));
                    }
                }
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        refLat.setValue(originLat);
        refLon.setValue(originLon);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
