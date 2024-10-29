package com.example.needit;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "IntroActivity";

    private FirebaseAuth mAuth;

    private TextView promptText; // Declare promptText here


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mAuth = FirebaseAuth.getInstance();

        ImageButton micButton = findViewById(R.id.mic_button);
        Button loginButton = findViewById(R.id.login_button);
        Button getStartedButton = findViewById(R.id.get_started_button);
        promptText = findViewById(R.id.prompt_text);
        TextView optionYes = findViewById(R.id.option_yes);
        TextView optionNo = findViewById(R.id.option_no);
        Button cancelButton = findViewById(R.id.cancel_button);
        //removed all voice-related functionality

        loginButton.setOnClickListener(v -> navigateToMainActivity());
        getStartedButton.setOnClickListener(v -> navigateToSignUpActivity());


        cancelButton.setOnClickListener(v -> Toast.makeText(IntroActivity.this, "Voice assistant not available", Toast.LENGTH_SHORT).show());

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(v -> {
            if (v != promptText) {
                Toast.makeText(IntroActivity.this, "Voice assistant not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //removed TTS


    private void navigateToMainActivity() {
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void navigateToSignUpActivity() {
        Intent intent = new Intent(IntroActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            promptText.setVisibility(View.GONE);
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {

        executorService.shutdown(); // Shut down the ExecutorService
        super.onDestroy();
    }
}
