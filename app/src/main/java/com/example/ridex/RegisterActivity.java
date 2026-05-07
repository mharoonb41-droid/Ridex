package com.example.ridex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, emailInput, phoneInput, passwordInput;
    Button registerBtn, backToLoginBtn;
    RadioButton riderRadio, driverRadio;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase initialize
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Views
        nameInput     = findViewById(R.id.nameInput);
        emailInput    = findViewById(R.id.emailInput);
        phoneInput    = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn   = findViewById(R.id.registerBtn);
        backToLoginBtn = findViewById(R.id.backToLoginBtn);
        riderRadio    = findViewById(R.id.riderRadio);
        driverRadio   = findViewById(R.id.driverRadio);

        // Register button
        registerBtn.setOnClickListener(v -> registerUser());

        // Back to login
        backToLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name  = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String pass  = passwordInput.getText().toString().trim();
        String role  = riderRadio.isChecked() ? "rider" : "driver";

        // Validation
        if (name.isEmpty()) {
            nameInput.setError("Enter Name!");
            return;
        }
        if (email.isEmpty()) {
            emailInput.setError("Enter Email!");
            return;
        }
        if (phone.isEmpty()) {
            phoneInput.setError("Enter Phone Number!");
            return;
        }
        if (pass.isEmpty() || pass.length() < 6 || pass.length() >10 ) {
            passwordInput.setError("Enter passwords in 7 to 9 chracter/alphabets");
            return;
        }

        // Firebase mein account banao
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {

                    String userId = mAuth.getCurrentUser().getUid();

                    // User data database mein save karo
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("role", role);

                    dbRef.child("users").child(userId).setValue(userData)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "Account Successfully Created, Login Now! ",
                                        Toast.LENGTH_LONG).show();

                                // Login screen pe wapis jao
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Registration Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}