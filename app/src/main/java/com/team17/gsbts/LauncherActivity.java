package com.team17.gsbts;

import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_launcher);

        FirebaseApp.initializeApp(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            final String u_id = user.getUid();

            DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference("Users/Parents");
            DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("Users/Drivers");

            parentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(u_id))
                    {
                        Intent intent = new Intent(LauncherActivity.this, ParentActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(u_id))
                    {
                        Intent intent = new Intent(LauncherActivity.this, DriverActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }
}
