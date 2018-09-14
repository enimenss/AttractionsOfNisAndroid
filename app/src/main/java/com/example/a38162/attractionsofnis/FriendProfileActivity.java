package com.example.a38162.attractionsofnis;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.Uri;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.ChildEventListener;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Query;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;
        import com.squareup.picasso.Picasso;

        import java.io.File;
        import java.net.URL;

public class FriendProfileActivity extends AppCompatActivity {
FirebaseAuth mAuth;
    String userID;
    //kada na mapi kliknem na prijatelja da mi se prikaze njegov profil
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        Intent friendIntent = getIntent();
        Bundle friendBundle = friendIntent.getExtras();
        mAuth = FirebaseAuth.getInstance();

        userID = friendBundle.getString("friendID","");

        GetFriendInfo();


    }

    private void GetFriendInfo()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("users");


        final Query query = dr.orderByKey().equalTo(userID);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String ss) {
                final User user  = dataSnapshot.getValue(User.class);

                setTitle(user.username);
                TextView name,lastname,phone,email,age,points;

                name = findViewById(R.id.textView1);
                lastname = findViewById(R.id.textView2);
                phone = findViewById(R.id.textView3);
                email = findViewById(R.id.textView4);
                points = findViewById(R.id.textView7);

                name.setText(user.name);
                lastname.setText(user.surname);
                phone.setText(user.phone_number);
                email.setText(user.email);
                points.setText(String.valueOf(user.score));

                ImageView i = findViewById(R.id.imageView);
                Picasso.with(FriendProfileActivity.this)
                        .load(user.picture)
                        .into(i);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

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
            Intent i = new Intent(this,  MainActivity.class);
            startActivity(i);
        }
        else if(id == R.id.settings) {
            Intent i = new Intent(this,  SettingsActivity.class);
            startActivity(i);
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
        else if(id == R.id.logOut) {
            FirebaseDatabase.getInstance().getReference().child("users").
                    child(mAuth.getCurrentUser().getUid()).child("visable").setValue("0");
            Intent i = new Intent(this,  LoginActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
