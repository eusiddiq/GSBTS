package com.team17.gsbts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingFragment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)this).getSupportActionBar().setTitle("Setting");

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor sharedPreferencesEditor;
        //final Context context = getContext();
        private FirebaseUser currentUser;
        private FirebaseDatabase db;
        private DatabaseReference ref;
        private String uid;
        private SwitchPreference notification;
        private SwitchPreference account;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            final Context context = getContext();

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferencesEditor = sharedPreferences.edit();

            notification = (SwitchPreference) findPreference("notification_enable");
            account = (SwitchPreference) findPreference("disable_my_account");
            Log.d("Preferences", "value = " + notification.getKey());

            PreferenceManager.setDefaultValues(context, R.xml.preference, true);

            currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
            uid = currentUser.getUid();

            db = FirebaseDatabase.getInstance();
            ref = db.getReference("Users/Parents/" + uid);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String test = dataSnapshot.child("notification").getValue().toString();
                    Log.d("Preferences","notificationFire = " + test);
                    sharedPreferencesEditor.putBoolean("notification_enable", test.equalsIgnoreCase("true"));
                    sharedPreferencesEditor.commit();

                    test = dataSnapshot.child("active").getValue().toString();
                    Log.d("Preferences","activeFire = " + test);
                    sharedPreferencesEditor.putBoolean("disable_my_account", test.equalsIgnoreCase("true"));
                    sharedPreferencesEditor.commit();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("onCancelled", "Failed to read value.", error.toException());
                }
            });

            notification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d("Preferences", "onChange: " + newValue.toString());
                    ref.child("notification").setValue(newValue.toString());

                    return true;
                }
            });

            account.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ref.child("active").setValue(newValue.toString());

                    return true;
                }
            });
        }


    }

}
