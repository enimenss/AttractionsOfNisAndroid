package com.example.a38162.attractionsofnis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class MapActivity extends  AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private LocationManager locationManager, mLocationManager;
    Location myLocation;
    Marker myMarker;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    List<Place> landmarks;
    List<Place> museums;
    List<Place> restaurants;
    List<Place> placesForUser;
    Map<String, String> friends;
    Set<String> friendsIDs;
    Set<String> userIDs;
    ArrayList<User> friendsOnMap;
    ArrayList<User> usersOnMap;
    List<Marker> markerPlaces;
    List<Marker> markersForUser;
    HashMap<Marker,String> markerUsers;
    HashMap<Marker, String> markerMyFriends;
    ArrayList<LatLng> listPoints;
    String idUser;
    Handler handler;
    boolean visableFriends = false;
    List<Score> scoresForUser;
    int pointsForUser;
    FirebaseAuth mAuth;
    BroadcastReceiver broadcastReceiver;
    boolean showFriends = false;
    boolean showUsers = false;
    Semaphore userSemaphore;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        landmarks = new ArrayList<>();
        museums = new ArrayList<>();
        restaurants = new ArrayList<>();
        listPoints = new ArrayList<>();
        friends = new HashMap<>();
        placesForUser = new ArrayList<>();
        markersForUser = new ArrayList<>();
        markerPlaces = new ArrayList<>();
        scoresForUser = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        friendsOnMap = new ArrayList<>();
        markerMyFriends = new HashMap<>();
        markerUsers = new HashMap<>();
        usersOnMap = new ArrayList<>();
        userIDs = new android.support.v4.util.ArraySet<>();
        friendsIDs = new android.support.v4.util.ArraySet<>();

        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();

        idUser = mAuth.getCurrentUser().getUid();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        Places();
        UsersPlaces();

       Button button_addPlace = (Button) findViewById(R.id.button_addPlace);
       button_addPlace.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(MapActivity.this, AddPlaceActivity.class);
               startActivity(intent);
           }
       });

        Button button_search = (Button) findViewById(R.id.button_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchPlace();
            }
        });

        Button button_landmarks = (Button) findViewById(R.id.button_landmarks);
        button_landmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMarkersForPlaces("landmarks");
            }
        });

        Button button_restaurants = (Button) findViewById(R.id.button_clubs);
        button_restaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMarkersForPlaces("restaurants");
            }
        });

        Button button_museums = (Button) findViewById(R.id.button_museums);
        button_museums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMarkersForPlaces("museums");
            }
        });

        Button button_radius = (Button) findViewById(R.id.button_radius);
        button_radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMarkersForPlaces("radius");
            }
    });

        Button button_clearMarkers = (Button) findViewById(R.id.button_clearMarkers);
        button_clearMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearAllMarkers();
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");

        showFriends = sharedPref.getBoolean("showFriends", false);
        showUsers = sharedPref.getBoolean("showUsers", false);

        Log.e("Show users", showUsers + " ");
        if(showUsers == true)
            ShowUsers();
        else RemoveAllMarkers(markerUsers);

        if(showFriends)
            ShowFriends();
        else RemoveAllMarkers(markerMyFriends);

        Intent i = new Intent(getApplicationContext(),LocationService.class);
        startService(i);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(broadcastReceiver == null)
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Toast.makeText(getApplicationContext(),intent.getExtras().get("coordinates").toString(),Toast.LENGTH_SHORT).show();
                    String filter = intent.getStringExtra("filter");

                    if(filter.equals("update")) {
                        Double lat = intent.getDoubleExtra("latitude", 0);
                        Double lon = intent.getDoubleExtra("longitude", 0);
                        if (lon != 0 && lat != 0) {
                            LatLng placeLoc = new LatLng(lat, lon);
                            myMarker.remove();
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(placeLoc);
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mymarker));
                            markerOptions.title("This is me");
                            myMarker = mMap.addMarker(markerOptions);

                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
                        }
                    }
                    else if(filter.equals("final")) {
                        String placeId = intent.getStringExtra("placeId");
                        String scoreId = intent.getStringExtra("scoreId");
                        int i = 0;
                        boolean deleted = true;
                        while(i < placesForUser.size() && deleted) {
                            if(placesForUser.get(i).getPlaceId().equals(placeId)) {
                                //markersForUser.remove(i);
                                markersForUser.get(i).remove();
                                deleted = false;
                                placesForUser.remove(i);
                                FirebaseDatabase.getInstance().getReference().child("scores").child(scoreId).child("visited").setValue("1");
                            }
                            i++;
                        }
                    }
                }
            };
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
        registerReceiver(broadcastReceiver,new IntentFilter("location_final"));

        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);

        showFriends = sharedPref.getBoolean("showFriends", false);
        showUsers = sharedPref.getBoolean("showUsers", false);

        if(showUsers == true)
            ShowUsers();
        else RemoveAllMarkers(markerUsers);

        if(showFriends)
            ShowFriends();
        else RemoveAllMarkers(markerMyFriends);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.map) {
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

    private void RemoveAllMarkers(HashMap<Marker, String> map) {
        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            Marker m= entry.getKey();
            m.remove();

        }
        map.clear();
    }

    public void Places() {
        FirebaseDatabase.getInstance().getReference().child("places").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                restaurants.clear();
                landmarks.clear();
                museums.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Place place = snapshot.getValue(Place.class);
                        if (place.category.equals("landmark")) {
                            landmarks.add(place);
                        } else if (place.category.matches("museum")) {
                            museums.add(place);
                        } else if (place.category.matches("restaurant")) {
                            restaurants.add(place);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void SetMarkersForPlaces(String type) {
        if(type == "landmarks") {
            for(int i=0;i<landmarks.size();i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                LatLng latLng = new LatLng(Double.parseDouble(landmarks.get(i).getLatitude()),Double.parseDouble(landmarks.get(i).getLongitude()));
                mMap.addMarker(markerOptions.position(latLng).title(landmarks.get(i).getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                markerPlaces.add(mMap.addMarker(markerOptions));
            }
        }
        else if(type == "museums") {
            for(int i=0;i<museums.size();i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(Double.parseDouble(museums.get(i).getLatitude()),Double.parseDouble(museums.get(i).getLongitude()));
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                mMap.addMarker(markerOptions.title(museums.get(i).getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                markerPlaces.add(mMap.addMarker(markerOptions));
            }
        }
        else if(type == "restaurants") {
            for(int i=0;i<restaurants.size();i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(Double.parseDouble(restaurants.get(i).getLatitude()),Double.parseDouble(restaurants.get(i).getLongitude()));
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                mMap.addMarker(markerOptions.title(restaurants.get(i).getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                markerPlaces.add(mMap.addMarker(markerOptions));
            }
        }
        else if(type =="radius") {
            List<Place> places = new ArrayList<>();
            places.addAll(museums);
            places.addAll(landmarks);
            places.addAll(restaurants);
            for (int i = 0; i < places.size(); i++)
            {
                Place place = places.get(i);

                float[] dist = new float[1];
                if(myLocation == null) {
                    MyLocation();
                }
                Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), Double.parseDouble(place.latitude), Double.parseDouble(place.longitude), dist);
                //u metrima, distanca od 2km
                if (dist[0] <= 2000) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(Double.parseDouble(places.get(i).getLatitude()),Double.parseDouble(places.get(i).getLongitude()));
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    mMap.addMarker(markerOptions.title(places.get(i).getName()));
                }
            }
        }
    }

    public void UsersPlaces() {
        FirebaseDatabase.getInstance().getReference("scores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                placesForUser.clear();
                if (dataSnapshot.exists())
                {
                    idUser = mAuth.getCurrentUser().getUid();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Score score = snapshot.getValue(Score.class);
                        if (score.getUserId().equals(idUser) && score.getVisited().equals("0")) {
                            scoresForUser.add(score);
                            boolean finded = false;
                            for (Place p : museums) {
                                if (p.getPlaceId().equals(score.getPlaceId())) {
                                    placesForUser.add(p);
                                    if (score.getVisited().equals("1"))
                                        pointsForUser += Integer.parseInt(p.getPoints());
                                    break;
                                }
                            }
                            if (!finded) {
                                for (Place p : landmarks) {
                                    if (p.getPlaceId().equals(score.getPlaceId())) {
                                        placesForUser.add(p);
                                        if (score.getVisited().equals("1"))
                                            pointsForUser += Integer.parseInt(p.getPoints());
                                        break;
                                    }
                                }
                            }
                            if (!finded) {
                                for (Place p : restaurants) {
                                    if (p.getPlaceId().equals(score.getPlaceId())) {
                                        placesForUser.add(p);
                                        if (score.getVisited().equals("1"))
                                            pointsForUser += Integer.parseInt(p.getPoints());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                for (Place p : placesForUser) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()));
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    Marker m = mMap.addMarker(markerOptions.position(latLng).title(p.getName()));
                    markersForUser.add(m);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Geofire");
                    GeoFire geoFire = new GeoFire(ref);

                    geoFire.setLocation(p.placeId, new GeoLocation(Double.parseDouble(p.latitude), Double.parseDouble(p.longitude)), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: " + error);
                            } else {
                                System.out.println("Location saved on server successfully!");
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void SearchPlace() {
        EditText et_search = (EditText) findViewById(R.id.editText_search);
        String location = et_search.getText().toString();
        List<Address> addressList = null;
        MarkerOptions markerOptions = new MarkerOptions();

        if(! location.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(location, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i=0; i < addressList.size();i++) {
                Address myAddress = addressList.get(i);
                LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                String string = myAddress.getLocality();
                markerOptions.position(latLng);
                mMap.addMarker(markerOptions.title(string));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    public void ClearAllMarkers() {
        for(Marker m : markerPlaces) {
            m.remove();
        }
        markerPlaces.clear();
        mMap.clear();

        for (Place p : placesForUser) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mMap.addMarker(markerOptions.position(latLng).title(p.getName()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            //markerPlaces.add(markerOptions);
        }    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.e("Usao u fju", "Marker tu");
        int i=0;
        for(Marker m : markerMyFriends.keySet()) {
            if (marker.equals(myMarker)) {
                String friendID = friendsOnMap.get(i).userId;
                Intent intent = new Intent(MapActivity.this, FriendProfileActivity.class);
                intent.putExtra("friendID", friendID);
                startActivity(intent);
            }
            i++;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        MyLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }

                listPoints.add(latLng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if(listPoints.size() == 1) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                }
                mMap.addMarker(markerOptions);

                if(listPoints.size() == 2) {
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        });
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude +","+origin.longitude;
        String str_dest ="destination="+dest.longitude+","+dest.longitude;
        String sensor = "sensor=false";
        String mode ="mode=driving";
        String param = str_origin+"&"+str_dest+"&"+sensor+"&"+mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output +"?" + param;

        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    private void MyLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        myLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            }

            mMap.setMyLocationEnabled(true);

            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (myLocation == null || l.getAccuracy() < myLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                myLocation = l;
            }
        }

        if(myLocation != null)
        {
            LatLng placeLoc = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());           //posalji ovde moju lokaciju
            placeLoc = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(placeLoc);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mymarker));
            markerOptions.title("This is me");

            myMarker = mMap.addMarker(markerOptions);

//            LocationService locationService = new LocationService(idUser);
//            locationService.UpdateMyLocation(idUser,  myLocation.getLatitude(),myLocation.getLongitude());
        }
    }

    private void ShowUsers() {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference userReference=db.getReference("users");
        ChildEventListener userListener=userReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final User user=dataSnapshot.getValue(User.class);
                if(user != null) {
                    if (!dataSnapshot.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        userIDs.add(dataSnapshot.getKey());
                        if (user.visable.equals("1")) {
                            if (!usersOnMap.contains(user))
                                usersOnMap.add(user);

                            final LatLng ll = new LatLng(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude));

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    URL url;
                                    final MarkerOptions markerOptions = new MarkerOptions();
                                    try {
                                        url = new URL(user.picture);
                                        final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                        final Bitmap smallMarker = Bitmap.createScaledBitmap(bmp, 110, 115, false);
                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            markerOptions.position(ll);
                                            markerOptions.snippet(user.name + " " + user.surname);

                                            markerOptions.title(user.username);

                                            if (markerUsers.containsValue(dataSnapshot.getKey())) {
                                                Marker delete = getKeyByValue(markerUsers, dataSnapshot.getKey());
                                                markerUsers.remove(delete);
                                                delete.remove();
                                            }

                                            Marker marker = mMap.addMarker(markerOptions);
                                            marker.setTag(user);

                                            markerUsers.put(marker, dataSnapshot.getKey());
                                        }
                                    });
                                }
                            });
                            thread.start();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                final User user=dataSnapshot.getValue(User.class);
                if(!dataSnapshot.getKey().equals(mAuth.getCurrentUser().getUid())) {
                    userIDs.add(dataSnapshot.getKey());
                    if (user.visable.equals("1")) {
                        if (!usersOnMap.contains(user))
                            usersOnMap.add(user);

                        final LatLng ll = new LatLng(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude));

                        final Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                URL url;
                                final MarkerOptions markerOptions = new MarkerOptions();
                                try {
                                    url = new URL(user.picture);
                                    final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                    final Bitmap smallMarker = Bitmap.createScaledBitmap(bmp, 110, 115, false);
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {


                                        markerOptions.position(ll);
                                        markerOptions.snippet("Name: " + user.name + "\n" + "Surname: " + user.surname);
                                        markerOptions.title(user.username);

                                        if (markerUsers.containsValue(dataSnapshot.getKey())) {
                                            Marker delete = getKeyByValue(markerUsers, dataSnapshot.getKey());
                                            markerUsers.remove(delete);
                                            delete.remove();
                                        }

                                        Marker marker = mMap.addMarker(markerOptions);
                                        marker.setTag(user);

                                        markerUsers.put(marker, dataSnapshot.getKey());
                                        userSemaphore.release();
                                    }
                                });
                            }
                        });
                        thread.start();
                    }
                }
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

    private void ShowFriends() {

        DatabaseReference friendRefrence= FirebaseDatabase.getInstance().getReference().child("friends").child(mAuth.getCurrentUser().getUid());
        ValueEventListener friendsListener = friendRefrence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = (Map<String, String>) dataSnapshot.getValue();
                if (friends != null) {
                    friendsIDs = friends.keySet();
                }
                if(friendsIDs!=null)
                {
                    List<String> list= new ArrayList<>(friendsIDs);
                    for (int i=0;i<list.size();i++)
                    {
                        getFriendFromFirebase(list.get(i));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFriendFromFirebase(final String s) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("users");

        final Query query = dr.orderByKey().equalTo(s);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String ss) {
                final User user  = dataSnapshot.getValue(User.class);
                if (user.visable.equals("1")) {
                    if (!friendsOnMap.contains(user))
                        friendsOnMap.add(user);

                    final LatLng ll = new LatLng(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude));

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            URL url;
                            final MarkerOptions markerOptions = new MarkerOptions();
                            try {
                                url = new URL(user.picture);
                                final Bitmap bmp  = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                final Bitmap smallMarker = Bitmap.createScaledBitmap(bmp,110,115,false);
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    markerOptions.position(ll);
                                    markerOptions.snippet(user.name + " " + user.surname);
                                    markerOptions.title(user.username);

                                    if (markerMyFriends.containsValue(s)) {
                                        Marker delete=getKeyByValue(markerMyFriends,s);
                                        markerMyFriends.remove(delete);
                                        delete.remove();
                                    }

                                    Marker marker = mMap.addMarker(markerOptions);
                                    if(!showFriends)
                                        marker.setVisible(false);
                                    marker.setTag(user);
                                    markerMyFriends.put(marker, s);

                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            int i=0;
                                            for(User u : friendsOnMap) {
                                                LatLng pos = new LatLng(Double.parseDouble(u.latitude), Double.parseDouble(u.longitude));
                                                if (marker.getPosition().equals(pos)) {
                                                    String friendID = u.getUserId();
                                                    Intent intent = new Intent(MapActivity.this, FriendProfileActivity.class);
                                                    intent.putExtra("friendID", friendID);
                                                    startActivity(intent);
                                                }
                                                i++;
                                            }
                                            return false;
                                        }
                                    });
                                }
                            });
                        }
                    });
                    thread.start();
                }

                query.removeEventListener(this);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, final String s) {
                final User user  = dataSnapshot.getValue(User.class);
                if (user.visable.equals("1")) {
                    if (!friendsOnMap.contains(user))
                        friendsOnMap.add(user);

                    final LatLng ll = new LatLng(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude));

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            URL url ;
                            final MarkerOptions markerOptions = new MarkerOptions();
                            try {
                                url = new URL(user.picture);
                                final Bitmap bmp  = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                final Bitmap smallMarker = Bitmap.createScaledBitmap(bmp,110,115,false);
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    markerOptions.position(ll);
                                    markerOptions.snippet("Name: " + user.name + "\n" + "Last name: " + user.surname);
                                    markerOptions.title(user.username);

                                    if (markerMyFriends.containsValue(s)) {
                                        Marker delete=getKeyByValue(markerMyFriends,s);
                                        markerMyFriends.remove(delete);
                                        delete.remove();
                                    }

                                    Marker marker = mMap.addMarker(markerOptions);
                                    if(!showFriends)
                                        marker.setVisible(false);
                                    marker.setTag(user);
                                    markerMyFriends.put(marker, s);
                                }
                            });
                        }
                    });
                    thread.start();





                }

                query.removeEventListener(this);


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

    public Marker getKeyByValue( Map<Marker, String> map, String value) {
        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            if ( value.compareTo(entry.getValue())==0) {
                return entry.getKey();
            }
        }
        return null;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString ="";

            try {
                responseString = requestDirection(strings[0]);
            }catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject=null;
            List<List<HashMap<String,String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionParser directionParser = new DirectionParser();
                routes = directionParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(List<HashMap<String,String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for(HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if(polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
            }
            else {
                Toast.makeText(getApplicationContext(), "Direction not fount", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
