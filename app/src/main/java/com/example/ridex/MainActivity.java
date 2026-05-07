package com.example.ridex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Variables
    EditText emailInput, passwordInput;
    Button loginBtn, registerBtn;
    RadioButton riderRadio, driverRadio;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth initialize
        mAuth = FirebaseAuth.getInstance();

        // Views connect karo
        emailInput    = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn      = findViewById(R.id.loginBtn);
        registerBtn   = findViewById(R.id.registerBtn);
        riderRadio    = findViewById(R.id.riderRadio);
        driverRadio   = findViewById(R.id.driverRadio);

        // Login button
        loginBtn.setOnClickListener(v -> loginUser());

        // Register button
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String pass  = passwordInput.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            emailInput.setError("Enter Email");
            return;
        }
        if (pass.isEmpty()) {
            passwordInput.setError("Enter Password");
            return;
        }
        if (pass.length() < 6 || pass.length()>10) {
            passwordInput.setError("Enter passwords in 7 to 9 chracter/alphabets");
            return;
        }

        // Firebase se login
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                    // Role check karo
                    if (riderRadio.isChecked()) {
                        // Rider screen pe jao
                        startActivity(new Intent(this, RiderActivity.class));
                    } else {
                        // Driver screen pe jao
                        startActivity(new Intent(this, DriverActivity.class));
                    }
                    finish(); // Login screen band karo

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Login Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Agar pehle se logged in hai toh seedha Rider screen
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, RiderActivity.class));
            finish();
        }
    }
}