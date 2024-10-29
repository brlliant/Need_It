package com.example.needit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.speech.RecognizerIntent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeTextView;
    private ImageView profileImageView;
    private EditText searchEditText;
    private ImageView searchIcon;
    private ImageView micIcon;
    private RecyclerView searchResultsRecyclerView;
    private List<String> searchResults;
    private SearchResultsAdapter searchResultsAdapter;

    private ActivityResultLauncher<Intent> voiceRecognitionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeTextView = view.findViewById(R.id.welcome_text);
        profileImageView = view.findViewById(R.id.profile_image);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchIcon = view.findViewById(R.id.search_icon);
        micIcon = view.findViewById(R.id.mic_icon);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        LinearLayout shareLayout = view.findViewById(R.id.share_layout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileCustomizationActivity.class);
            startActivity(intent);
        });

        // Initialize voice recognition launcher
        voiceRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            searchEditText.setText(matches.get(0));
                        }
                    }
                }
        );

        micIcon.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            voiceRecognitionLauncher.launch(intent);
        });

        // Initialize the RecyclerView
        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(searchResults, this::onSearchResultClick);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Set click listener for the search icon
        searchIcon.setOnClickListener(v -> performSearch(searchEditText.getText().toString()));

        // Initialize share functionality
        shareLayout.setOnClickListener(v -> shareApp());

        return view;
    }

    private void loadUserData(String uid) {
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    welcomeTextView.setText("Hi, " + name + "!");
                } else {
                    Log.d(TAG, "No such document");
                    Toast.makeText(getActivity(), "No user data found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        // Dummy implementation for search, replace with actual search logic
        searchResults.clear();
        if (query.toLowerCase().contains("profile")) {
            searchResults.add("Profile Customization");
        } else if (query.toLowerCase().contains("settings")) {
            searchResults.add("Settings");
        } else {
            searchResults.add("No results found for " + query);
        }
        searchResultsAdapter.notifyDataSetChanged();
    }

    private void onSearchResultClick(String item) {
        Intent intent;
        switch (item) {
            case "Profile Customization":
                intent = new Intent(getActivity(), ProfileCustomizationActivity.class);
                startActivity(intent);
                break;
            case "Settings":
                // Replace with the appropriate activity for settings
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            case "Sign out": //Replace it wwith anything its just testing for the git.

            default:
                Toast.makeText(getActivity(), "Unknown option selected", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out the Need It app!");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}
