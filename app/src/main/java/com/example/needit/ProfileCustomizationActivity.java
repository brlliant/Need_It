package com.example.needit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileCustomizationActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, dobEditText, disabilityEditText;
    private Spinner genderSpinner;
    private Button updateButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_customiztion);

        nameEditText = findViewById(R.id.profile_name);
        emailEditText = findViewById(R.id.profile_email);
        dobEditText = findViewById(R.id.profile_dob);
        genderSpinner = findViewById(R.id.profile_gender);
        disabilityEditText = findViewById(R.id.profile_disability);
        updateButton = findViewById(R.id.profile_update_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dobEditText.setOnClickListener(v -> showDatePickerDialog());

        updateButton.setOnClickListener(v -> updateProfile());

        // Set up the Spinner with gender options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        fetchUserProfile();
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            populateProfileData(documentSnapshot);
                        } else {
                            Toast.makeText(this, "Profile data not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching profile data.", Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Error fetching profile data", e);
                    });
        }
    }

    private void populateProfileData(DocumentSnapshot documentSnapshot) {
        nameEditText.setText(documentSnapshot.getString("name"));
        emailEditText.setText(documentSnapshot.getString("email"));
        dobEditText.setText(documentSnapshot.getString("dob"));
        String gender = documentSnapshot.getString("gender");
        if (gender != null) {
            int genderPosition = getGenderPosition(gender);
            genderSpinner.setSelection(genderPosition);
        }
        disabilityEditText.setText(documentSnapshot.getString("disability"));
    }

    private int getGenderPosition(String gender) {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equals(gender)) {
                return i;
            }
        }
        return 0; // Default to first option if not found
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileCustomizationActivity.this,
                (view, year1, month1, dayOfMonth) -> dobEditText.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1), year, month, day);
        datePickerDialog.show();
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString().trim();
        String disability = disabilityEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || dob.isEmpty() || gender.isEmpty() || disability.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("dob", dob);
            userData.put("gender", gender);
            userData.put("disability", disability);

            db.collection("users").document(user.getUid())
                    .update(userData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Error updating profile", e);
                    });
        }
    }
}

