package com.example.a38162.attractionsofnis;

import android.*;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class LocationService extends Service {
    String myID;
    FirebaseAuth mAuth;
    private LocationListener listener;
    private LocationManager locationManager;
    GeoFire geoFire;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GeoQuery geoQuery;
    private Map<String,Boolean> notificationList;
    List<Score> userScores;
    Location myLocation;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        mAuth = FirebaseAuth.getInstance();
        myID = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Geofire");
        geoFire = new GeoFire(ref);
        notificationList=new HashMap<>();
        userScores = new ArrayList<>();

        GetUserScores();

        //InitListener();

//        listener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {   //na promenu moje lokacije se poziva, ovaj servis detektuje kad se moja lokacija promeni i promenim je na mapi
//                Intent i = new Intent("location_update");
//                i.putExtra("latitude", location.getLatitude());
//                i.putExtra("longitude", location.getLongitude());
//                i.putExtra("filter","update");
//                sendBroadcast(i);		//poziv mape   pronadji takvu fju na mapi!!!!!!!!!!!!!!!!!!
//
//                myLocation = location;
//                FirebaseDatabase db = FirebaseDatabase.getInstance();
//                DatabaseReference dr = db.getReference("users").child(myID);
//                dr.child("latitude").setValue(String.valueOf(location.getLatitude()));
//                dr.child("longitude").setValue(String.valueOf(location.getLongitude()));
//
//                //if(geoQuery != null)
//                    //geoQuery.removeAllListeners();
//
//                checkMap(notificationList);			//provera blizine objekata od mene
//
//                geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.5);
//                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
//                    @Override
//                    public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
//                        Object o = dataSnapshot.getValue();
//
//                        final String placeID = dataSnapshot.getKey();
//
//                        if(IsTherePlaceInUserScores(placeID)) {
//                            Log.e("Lokacija u blizini", placeID);
//
//                            if (notificationList.containsKey(placeID)) {        //proveravam da li u noti postoji objekat, ako postiji stavljam ga na true
//                                notificationList.remove(placeID);
//                                notificationList.put(placeID, true);
//                            } else {
//                                notificationList.put(placeID, true);        //ako ne postoji objekat u noti, onda ga dodajem u listu i saljem korisniku noti
//                                SendNotification(placeID, false);
//                            }
//
//                            float[] dist = new float[1];
//                            Location.distanceBetween(location.latitude, location.longitude, myLocation.getLatitude(), myLocation.getLongitude(), dist);
//                            //u metrima, distanca od 200m
//                            if (dist[0] <= 200) {
//                                Log.e("Pored objekta sam", placeID);
//
//                                SendNotification(placeID, true);            //posaljem sebi noti da mi je u blizini objekat
//
//                                notificationList.remove(placeID);        //ubacim u hash, da ne bi svaki put slala noti vec samo jednom
//
//                                int index = 0;
//                                boolean success = false;
//                                while( index < userScores.size() && !success)
//                                 {
//                                     Score score = userScores.get(index);
//
//                                     if(score.getPlaceId().equals(placeID)) {
//                                         FirebaseDatabase.getInstance().getReference().child("scores").child(score.getScoreId()).child("visited").setValue("1");
//                                         Intent i = new Intent("location_final");
//                                         i.putExtra("scoreId", score.getScoreId());
//                                         i.putExtra("placeId", score.getPlaceId());
//                                         i.putExtra("filter", "final");
//                                         userScores.remove(index);
//                                         success = true;
//                                         sendBroadcast(i);
//                                     }
//                                    else
//                                        index++;
//                                }
//                            }
//                        }
//                    }
//                    @Override
//                    public void onDataExited(DataSnapshot dataSnapshot) {
//                    }
//
//                    @Override
//                    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
//                    }
//
//                    @Override
//                    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
//                    }
//
//                    @Override
//                    public void onGeoQueryReady() {
//                    }
//
//                    @Override
//                    public void onGeoQueryError(DatabaseError error) {
//                    }
//                });
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//                Log.i("status", "dddddd");
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);			//sa neta deo, za gasenje servisa pojma nemam, neka ostane tako
//            }
//        };
        //locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0.2f, listener);		//setinterval na 5s, 0 je minimalna distanca
    }


    private void InitListener() {

        LocationManager mLocationManager;
        Location location;

        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        location = bestLocation;

        if (location != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference dr = db.getReference("users").child(myID);
            dr.child("latitude").setValue(String.valueOf(location.getLatitude()));            //moja lokacija
            dr.child("longitude").setValue(String.valueOf(location.getLongitude()));

            geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.5);        //svi objekti koji su na  od mog radiusa
            geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                @Override
                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                    Object o = dataSnapshot.getValue();

                    final String placeID = dataSnapshot.getKey();
                    Log.e("Pored objekta sam", placeID);

                    SendNotification(placeID, true);            //posaljem sebi noti da mi je u blizini objekat

                    notificationList.remove(placeID);        //ubacim u hash, da ne bi svaki put slala noti vec samo jednom

                    for (Score score : userScores) {
                        if (score.getPlaceId().equals(placeID)) {
                            FirebaseDatabase.getInstance().getReference().child("scores").child(score.getScoreId()).child("visited").setValue("1");
                            Intent i = new Intent("location_final");
                            i.putExtra("scoreId", score.getScoreId());
                            i.putExtra("placeId", score.getPlaceId());
                            i.putExtra("filter", "final");
                            sendBroadcast(i);
                        }
                    }

                /*FirebaseDatabase.getInstance().getReference().child("scores").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            for(DataSnapshot d : dataSnapshot.getChildren()) {
                                Score score = d.getValue(Score.class);
                                if(score.getPlaceId().equals(placeID)) {
                                    score.visited = "1";

                                    Intent i = new Intent("location_final");
                                    i.putExtra("scoreId", score.getScoreId());
                                    i.putExtra("placeId", score.getPlaceId());
                                    i.putExtra("filter","final");
                                    sendBroadcast(i);		//poziv mape   pronadji takvu fju na mapi!!!!!!!!!!!!!!!!!!
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });*/
                }

                @Override
                public void onDataExited(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
                }

                @Override
                public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
                }

                @Override
                public void onGeoQueryReady() {
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                }
            });
        }
    }

    private void checkMap(Map<String, Boolean> notificationList) {			//objekti koji su u mom radijusu, pa izadju iz njega, pa potpuno izadju
        for (Map.Entry<String, Boolean> entry : notificationList.entrySet()) {
            if ( entry.getValue()==true) {
                entry.setValue(false);		//izadju iz mog radiusa, ali su u blizini
            }
            else
                notificationList.remove(entry.getKey());			//izbrisem ih skroz jer nisu u mojoj blizini

        }
    }

    private void SendNotification(String challengeID, boolean finalDestination)
    {
        String header;
        String body;

        if(finalDestination) {
            header = "Final place!";
            body = "Congratulations! You have reached the destination, Click here to open map";
        }
        else {
            header = "Place nearby";
            body = "There's a place nearby. Click here to open map";
        }

        Intent intent = new Intent(LocationService.this, MapActivity.class);		//ovo je deo za pracenje lokacije
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Log.e("Notifikacija", "usaooo u fjuuuu");
        Toast.makeText(LocationService.this, header + "\n" + body, Toast.LENGTH_LONG).show();
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =//SettingsActivity.this
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(header)
                        .setSound(uri)
                        .setSubText(challengeID)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        mNotificationManager.notify(m, mBuilder.build());

//        NotificationManager mNotificationManager;
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
//        Intent ii = new Intent(getApplicationContext(), MapActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);
//
//        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
//        bigText.bigText("Place");
//        bigText.setBigContentTitle("Today's Bible Verse");
//        bigText.setSummaryText("Text in detail");
//
//        mBuilder.setContentIntent(pendingIntent);
//        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
//        mBuilder.setContentTitle("Your Title");
//        mBuilder.setContentText("Your text");
//        mBuilder.setPriority(Notification.PRIORITY_MAX);
//        mBuilder.setStyle(bigText);
//
//        mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("notify_001",
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            mNotificationManager.createNotificationChannel(channel);
//        }
//
//        mNotificationManager.notify(0, mBuilder.build());
    }

    private void GetUserScores() {
        FirebaseDatabase.getInstance().getReference().child("scores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    for(DataSnapshot d : dataSnapshot.getChildren()) {
                        Score score = d.getValue(Score.class);
                        if(score.getUserId().equals(userId)) {
                            userScores.add(score);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean IsTherePlaceInUserScores(String placeId) {
        int i=0;
        boolean success = false;
        while(i < userScores.size() && !success) {
            if(userScores.get(i).getPlaceId().equals(placeId) && userScores.get(i).getVisited().equals("0")) {
                success = true;
            }
        }
        return success;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
        {
            locationManager.removeUpdates(listener);
        }
        Log.i("destroy","dddddd");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
