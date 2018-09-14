package com.example.a38162.attractionsofnis;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class User {
    String name;
    String surname;
    String username;
    String password;
    String phone_number;
    String picture;
    String score;
    String email;
    String visable;
    String registrationToken;
    String latitude;
    String longitude;
    String scoringPlaces;

    @Exclude
    String userId;
    public User() {}

    public User(String userId, String name, String surname, String email, String username, String password, String phone_number, String picture, String score, String visable, String registrationToken) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.phone_number = phone_number;
        this.picture = picture;
        this.score = score;
        this.visable = visable;
        this.registrationToken = registrationToken;
        this.scoringPlaces = "no";
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getPicture() {
        return picture;
    }

    public  String getScore() { return  score; }

    public String getRegistrationToken() { return registrationToken; }

    @Override
    public String toString() {
        return username;
    }
}
