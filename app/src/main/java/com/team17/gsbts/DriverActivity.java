package com.team17.gsbts;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DriverActivity extends AppCompatActivity {

    private ListView lBus;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser currentUser;
    private Button dLogOut;

    private Map<String, ArrayList<String>> routes;
    private ArrayList<Integer> studentCount;
    private List<HashMap<String, String>> aList = new ArrayList<>();

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        routes = new LinkedHashMap<>();
        studentCount = new ArrayList<>();

        dLogOut = (Button) findViewById(R.id.logOut);

        currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        uid = currentUser.getUid();

        lBus = findViewById(R.id.busList);
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Users/Drivers/" + uid + "/Bus");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentCount.clear();
                routes.clear();
                aList.clear();

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    routes.put(postSnapshot.getKey(), new ArrayList<String>());
                    Log.d("Input Route", postSnapshot.getKey());
                }
                getStops();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });

        dLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(DriverActivity.this, LoginActivity.class));
            }
        });

    }

    public void getStops()
    {
        final ArrayList<String> tmp = new ArrayList<>(routes.keySet());

        for(int i = 0; i < routes.size(); i++)
        {
            ref = db.getReference("Bus/" + tmp.get(i) + "/Stops");

            final int z = i;
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                        String t = postSnapshot.getKey();
                        routes.get(tmp.get(z)).add(t);
                        Log.d("Input Stop", t);
                        Log.d("In Route", tmp.get(z));
                    }
                    if(z == routes.size() - 1)
                    {
                        getCount();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: ", databaseError.getMessage());
                }
            });
        }
    }

    public void getCount()
    {
        final ArrayList<String> tmp = new ArrayList<>(routes.keySet());

        Log.d("Route Size", String.valueOf(routes.size()));

        for(int i = 0; i < routes.size(); i++)
        {
            studentCount.add(0);

            Log.d("Stop Size", String.valueOf(routes.get(tmp.get(i)).size()));

            for(int x = 0; x < routes.get(tmp.get(i)).size(); x++)
            {
                Log.d("Stop Value", String.valueOf(routes.get(tmp.get(i)).get(x)));

                ref = db.getReference("Stops/" + routes.get(tmp.get(i)).get(x) + "/Students");

                final int z = x;
                final int c = i;

                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("Stop Index", String.valueOf(z));
                        Log.d("Count Value", String.valueOf(studentCount.get(c) + (int) dataSnapshot.getChildrenCount()));
                        studentCount.set(c, studentCount.get(c) + (int) dataSnapshot.getChildrenCount());

                        Log.d("Count", String.valueOf(dataSnapshot.getChildrenCount()));

                        if(c == routes.size() - 1 && z == routes.get(tmp.get(c)).size() - 1)
                        {
                            for(int f = 0; f < routes.size(); f++)
                            {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("listview_busNum", tmp.get(f));
                                hm.put("listview_stuCount", String.valueOf(studentCount.get(f)));
                                aList.add(hm);
                            }

                            String[] from = {"listview_busNum", "listview_stuCount"};
                            int[] to = {R.id.busNum, R.id.stuCount};

                            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_bus, from, to);
                            lBus.setAdapter(adapter);

                            lBus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    TextView rN = view.findViewById(R.id.busNum);
                                    String routeNum = rN.getText().toString();

                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("routeNum", routeNum);
                                    bundle.putSerializable("driverID", uid);
                                    bundle.putSerializable("allStops", (Serializable) routes.get(routeNum));

                                    Intent activityChangeIntent = new Intent(DriverActivity.this, RouteActivity.class);
                                    activityChangeIntent.putExtras(bundle);

                                    DriverActivity.this.startActivity(activityChangeIntent);
                                }
                            });
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

}
