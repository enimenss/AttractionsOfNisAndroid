package com.example.a38162.attractionsofnis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    ListView listView;
    String nameUser;
    String surnameUser;
    String idUser;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        nameUser = intent.getStringExtra("Name");
        surnameUser = intent.getStringExtra("Surname");
        idUser = intent.getStringExtra("UserId");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.addListenerForSingleValueEvent(valueEventListener);

        //User user = UsersData.getInstance().getUser(Integer.parseInt(idUser));

        listView = (ListView) findViewById(R.id.listFriendList);
        List<String> flist = new ArrayList<String>();
        //for (int i = 1; i < user.friends.size(); i++) {
           // flist.add(user.friends.get(i));
        //}
        flist.add("Lazar");
        flist.add("Danica");
        FriendListViewAdapter adapter = new FriendListViewAdapter(flist, getApplicationContext());
        listView.setAdapter(adapter);

        /*ListView lview = (ListView) findViewById(R.id.listFriendList);
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // User currentUser = UsersData.getInstance().getCurrentUser();
                /*List<String> friends = user.friends;
                String friend = friends.get(position + 1);
                int userid = UsersData.getInstance().usersKeyIndexMapping.get(friend);
                User user = UsersData.getInstance().getUser(userid);
*/
               /* Intent data = new Intent(FriendListActivity.this, ProfileActivity.class);
                data.putExtra("firstName", user.firstName);
                data.putExtra("lastName", user.lastName);
                data.putExtra("email", user.email);
                data.putExtra("checkins", user.checkins);
                data.putExtra("imageURL", user.imageURL);
                data.putExtra("key", user.key);

                startActivity(data);

            }
        });*/
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<User> users = null;

            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }
            }

            for (User u : users) {
                if (u.name == nameUser && u.surname == surnameUser) {
                    user = u;
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
}
