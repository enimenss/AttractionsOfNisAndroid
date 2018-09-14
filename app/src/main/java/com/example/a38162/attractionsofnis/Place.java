package com.example.a38162.attractionsofnis;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Place {
    String name;
    String description;
    String category;
    String latitude;
    String longitude;
    String points;

    @Exclude
    String placeId;

    public Place() {}

    public Place(String name, String description, String category, String latitude, String longitude, String points, String placeId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.points = points;
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPoints() {
        return points;
    }

    public String getPlaceId() {
        return placeId;
    }
}
