package com.team17.gsbts;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team17.gsbts.Services.MyHttpURLConnection;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<ModelFeed> modelFeedArrayList = new ArrayList<>();
    AdapterFeed adapterFeed;

    private FirebaseUser currentUser;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private String uid;
    private Map<String, ArrayList<String>> historyIds;
    private ArrayList<History> historyInfo;

    private MyHttpURLConnection myConnection = new MyHttpURLConnection();


    public FeedFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private class History {
        private String latitude;
        private String longitude;
        private String time;
        private String boardStatus;
        private String fName;
        private String address;
        private String message;
        private String messageTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        private LocalDateTime dateTime;
        private boolean checked;

        public History(){
            this.latitude = "";
            this.longitude = "";
            this.time = "";
            this.boardStatus = "";
            this.fName = "";
            this.address = "";
            this.message = "";
            this.messageTime = "";
            this.checked = false;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            String temp1 = time.substring(0, 10);
            String temp2 = time.substring(11, 16);

            messageTime = temp2 + "\n" + temp1;

            dateTime = LocalDateTime.parse(time, formatter);

            Log.d("Feed", "messageTime: " + messageTime);

            this.time = time;
        }

        public String getBoardStatus() {
            return boardStatus;
        }

        public void setBoardStatus(String boardStatus) {
            this.boardStatus = boardStatus;
        }

        public String getfName() {
            return fName;
        }

        public void setfName(String fName) {
            this.fName = fName;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress(){
            return address;
        }

        public String getMessage() {
            message = fName + " got " + boardStatus + " the bus at " + address + ".";

            return message;
        }

        public String getMessageTime() {
            return messageTime;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked() {
            this.checked = true;
        }

        public void setChecked(boolean check) {
            this.checked = check;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void populateRecyclerView() {

        modelFeedArrayList.clear();

        ArrayList<Integer> sorted = new ArrayList<Integer>();
        Integer x = 0;
        History temp = new History();
        //temp.setTime(historyInfo.get(0).getTime());

        for(int i = 0; i < historyInfo.size(); i++)
            historyInfo.get(i).setChecked(false);

        for(int i = 0; i < historyInfo.size(); i++){
            temp.setTime("1970-01-01 05:09");
            for(int j = 0; j < historyInfo.size(); j++){
                if (historyInfo.get(j).getDateTime().isAfter(temp.getDateTime()) && !historyInfo.get(j).isChecked()){
                    x = j;
                    temp.setTime(historyInfo.get(j).getTime());
                }
            }
            historyInfo.get(x).setChecked();
            sorted.add(x);
        }
        Log.d("Feed", "Sort: ");
        for(int i = 0; i < sorted.size(); i++)
            Log.d("Feed", sorted.get(i).toString());


        Log.d("Feed","historyInfo.size(): " + historyInfo.size());
        ModelFeed modelFeed;
        for (int i = 0; i < sorted.size(); i++){
            modelFeed = new ModelFeed(historyInfo.get(sorted.get(i)).getMessageTime(), historyInfo.get(sorted.get(i)).getMessage());
            modelFeedArrayList.add(modelFeed);
        }

        adapterFeed.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        uid = currentUser.getUid();

        historyIds = new LinkedHashMap<>();
        historyInfo = new ArrayList<>();

        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Users/Parents/" + uid + "/Students");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyIds.clear();
                historyInfo.clear();
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    //Log.d("Feed", "postSnapshot.getKey(): " + postSnapshot.getKey());
                    historyIds.put(postSnapshot.getKey(), new ArrayList<String>());
                }

                final ArrayList<String> tmp = new ArrayList<>(historyIds.keySet());
                //Log.d("Feed", "historyIds.size(): " + historyIds.size());

                for(int i = 0; i < historyIds.size(); i++) {
                    getHistoryID(tmp.get(i));
                }
                //populateRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("onCancelled", "Failed to read value.", error.toException());
            }
        });


        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterFeed = new AdapterFeed(getActivity(), modelFeedArrayList);
        recyclerView.setAdapter(adapterFeed);

        //populateRecyclerView();

        return rootView;
    }

    public void getHistoryID(final String id)
    {
        ref = db.getReference("Students/" + id + "/History");
        //Log.d("Feed","getHistoryID: " + id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    //Log.d("Feed", "getHistoryID(" + id + "): postSnapshot.getKey(): " + postSnapshot.getKey());
                    historyIds.get(id).add(postSnapshot.getKey());
                }

                final ArrayList<String> tmp = new ArrayList<>(historyIds.keySet());
                //Log.d("Feed", "historyIds.size(): " + historyIds.size());

                for(int i = 0; i < historyIds.size(); i++) {
                    for(int j = 0; j < historyIds.get(tmp.get(i)).size(); j++) {
                        //Log.d("Feed", "historyIds(" + tmp.get(i) + ").get(" + j + ") = " + historyIds.get(tmp.get(i)).get(j));
                        getHistoryInfo(historyIds.get(tmp.get(i)).get(j));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("onCancelled", "Failed to read value.", error.toException());
            }
        });
    }

    public void getHistoryInfo(final String id)
    {
        ref = db.getReference("History/" + id);
        //Log.d("Feed","getHistoryID: " + id);

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                History temp = new History();

                temp.setLatitude(dataSnapshot.child("Latitude").getValue().toString());
                temp.setLongitude((String) dataSnapshot.child("Longitude").getValue().toString());
                temp.setTime(dataSnapshot.child("Time").getValue().toString());
                temp.setBoardStatus(dataSnapshot.child("boardStatus").getValue().toString());
                temp.setfName(dataSnapshot.child("fName").getValue().toString());

                String temp1 = "";

                try {
                    temp1 = myConnection.sendGet(temp.getLatitude(), temp.getLongitude());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Log.d("Feed","Address: " + temp1);
                temp.setAddress(temp1);

                Log.d("Feed","ADD: " + temp.getMessage());
                historyInfo.add(temp);
                Log.d("Feed","ADD: #" + historyInfo.size());

                populateRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("onCancelled", "Failed to read value.", error.toException());
            }
        });
    }

}
