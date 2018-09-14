package com.example.a38162.attractionsofnis;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class DirectionParser {

    public List<List<HashMap<String, String>>> parse(JSONObject jsonObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String ,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {
            jRoutes = jsonObject.getJSONArray("routes");

            for(int i=0; i < jRoutes.length();i++){
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                for(int j=0;j< jLegs.length();j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    for(int k=0;k < jSteps.length();k++) {
                        String polyline="";
                        polyline = ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).toString();
                        List list = decodePolylines(polyline);

                        for(int l=0;l<list.size();l++) {
                            HashMap<String,String> hashMap = new HashMap<String, String>();
                            hashMap.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hashMap.put("lon", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routes;
    }

    private List decodePolylines(String encode) {
        List poly = new ArrayList();
        int index = 0, len = encode.length();
        int lat =0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encode.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            } while (b >=0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encode.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat/1E5, (double) lng/1E5);
            poly.add(p);
        }
        return poly;
    }
}
