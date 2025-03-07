package com.example.needit;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText dobEditText;
    private EditText passwordEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.signup_email);
        nameEditText = findViewById(R.id.signup_name);
        dobEditText = findViewById(R.id.signup_dob);
        passwordEditText = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dobEditText.setOnClickListener(v -> showDatePickerDialog());

        signupButton.setOnClickListener(v -> signUpUser());
        ToggleButton togglePassword = findViewById(R.id.togglePassword);

        togglePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move cursor to end of text
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
        // Handle the back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SignUpActivity.this, IntroActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                (view, year1, month1, dayOfMonth) -> dobEditText.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1), year, month, day);
        datePickerDialog.show();
    }

    private void signUpUser() {
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if any field is empty
        if (email.isEmpty() || name.isEmpty() || dob.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash the password using SCRYPT algorithm and Firebase parameters
        try {
            String hashedPassword = hashPassword(password);

            // Create a HashMap to store user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("name", name);
            userData.put("dob", dob);

            // Proceed to sign up the user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                // Store user data in Firestore under their UID
                                db.collection("users").document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            Toast.makeText(SignUpActivity.this, "User data added successfully", Toast.LENGTH_SHORT).show();

                                            // Send email verification and update UI
                                            sendEmailVerification(user);
                                            updateUI(user);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error writing document", e);
                                            Toast.makeText(SignUpActivity.this, "Failed to add user data to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Handle exceptions
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                Toast.makeText(SignUpActivity.this, "Email already exists.",
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.w(TAG, "createUserWithEmail:failure", e);
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            updateUI(null);
                        }
                    });

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error hashing password.", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashPassword(String password) throws GeneralSecurityException {
        // Convert base64 encoded strings to byte arrays
        byte[] saltSeparator = Base64.decode("Bw==", Base64.DEFAULT);
        byte[] signerKey = Base64.decode("E32zA6BuRlClDPEOj+xwIqBvvddoPQXSVwoD4Z4BmaxY53L5zPUP4XhxuNIK0SWuyqXZlJx1yunjS9NwCg97Kg==", Base64.DEFAULT);

        // SCRYPT parameters
        int rounds = 8;
        int memoryCost = 16384; // 2^14

        // Hash password using SCRYPT
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltSeparator, rounds, memoryCost);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        // Combine hashed password with algorithm identifier and SCRYPT parameters
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Log the success and user email
                        Toast.makeText(SignUpActivity.this, "Verification e-mal has been sent", Toast.LENGTH_SHORT).show();
                        // Show popup message instead of Toast
                        new Handler(Looper.getMainLooper()).post(() -> showVerificationPopup(user.getEmail()));
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(SignUpActivity.this,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVerificationPopup(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Verification")
                .setMessage("Verification mail has been sent to: " + email)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // Continue with the application flow
                    Toast.makeText(SignUpActivity.this, "Verification Popup shown", Toast.LENGTH_SHORT).show();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

