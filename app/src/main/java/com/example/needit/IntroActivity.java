package com.example.needit;

<<<<<<< HEAD

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
=======
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import androidx.credentials.exceptions.GetCredentialException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
>>>>>>> origin/master

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "IntroActivity";
<<<<<<< HEAD

    private FirebaseAuth mAuth;

    private TextView promptText; // Declare promptText here


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

=======
    private CredentialManager credentialManager;
    private FirebaseAuth mAuth;

>>>>>>> origin/master
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mAuth = FirebaseAuth.getInstance();
<<<<<<< HEAD

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

=======
        credentialManager = CredentialManager.create(this);

        // Google Sign-In
        findViewById(R.id.google_login_button).setOnClickListener(v -> signInWithGoogle());

        // Existing navigation
        findViewById(R.id.login_button).setOnClickListener(v -> navigateToLogin());
        findViewById(R.id.get_started_button).setOnClickListener(v -> navigateToSignUp());
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Google Sign-In failed", e);
                        showToast("Sign-in failed: " + e.getMessage());
                    }
                }
        );
    }

    private void handleGoogleCredential(Credential credential) {
        try {
            if (credential instanceof CustomCredential) {
                CustomCredential customCredential = (CustomCredential) credential;
                if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                    GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential
                            .createFrom(customCredential.getData());
                    authenticateWithFirebase(googleCredential.getIdToken());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling credential", e);
            showToast("Error processing sign-in");
        }
    }

    private void authenticateWithFirebase(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            navigateToMain();
                        } else {
                            mAuth.signOut();
                            showToast("Please verify your email first");
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        showToast("Authentication failed");
                    }
                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void navigateToSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
>>>>>>> origin/master

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
<<<<<<< HEAD
        if (currentUser != null) {
            promptText.setVisibility(View.GONE);
            updateUI(currentUser);
        }
=======
        updateUI(currentUser);
>>>>>>> origin/master
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

<<<<<<< HEAD
        executorService.shutdown(); // Shut down the ExecutorService
        super.onDestroy();
    }
}
=======
        super.onDestroy();
    }
}
>>>>>>> origin/master
