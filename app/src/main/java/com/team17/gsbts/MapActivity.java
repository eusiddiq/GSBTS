package com.team17.gsbts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team17.gsbts.Services.FirebaseNotificationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;
    private FirebaseDatabase db, busLocationDb;
    private DatabaseReference ref, busLocationRef;
    private FirebaseUser currentUser;
    private String uid;
    private ArrayList<String> studentIds;
    private ArrayList<String> stopNumbers;
    private String currentRoute;
    private int routeColor = 0;
    private int routeImage = 0;
    private Marker busMarker;
    private float markerColor;
    private NotificationManager notificationManager;
    private double homeLatitude;
    private double homeLongitude;
    private double nearLatitude;
    private double nearLongitude;
    private double schoolLatitude;
    private double schoolLongitude;
    private boolean getHomeLocation = false;
    private boolean getSchoolLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ((AppCompatActivity)this).getSupportActionBar().setTitle("Map");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        studentIds = new ArrayList<>();
        stopNumbers = new ArrayList<>();
        busMarker = null;

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ILOVEEMAD",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        uid = currentUser.getUid();
        db = FirebaseDatabase.getInstance();
        busLocationDb = FirebaseDatabase.getInstance();

        getStudentInfo();


        // Send Notification to Server
        /*
        String [] regIds = new String[1];
        regIds[0] = "eICKkPTwuuA:APA91bHWmnoQlG9YCUw8t2vea5tKJKE0YTHsW7dBfmcRCKvvErLdYg5jJeBJW--TntjR8WosOggW-ke-QF3dt6_DkZbWRJyuvFpiBVbFXDnnc-iq_asF97rDEsgi7zhtVS3Gee7Ye-mN";
        JSONArray regArray = null;
        try {
            regArray = new JSONArray(regIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FirebaseNotificationHelper firebaseNotificationHelper = new FirebaseNotificationHelper();
        firebaseNotificationHelper.sendMessage(regArray,"GSBTS","The bus is nearing your child's stop, please get your child ready.","icon","hello");
        */
    }

    private void getStudentInfo() {
        ref = db.getReference("Users/Parents/" + uid + "/Students");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    // get student ID's
                    studentIds.add(postSnapshot.getKey());
                    Log.d("Snapshot", postSnapshot.getKey());
                }
                getRoute();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    private void getRoute() {
        ref = db.getReference("Students/" + studentIds.get(0));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentRoute = String.valueOf(dataSnapshot.child("Route").getValue());
                if(currentRoute.equals("29")) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(36.098,-94.161)));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(13.5f));
                    routeColor = getResources().getColor(R.color.redRouteColor);
                    routeImage = R.drawable.red_stop;
                    markerColor = BitmapDescriptorFactory.HUE_RED;
                }
                else if (currentRoute.equals("42")) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(36.060492,-94.17584)));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
                    routeColor = getResources().getColor(R.color.greenRouteColor);
                    routeImage = R.drawable.green_stop;
                    markerColor = BitmapDescriptorFactory.HUE_GREEN;
                }

                Log.d("Snapshot", currentRoute);
                // use currentRoute to get current bus location
                getStopNumbers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    private void getStopNumbers() {
        ref = db.getReference("Bus/" + currentRoute + "/Stops");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    // get student ID's
                    stopNumbers.add(postSnapshot.getKey());
                    Log.d("Snapshot", postSnapshot.getKey());
                }
                getStopGeoLocation();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });

    }

    private void getStopGeoLocation() {
        ref = db.getReference("Stops");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    if(!getSchoolLocation && postSnapshot.getKey().equals("1")) {
                        Log.d("SnapshotStudent", postSnapshot.getKey());
                        getSchoolLocation = true;
                        schoolLatitude = Double.valueOf(postSnapshot.child("StopInfo/Latitude").getValue().toString());
                        schoolLongitude = Double.valueOf(postSnapshot.child("StopInfo/Longitude").getValue().toString());
                    }

                    if(stopNumbers.contains(postSnapshot.getKey())){
                        double latitude = Double.valueOf(postSnapshot.child("StopInfo/Latitude").getValue().toString());
                        double longitude = Double.valueOf(postSnapshot.child("StopInfo/Longitude").getValue().toString());
                        LatLng stop = new LatLng(latitude, longitude);
                        googleMap.addMarker(new MarkerOptions().position(stop).title(postSnapshot.getKey()).flat(true)).setIcon(BitmapDescriptorFactory.fromResource(routeImage));
                        Log.d("Snapshot", String.valueOf(postSnapshot.child("StopInfo/Longitude").getValue()));
                        Log.d("Snapshot", String.valueOf(postSnapshot.child("StopInfo/Latitude").getValue()));


                        if(!getHomeLocation) {
                            for (DataSnapshot student : postSnapshot.child("Students").getChildren()){
                                if(studentIds.get(0).equals(student.getKey())) {
                                    homeLatitude = latitude;
                                    homeLongitude =longitude;
                                    Log.d("SnapshotStudent", student.getKey());
                                }
                            }
                        }
                    }

                }
                getShapeInfo();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });

    }

    private void getShapeInfo() {
        ref = db.getReference("Bus/" + currentRoute + "/Shape");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int dataCount = 1;
                DataSnapshot firstData = null;
                DataSnapshot lastData = null;
                DataSnapshot previousData = null;
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    if(dataCount == 1) {
                        firstData = postSnapshot;
                    }
                    else if(dataCount == dataSnapshot.getChildrenCount()) {
                        lastData = postSnapshot;
                    }
                    else {
                        previousData.child("Longitude").getValue().toString();
                        Log.d("SnapshotShape", previousData.child("Latitude").getValue().toString());
                        Log.d("SnapshotShape", previousData.child("Longitude").getValue().toString());
                        Log.d("SnapshotShape", postSnapshot.child("Latitude").getValue().toString());
                        Log.d("SnapshotShape", postSnapshot.child("Latitude").getValue().toString());

                        Polyline line = googleMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(Double.parseDouble(previousData.child("Latitude").getValue().toString()), Double.parseDouble(previousData.child("Longitude").getValue().toString())),
                                        new LatLng(Double.parseDouble(postSnapshot.child("Latitude").getValue().toString()), Double.parseDouble(postSnapshot.child("Longitude").getValue().toString())))
                                .color(routeColor));
                    }

                    previousData = postSnapshot;
                    dataCount++;

                }
                googleMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(Double.parseDouble(lastData.child("Latitude").getValue().toString()), Double.parseDouble(lastData.child("Longitude").getValue().toString())),
                                new LatLng(Double.parseDouble(firstData.child("Latitude").getValue().toString()), Double.parseDouble(firstData.child("Longitude").getValue().toString())))
                        .color(getResources().getColor(R.color.busRouteColor)));
                getBusCurrentLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });

    }

    private void getBusCurrentLocation() {

        busLocationRef = busLocationDb.getReference("Bus/" + currentRoute + "/currentLocation");

        busLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                double latitude = Double.valueOf(dataSnapshot.child("Latitude").getValue().toString());
                double longitude = Double.valueOf(dataSnapshot.child("Longitude").getValue().toString());
                Log.d("SnapshotLocationLati", "Test");
                Log.d("SnapshotLocationLati", String.valueOf(latitude));
                Log.d("SnapshotLocationLong", String.valueOf(longitude));
                Log.d("SnapshotLocationLatiH", String.valueOf(homeLatitude));
                Log.d("SnapshotLocationLongH", String.valueOf(homeLongitude));

                if(busMarker != null){
                    busMarker.remove();
                }
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.bus);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                busMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .draggable(true)
                        .title("Union Bus Station")
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                Log.d("SnapshotCurrentLocation",String.valueOf(dataSnapshot.child("Latitude").getValue()));

                // If location is school
                if(latitude == schoolLatitude && longitude == schoolLongitude) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String currentTime = sdf.format(new Date());
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ILOVEEMAD")
                            .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                            .setContentTitle("GSBTS") // title for notification
                            .setContentText("Your child arrived at the school at " + currentTime)// message for notification
                            .setAutoCancel(true); // clear notification after click
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pi);
                    notificationManager.notify(0, mBuilder.build());
                }
                // If location is near the stop
                if(latitude == 1.111 && longitude == 1.1111) {
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ILOVEEMAD")
                            .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                            .setContentTitle("GSBTS") // title for notification
                            .setContentText("Your child's bus is near by, please get your child ready.")// message for notification
                            .setAutoCancel(true); // clear notification after click
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pi);
                    notificationManager.notify(0, mBuilder.build());
                }
                // If location is home
                if(latitude == homeLatitude && longitude == homeLongitude) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String currentTime = sdf.format(new Date());
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ILOVEEMAD")
                            .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                            .setContentTitle("GSBTS") // title for notification
                            .setContentText("Your child arrived at the home at " + currentTime)// message for notification
                            .setAutoCancel(true); // clear notification after click
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pi);
                    notificationManager.notify(0, mBuilder.build());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


}


