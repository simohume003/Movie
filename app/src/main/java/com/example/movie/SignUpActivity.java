package com.example.movie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;
    private Button btnSignUp, btnLogin;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSignUp = findViewById(R.id.signUpButton);
        btnLogin = findViewById(R.id.btnLogin);

        // Sign Up new user
        btnSignUp.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty()||email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter a name, email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "User created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                          .addOnFailureListener(e -> {
                                            Toast.makeText(this, "User created, but profile name was not saved", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                            Toast.makeText(this, "Sign Up failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                                    });
        });


                        // Log In existing user
                        btnLogin.setOnClickListener(v -> {
                            String email = emailInput.getText().toString().trim();
                            String password = passwordInput.getText().toString().trim();

                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Login failed: " +
                                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                        }
    }
