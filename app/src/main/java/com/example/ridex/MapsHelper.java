package com.example.ridex;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsHelper {

    // Callback interfaces
    public interface RouteCallback {
        void onRouteFound(List<LatLng> points, String distance, String duration);
        void onError(String error);
    }

    public interface PriceCallback {
        void onPriceCalculated(String distance, String duration, double price);
        void onError(String error);
    }

    // Price per KM (Rs.)
    private static final double PRICE_PER_KM = 50.0;
    private static final double BASE_FARE    = 100.0;

    // ─── Route Draw Karo ───────────────────────────────────────────
    public static void drawRoute(GoogleMap map, LatLng origin,
                                 LatLng destination, String apiKey,
                                 RouteCallback callback) {
        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&key=" + apiKey;

                String result = fetchUrl(url);
                JSONObject json = new JSONObject(result);
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() == 0) {
                    callback.onError("Koi route nahi mila!");
                    return;
                }

                JSONObject route = routes.getJSONObject(0);
                JSONObject leg   = route.getJSONArray("legs").getJSONObject(0);

                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                // Polyline points decode karo
                String encodedPoints = route
                        .getJSONObject("overview_polyline")
                        .getString("points");
                List<LatLng> points = decodePolyline(encodedPoints);

                // Main thread pe route draw karo
                android.os.Handler mainHandler =
                        new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    map.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .width(10)
                            .color(Color.parseColor("#FFD700"))
                            .geodesic(true));
                    callback.onRouteFound(points, distance, duration);
                });

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Distance + Price Calculate Karo ──────────────────────────
    public static void calculatePrice(LatLng origin, LatLng destination,
                                      String apiKey, PriceCallback callback) {
        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/distancematrix/json?"
                        + "origins=" + origin.latitude + "," + origin.longitude
                        + "&destinations=" + destination.latitude + "," + destination.longitude
                        + "&key=" + apiKey;

                String result = fetchUrl(url);
                JSONObject json = new JSONObject(result);

                JSONObject element = json
                        .getJSONArray("rows").getJSONObject(0)
                        .getJSONArray("elements").getJSONObject(0);

                String distance = element.getJSONObject("distance").getString("text");
                String duration = element.getJSONObject("duration").getString("text");
                double distanceKm = element.getJSONObject("distance")
                        .getDouble("value") / 1000.0;

                // Price calculate karo
                double price = BASE_FARE + (distanceKm * PRICE_PER_KM);

                android.os.Handler mainHandler =
                        new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() ->
                        callback.onPriceCalculated(distance, duration, price)
                );

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── URL Fetch Karo ───────────────────────────────────────────
    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        reader.close();
        return result.toString();
    }

    // ─── Polyline Decode Karo ─────────────────────────────────────
    private static List<LatLng> decodePolyline(String encoded) {
        List<LatLng> points = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0; result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            points.add(new LatLng(lat / 1e5, lng / 1e5));
        }
        return points;
    }
}