package com.example.a38162.attractionsofnis;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<Place> places;
    String nameUser, surnameUser, idUser;
    FirebaseAuth mAuth;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
        }
        else if(id == R.id.map) {
            Intent i = new Intent(this,  MapActivity.class);
            startActivity(i);
        }
        else if(id == R.id.ranking)
        {
            Intent i = new Intent(this,  RankUserActivity.class);
            startActivity(i);
        }
        else if(id == R.id.settings) {
            Intent i = new Intent(this,  SettingsActivity.class);
            startActivity(i);
        }
        else if(id == R.id.logOut) {
            FirebaseDatabase.getInstance().getReference().child("users").
                    child(mAuth.getCurrentUser().getUid()).child("visable").setValue("0");
            Intent i = new Intent(this,  LoginActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        places = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        ((TextView) findViewById(R.id.textView)).setText("Welcome, what do you want to explore? You can pick more than one:");

        Button button = (Button) findViewById(R.id.button_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean landmarks = ((CheckBox) findViewById(R.id.checkBox_landmarks)).isChecked();
                final boolean museums = ((CheckBox) findViewById(R.id.checkBox_museums)).isChecked();
                final boolean restaurants = ((CheckBox) findViewById(R.id.checkBox_night_life)).isChecked();
                final boolean other = ((CheckBox) findViewById(R.id.checkBox_interesting)).isChecked();

                if (!(landmarks || museums || restaurants || other)) {
                    Toast.makeText(MainActivity.this, "You have to check something!", Toast.LENGTH_LONG).show();
                    return;
                }

                    FirebaseDatabase firebaseDatabase = null;
                    final DatabaseReference databaseReference, databaseReferencePlaces;
                    databaseReference = firebaseDatabase.getInstance().getReference().child("scores");
                    databaseReferencePlaces = firebaseDatabase.getInstance().getReference().child("places");

                    firebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.e("Score", "usao u fju");
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String scored = snapshot.getValue(String.class);
                                    if (scored.equals("no")) {
                                        Log.e("Score", "usao u fju za deo no");
                                        databaseReferencePlaces.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                idUser = mAuth.getCurrentUser().getUid();
                                                places.clear();
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Place place = snapshot.getValue(Place.class);
                                                        places.add(place);
                                                    }
                                                }
                                                Log.e("Score", "dodao je " + places.size() + " mesta");

                                                for (Place place : places) {
                                                    if (landmarks) {
                                                        if (place.category.equals("landmark")) {
                                                            Log.e("Score", "Pronasao je landmark, treba da doda u bazu score");
                                                            String id = databaseReference.push().getKey();
                                                            Score score = new Score(id, idUser, place.placeId);
                                                            databaseReference.child(id).setValue(score);
                                                            Log.e("Score", "Score dodat");
                                                        }
                                                    } else if (museums) {
                                                        if (place.category.equals("museum")) {
                                                            String id = databaseReference.push().getKey();
                                                            Score score = new Score(id, idUser, place.placeId);
                                                            databaseReference.child(id).setValue(score);
                                                        }
                                                    } else if (restaurants) {
                                                        if (place.category.equals("restaurant")) {
                                                            String id = databaseReference.push().getKey();
                                                            Score score = new Score(id, idUser, place.placeId);
                                                            databaseReference.child(id).setValue(score);
                                                        }
                                                    } else if (other) {
                                                        if (place.category.equals("other")) {
                                                            String id = databaseReference.push().getKey();
                                                            Score score = new Score(id, idUser, place.placeId);
                                                            databaseReference.child(id).setValue(score);
                                                        }
                                                    }
                                                }
                                                FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("scoringPlaces").setValue("yes");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
