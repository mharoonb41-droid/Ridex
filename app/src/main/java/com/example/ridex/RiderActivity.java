package com.example.ridex;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import com.example.ridex.models.RideRequest;

public class RiderActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ─── Constants ────────────────────────────────────────────────
    private static final String API_KEY = "AIzaSyBUrpSUABPngho4jp0jZnF1y9xFybrxZiY";

    // ─── Views ────────────────────────────────────────────────────
    GoogleMap mMap;
    EditText destinationInput;
    TextView rideStatus, distanceText, durationText, priceText;
    Button requestRideBtn, logoutBtn, checkPriceBtn;
    LinearLayout priceCard;

    // ─── Firebase ─────────────────────────────────────────────────
    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    // ─── Location ─────────────────────────────────────────────────
    FusedLocationProviderClient locationClient;
    LocationCallback locationCallback;
    LatLng riderLocation;

    // ─── Destination ──────────────────────────────────────────────
    LatLng destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Views
        destinationInput = findViewById(R.id.destinationInput);
        rideStatus       = findViewById(R.id.rideStatus);
        requestRideBtn   = findViewById(R.id.requestRideBtn);
        logoutBtn        = findViewById(R.id.logoutBtn);
        checkPriceBtn    = findViewById(R.id.checkPriceBtn);
        priceCard        = findViewById(R.id.priceCard);
        distanceText     = findViewById(R.id.distanceText);
        durationText     = findViewById(R.id.durationText);
        priceText        = findViewById(R.id.priceText);

        // Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.riderMap);
        mapFragment.getMapAsync(this);

        // Location
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Buttons
        checkPriceBtn.setOnClickListener(v -> checkPrice());
        requestRideBtn.setOnClickListener(v -> requestRide());
        logoutBtn.setOnClickListener(v -> logoutUser());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Map click se destination set karo
        mMap.setOnMapClickListener(latLng -> {
            destinationLatLng = latLng;
            mMap.clear();

            // Rider marker
            if (riderLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(riderLocation)
                        .title("You"));
            }

            // Destination marker
            mMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title("🏁 Destination"));

            destinationInput.setText(
                    latLng.latitude + ", " + latLng.longitude
            );
            rideStatus.setText(" Destination set! Check Price");
        });

        checkLocationPermission();
    }

    // ─── Location Permission ───────────────────────────────────────
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
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null && riderLocation == null) {
                    riderLocation = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    mMap.addMarker(new MarkerOptions()
                            .position(riderLocation)
                            .title("You are here"));
                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(riderLocation, 15)
                    );
                    rideStatus.setText("Tap the Dastination on map");
                }
            }
        };

        locationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
        );
    }

    // ─── Price Check ──────────────────────────────────────────────
    private void checkPrice() {
        if (riderLocation == null) {
            Toast.makeText(this, "Please Wait...",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (destinationLatLng == null) {
            Toast.makeText(this, "Tap the Dastination on map",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        checkPriceBtn.setText("Calculating... ");
        checkPriceBtn.setEnabled(false);

        MapsHelper.calculatePrice(riderLocation, destinationLatLng,
                API_KEY, new MapsHelper.PriceCallback() {
                    @Override
                    public void onPriceCalculated(String distance,
                                                  String duration,
                                                  double price) {
                        // Price card dikhao
                        priceCard.setVisibility(View.VISIBLE);
                        distanceText.setText(distance);
                        durationText.setText(duration);
                        priceText.setText("Rs. " + (int) price);

                        // Route draw karo
                        MapsHelper.drawRoute(mMap, riderLocation,
                                destinationLatLng, API_KEY,
                                new MapsHelper.RouteCallback() {
                                    @Override
                                    public void onRouteFound(
                                            java.util.List<LatLng> points,
                                            String dist, String dur) {
                                        rideStatus.setText(
                                                " Route ready! Request For Ride."
                                        );
                                    }
                                    @Override
                                    public void onError(String error) {}
                                });

                        checkPriceBtn.setText("CHECK PRICE ");
                        checkPriceBtn.setEnabled(true);
                        rideStatus.setText("Price: Rs. " + (int) price
                                + " | " + distance + " | " + duration);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(RiderActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                        checkPriceBtn.setText("CHECK PRICE ");
                        checkPriceBtn.setEnabled(true);
                    }
                });
    }

    // ─── Ride Request ─────────────────────────────────────────────
    private void requestRide() {
        if (riderLocation == null) {
            Toast.makeText(this, "Please Wait...",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (destinationLatLng == null) {
            Toast.makeText(this,
                    "Tap the Dastination on map",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String riderId = mAuth.getCurrentUser().getUid();
        String price   = priceText.getText().toString();

        RideRequest rideRequest = new RideRequest(
                riderId,
                destinationInput.getText().toString(),
                riderLocation.latitude,
                riderLocation.longitude
        );

        // Destination aur price bhi save karo
        dbRef.child("rideRequests").child(riderId)
                .setValue(rideRequest)
                .addOnSuccessListener(unused -> {
                    // Extra info save karo
                    dbRef.child("rideRequests").child(riderId)
                            .child("destLat").setValue(destinationLatLng.latitude);
                    dbRef.child("rideRequests").child(riderId)
                            .child("destLng").setValue(destinationLatLng.longitude);
                    dbRef.child("rideRequests").child(riderId)
                            .child("price").setValue(price);

                    rideStatus.setText("RideX Finding a Driver...");
                    requestRideBtn.setEnabled(false);
                    requestRideBtn.setText("Searching... ");
                    listenForDriverAccept(riderId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ─── Driver Accept Listen ─────────────────────────────────────
    private void listenForDriverAccept(String riderId) {
        dbRef.child("rideRequests").child(riderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;
                        String status = snapshot.child("status")
                                .getValue(String.class);
                        if (status == null) return;
                        switch (status) {
                            case "accepted":
                                rideStatus.setText(
                                        "Driver is Found & it's on the way"
                                );
                                requestRideBtn.setText("RIDE ACCEPTED ");
                                break;
                            case "completed":
                                rideStatus.setText(" Ride Complete! Shukriya");
                                requestRideBtn.setEnabled(true);
                                requestRideBtn.setText("REQUEST RIDEX ");
                                priceCard.setVisibility(View.GONE);
                                destinationLatLng = null;
                                mMap.clear();
                                if (riderLocation != null) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(riderLocation)
                                            .title("You"));
                                }
                                break;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
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