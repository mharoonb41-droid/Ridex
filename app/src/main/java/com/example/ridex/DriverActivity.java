package com.example.ridex;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ─── Constants ────────────────────────────────────────────────
    private static final String API_KEY = "AIzaSyBUrpSUABPngho4jp0jZnF1y9xFybrxZiY";

    // ─── Views ────────────────────────────────────────────────────
    GoogleMap mMap;
    TextView driverStatus, riderInfo;
    Button acceptRideBtn, completeRideBtn, logoutBtn;

    // ─── Firebase ─────────────────────────────────────────────────
    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    // ─── Location ─────────────────────────────────────────────────
    FusedLocationProviderClient locationClient;
    LocationCallback locationCallback;
    LatLng driverLocation;

    // ─── Ride Info ────────────────────────────────────────────────
    String currentRiderId = null;
    LatLng riderLatLng    = null;
    LatLng destLatLng     = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Views
        driverStatus    = findViewById(R.id.driverStatus);
        riderInfo       = findViewById(R.id.riderInfo);
        acceptRideBtn   = findViewById(R.id.acceptRideBtn);
        completeRideBtn = findViewById(R.id.completeRideBtn);
        logoutBtn       = findViewById(R.id.logoutBtn);

        // Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.driverMap);
        mapFragment.getMapAsync(this);

        // Location
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Buttons
        acceptRideBtn.setOnClickListener(v -> acceptRide());
        completeRideBtn.setOnClickListener(v -> completeRide());
        logoutBtn.setOnClickListener(v -> logoutUser());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        listenForRideRequests();
    }

    // ─── Location ─────────────────────────────────────────────────
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        mMap.setMyLocationEnabled(true);

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    driverLocation = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    if (currentRiderId == null) {
                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(driverLocation, 15)
                        );
                    }
                }
            }
        };

        locationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
        );
    }

    // ─── Ride Requests Listen ─────────────────────────────────────
    private void listenForRideRequests() {
        dbRef.child("rideRequests")
                .orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ride : snapshot.getChildren()) {
                                currentRiderId = ride.getKey();

                                String destination = ride.child("destination")
                                        .getValue(String.class);
                                String price = ride.child("price")
                                        .getValue(String.class);
                                double rLat = ride.child("riderLat")
                                        .getValue(Double.class);
                                double rLng = ride.child("riderLng")
                                        .getValue(Double.class);

                                riderLatLng = new LatLng(rLat, rLng);

                                // Destination coordinates
                                Object dLatObj = ride.child("destLat").getValue();
                                Object dLngObj = ride.child("destLng").getValue();
                                if (dLatObj != null && dLngObj != null) {
                                    destLatLng = new LatLng(
                                            ((Number) dLatObj).doubleValue(),
                                            ((Number) dLngObj).doubleValue()
                                    );
                                }

                                // Map update
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions()
                                        .position(riderLatLng)
                                        .title("Rider"));
                                mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(riderLatLng, 14)
                                );

                                // Info dikhao
                                driverStatus.setText(" New ride request!");
                                riderInfo.setText(
                                        " Destination: " + destination
                                                + (price != null ? "\n Price: " + price : "")
                                );
                                riderInfo.setVisibility(View.VISIBLE);
                                acceptRideBtn.setVisibility(View.VISIBLE);
                                break;
                            }
                        } else {
                            if (currentRiderId == null) {
                                driverStatus.setText(
                                        " Waiting for ride requests..."
                                );
                                riderInfo.setVisibility(View.GONE);
                                acceptRideBtn.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ─── Accept Ride ──────────────────────────────────────────────
    private void acceptRide() {
        if (currentRiderId == null) return;

        String driverId = mAuth.getCurrentUser().getUid();
        dbRef.child("rideRequests").child(currentRiderId)
                .child("status").setValue("accepted");
        dbRef.child("rideRequests").child(currentRiderId)
                .child("driverId").setValue(driverId)
                .addOnSuccessListener(unused -> {
                    driverStatus.setText(" Ride Accepted! Finding Best Route ...");
                    acceptRideBtn.setVisibility(View.GONE);
                    completeRideBtn.setVisibility(View.VISIBLE);

                    // Rider tak route draw karo
                    if (driverLocation != null && riderLatLng != null) {
                        MapsHelper.drawRoute(mMap, driverLocation,
                                riderLatLng, API_KEY,
                                new MapsHelper.RouteCallback() {
                                    @Override
                                    public void onRouteFound(
                                            java.util.List<LatLng> points,
                                            String distance, String duration) {
                                        driverStatus.setText(
                                                " Rider : " + distance
                                                        + " | " + duration
                                        );

                                        // Destination ka bhi route dikhao
                                        if (destLatLng != null) {
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(destLatLng)
                                                    .title("Destination"));
                                        }
                                    }
                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(DriverActivity.this,
                                                "Route error: " + error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    // ─── Complete Ride ────────────────────────────────────────────
    private void completeRide() {
        if (currentRiderId == null) return;

        dbRef.child("rideRequests").child(currentRiderId)
                .child("status").setValue("completed")
                .addOnSuccessListener(unused -> {
                    driverStatus.setText("Ride Complete!");
                    completeRideBtn.setVisibility(View.GONE);
                    riderInfo.setVisibility(View.GONE);
                    currentRiderId = null;
                    riderLatLng    = null;
                    destLatLng     = null;
                    mMap.clear();
                    Toast.makeText(this,
                            "Ride complete! ", Toast.LENGTH_SHORT).show();
                });
    }

    // ─── Logout ───────────────────────────────────────────────────
    private void logoutUser() {
        if (locationCallback != null)
            locationClient.removeLocationUpdates(locationCallback);
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null)
            locationClient.removeLocationUpdates(locationCallback);
    }
}