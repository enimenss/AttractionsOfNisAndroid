package com.example.a38162.attractionsofnis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RankUserActivity extends AppCompatActivity {
    String loginUserId;
    ArrayList<User> users;
    FirebaseAuth mAuth;
    Map<String, String> friends;
    Set<String> friendsIDs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        users = new ArrayList<>();
        setContentView(R.layout.activity_rank_user);
        loginUserId = mAuth.getCurrentUser().getUid();

        friendsIDs = new android.support.v4.util.ArraySet<>();
        friends = new HashMap<>();

        Ranking();
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
            Intent intent = new Intent(RankUserActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.map) {
            Intent i = new Intent(this,  MapActivity.class);
            startActivity(i);
        }
        else if(id == R.id.ranking)
        {  }
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

    private void Ranking() {
        FirebaseDatabase firebaseDatabase = null;
        DatabaseReference databaseReference, databaseReferenceFriends;
        databaseReference = firebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        users.add(user);
                    }
                }

                for (int i = 0; i < users.size() - 1; i++) {
                    for (int j = i; j < users.size(); j++) {
                        if (Integer.parseInt(users.get(j).getScore()) > Integer.parseInt(users.get(i).getScore())) {
                            User user = users.get(i);
                            users.set(i, users.get(j));
                            users.set(j, user);
                        }
                    }
                }

                ListView listViewPlayers = (ListView) findViewById(R.id.list_rankPlayers);
                List<String> array = new ArrayList<>();
                for (User user : users) {
                    String string = user.getName() + " " + user.getSurname() + " score: " + user.getScore();
                    array.add(string);
                }

                listViewPlayers.setAdapter(new ArrayAdapter<String>(RankUserActivity.this, android.R.layout.simple_list_item_1, array));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        FirebaseDatabase.getInstance().getReference().child("friends").child(mAuth.getCurrentUser().getUid())
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = (Map<String, String>) dataSnapshot.getValue();
                if (friends != null) {
                    friendsIDs = friends.keySet();
                }
                if(friendsIDs!=null)
                {
                    List<User> list= new ArrayList<>();

                    for(String s : friendsIDs) {
                        for(User u : users) {
                            if(u.getUserId().equals(s)) {
                                list.add(u);
                            }
                        }
                    }

                    for (int i = 0; i < list.size() - 1; i++) {
                        for (int j = i; j < list.size(); j++) {
                            if (Integer.parseInt(list.get(j).getScore()) > Integer.parseInt(users.get(i).getScore())) {
                                User user = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, user);
                            }
                        }
                    }

                    ListView listViewFriends = (ListView) findViewById(R.id.list_rankFriends);
                    List<String> array = new ArrayList<>();
                    for (User user : list) {
                        String string = user.getName() + " " + user.getSurname() + " score: " + user.getScore();
                        array.add(string);
                    }

                    listViewFriends.setAdapter(new ArrayAdapter<String>(RankUserActivity.this, android.R.layout.simple_list_item_1, array));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

//    ValueEventListener valueEventListenerFriends = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            List<Friend> friends = new ArrayList<>();
//            List<User> usersBest = new ArrayList<>();
//            if(dataSnapshot.exists()) {
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Friend friend = snapshot.getValue(Friend.class);
//                    friends.add(friend);
//                }
//            }
//            loginUserId = mAuth.getCurrentUser().getUid();
//            for (int i=0; i < friends.size()-1; i++) {
//                if(friends.get(i).userId.equals(loginUserId)) {
//                    boolean find = false;
//                    int j = 0;
//                    while(!find) {
//                        if(users.get(j).getUserId().equals(friends.get(i).getFriendId())) {
//                            usersBest.add(users.get(j));
//                            find = true;
//                        } else j++;
//                    }
//                }
//            }
//
//
//            ListView listViewFriends = (ListView) findViewById(R.id.list_rankFriends);
//            List<String> array = new ArrayList<>();
//            for(User user : usersBest) {
//                String string = user.getName() + " " + user.getSurname() + " score: " + user.getScore();
//                array.add(string);
//            }
//
//            listViewFriends.setAdapter(new ArrayAdapter<String>(RankUserActivity.this, android.R.layout.simple_list_item_1, array));
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//        }
//    };
    }
}
