package com.example.needit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "IntroActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private boolean voiceAssistantEnabled = false;
    private TextToSpeech tts;
    private int failedAttempts = 0;

    private FirebaseAuth mAuth;

    private TextView promptText; // Declare promptText here


    private ActivityResultLauncher<Intent> speechRecognitionLauncher;

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

        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> voiceResults = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (voiceResults != null && !voiceResults.isEmpty()) {
                            Log.d(TAG, "Voice input received: " + voiceResults.get(0));
                            handleUserResponse(voiceResults.get(0).toLowerCase());
                        } else {
                            Log.d(TAG, "No voice input received");
                            speak("I didn't catch that. Please try again.");
                            handleFailedAttempt();
                        }
                    } else {
                        Log.d(TAG, "Speech recognition failed or canceled");
                        speak("Speech recognition failed. Please try again.");
                        handleFailedAttempt();
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_PERMISSIONS);
        } else {
            if (mAuth.getCurrentUser() == null) {
                initializeTTS();
            }
        }

        optionYes.setOnClickListener(v -> handleUserResponse("yes"));
        optionNo.setOnClickListener(v -> handleUserResponse("no"));

        loginButton.setOnClickListener(v -> navigateToMainActivity());
        getStartedButton.setOnClickListener(v -> navigateToSignUpActivity());

        micButton.setOnClickListener(v -> startListening());

        cancelButton.setOnClickListener(v -> {
            voiceAssistantEnabled = false;
            Toast.makeText(IntroActivity.this, "Voice assistant disabled", Toast.LENGTH_SHORT).show();
        });

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(v -> {
            if (v != promptText) {
                voiceAssistantEnabled = false;
                Toast.makeText(IntroActivity.this, "Voice assistant disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeTTS() {
        executorService.execute(() -> {
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        runOnUiThread(() -> {
                            Toast.makeText(IntroActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Language not supported");
                        });
                    } else {
                        Log.d(TAG, "TTS Initialized successfully");
                        runOnUiThread(this::askForVoiceAssistant);
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(IntroActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "TTS Initialization failed");
                    });
                }
            });
        });
    }

    private void askForVoiceAssistant() {
        speak("Do you want to use the voice assistant? Please say yes or no.");
        startListening();
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            Log.d(TAG, "Speaking: " + text);
        }
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            speechRecognitionLauncher.launch(intent);
            Log.d(TAG, "Listening started...");
        } catch (Exception e) {
            Log.e(TAG, "Speech recognition not supported", e);
            Toast.makeText(this, "Your device does not support Speech to Text", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= 3) {
            voiceAssistantEnabled = false;
            Toast.makeText(IntroActivity.this, "Voice assistant disabled after 3 failed attempts", Toast.LENGTH_SHORT).show();
        } else {
            startListening();
        }
    }

    private void handleUserResponse(String response) {
        Log.d(TAG, "Handling user response: " + response);
        failedAttempts = 0;

        switch (response) {
            case "yes":
                voiceAssistantEnabled = true;
                speak("Voice assistant enabled. Please say login to log in or sign up to create a new account.");
                startListening();
                break;

            case "no":
                voiceAssistantEnabled = false;
                speak("Voice assistant disabled. Please use the app manually.");
                break;

            case "login":
                if (voiceAssistantEnabled) {
                    navigateToMainActivityWithVoice();
                } else {
                    navigateToMainActivity();
                }
                break;

            case "sign up":
                if (voiceAssistantEnabled) {
                    navigateToSignUpActivity();
                }
                break;

            default:
                speak("I didn't understand. Please say login or sign up.");
                handleFailedAttempt();
                break;
        }
    }

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

    private void navigateToMainActivityWithVoice() {
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        intent.putExtra("voiceAssistantEnabled", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            voiceAssistantEnabled = false;
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        executorService.shutdown(); // Shut down the ExecutorService
        super.onDestroy();
    }
}
