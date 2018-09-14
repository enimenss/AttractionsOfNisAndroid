package com.example.a38162.attractionsofnis;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;


public class UsersData {
    private ArrayList<User> users;
    public HashMap<String, Integer> usersKeyIndexMapping;
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private static final String FIREBASE_CHILD = "users";


    private UsersData() {
      users = new ArrayList<>();
      usersKeyIndexMapping = new HashMap<String, Integer>();
      database = FirebaseDatabase.getInstance().getReference();
      database.child(FIREBASE_CHILD).addChildEventListener(chieldEventListener);
      database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
      mAuth = FirebaseAuth.getInstance();
  }

    public User getCurrentUser()
    {
        String fid = mAuth.getCurrentUser().getUid();
        int id = UsersData.getInstance().usersKeyIndexMapping.get(fid);

        return UsersData.getInstance().getUser(id);
    }

  ValueEventListener parentEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
            //users = (ArrayList<User>) dataSnapshot.getValue();
          users.add(dataSnapshot.getValue(User.class));
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
  };

  ChildEventListener chieldEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String userId = dataSnapshot.getKey();

            if(!usersKeyIndexMapping.containsKey(userId)) {
                User user = dataSnapshot.getValue(User.class);
                user.userId = userId;
                users.add(user);
                usersKeyIndexMapping.put(userId, users.size()-1);
            }
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
          String userId = dataSnapshot.getKey();
          User user = dataSnapshot.getValue(User.class);
          user.userId = userId;

          if(usersKeyIndexMapping.containsKey(userId)) {
              int index = usersKeyIndexMapping.get(userId);
              users.set(index, user);
          }
          else {
              users.add(user);
              usersKeyIndexMapping.put(userId, users.size()-1);
          }
      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
          String userId = dataSnapshot.getKey();

          if(usersKeyIndexMapping.containsKey(userId)) {
              int index = usersKeyIndexMapping.get(userId);
              users.remove(index);
              //recreateKeyIndexMapping();
          }
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
  };

  public void AddNewUser(User user) {
      String id = database.push().getKey();
      users.add(user);
      usersKeyIndexMapping.put(id, users.size()-1);
      database.child(FIREBASE_CHILD).child(id).setValue(user);
      user.userId = id;
  }

  private void recreateKeyIndexMapping() {
      usersKeyIndexMapping.clear();
      for (int i=0;i<users.size();i++) {
          usersKeyIndexMapping.put(users.get(i).userId, i);
      }
  };

  ListUpdateEventListener updateEventListener;
  public void setEventListener(ListUpdateEventListener listener) {
      updateEventListener = listener;
  };


  public interface ListUpdateEventListener {
      void onListUpdated();
  };


private static class SingletonHolder
{
    public static final UsersData instance = new UsersData();
}

    public static UsersData getInstance()
    {
        return SingletonHolder.instance;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

//    public ArrayList<User> getSortedUsers() {
//        return sortedUsers;
//    }


    public User getUser(int index)
    {
        return users.get(index);
    }


    /*public void recreateKeyIndexMapping()
    {
        usersKeyIndexMapping.clear();

        for(int i = 0; i < users.size(); i++)
        {
            usersKeyIndexMapping.put(users.get(i).getUserId(), i);
        }
    }*/
    public void updateUser(int index, String firstname, String lastname, String  phone, String lat)
    {
        User user = users.get(index);
        user.name = firstname;
        user.surname = lastname;
        user.phone_number = phone;

        database.child(FIREBASE_CHILD).child(user.userId).setValue(user);

    }


//    public void sortUsers() {
//        sortedUsers.clear();
//        for (int i = 0; i < users.size(); i++) {
//            sortedUsers.add(i,users.get(i));
//        }
//
//        int k;
//        for(int m = users.size(); m>0; m--)
//            for(int i = 0; i<users.size()-1; i++ )
//            {
//                k = i+1;
//                if(sortedUsers.get(i).checkins > sortedUsers.get(k).checkins)
//                {
//                    swap(i,k);
//                }
//            }
//        for(int i = 0; i< sortedUsers.size(); i++)
//        {
//            sortedUsersHashMap.put(sortedUsers.get(i).key,i);
//        }
//
//    }

//    private void swap(int i, int j) {
//        User tmp = sortedUsers.get(i);
//
//        sortedUsers.set(i,sortedUsers.get(j));
//
//        sortedUsers.set(j,tmp);
//    }

    ListUpdatedEventListener updateListener;
    public void setEventListener(ListUpdatedEventListener listener)
    {
        updateListener = listener;
    }

public interface ListUpdatedEventListener {
    void onListUpdated();
}
}

