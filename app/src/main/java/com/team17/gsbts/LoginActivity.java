package com.team17.gsbts;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText jEmail, jPassword;
    private Button jLogin;
    private TextView jForgot;
    private ProgressBar progressBar;

    private FirebaseAuth jAuth;
    private FirebaseAuth.AuthStateListener AuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        jAuth = FirebaseAuth.getInstance();
        AuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null)
                {
                    final String u_id = user.getUid();

                    DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference("Users/Parents");
                    DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("Users/Drivers");

                    parentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(u_id))
                            {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
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
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
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
            }
        };

        jEmail = (EditText) findViewById(R.id.email);
        jPassword = (EditText) findViewById(R.id.password);
        jLogin = (Button) findViewById(R.id.login);
        jForgot = (TextView) findViewById(R.id.forgot);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        jLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = jEmail.getText().toString();
                final String password = jPassword.getText().toString();

                if(email.isEmpty())
                {
                    jEmail.setError("Email is required!");
                    jEmail.requestFocus();
                    return;
                }

                if(password.isEmpty())
                {
                    jPassword.setError("Password is required!");
                    jPassword.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                jAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful())
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        jForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_reset, null);
                final EditText rEmail = (EditText) mView.findViewById(R.id.resetEmail);
                Button rReset = (Button) mView.findViewById(R.id.resetPassword);

                rReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        jAuth.sendPasswordResetEmail(rEmail.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(LoginActivity.this, "Password rest link sent to " + rEmail.getText().toString(), Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                });

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        jAuth.addAuthStateListener(AuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        jAuth.removeAuthStateListener(AuthListener);
    }
}
