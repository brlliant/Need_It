package com.example.needit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrowseFragment extends Fragment {

    private WebView webView;
    private boolean isWebViewAvailable = false;
    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        webView = view.findViewById(R.id.webview);
        isWebViewAvailable = true;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        if (isWebViewAvailable) {
            executorService.submit(new SetupWebViewTask(this));
        }
    }

    private static class SetupWebViewTask implements Runnable {
        private final WeakReference<BrowseFragment> fragmentReference;

        SetupWebViewTask(BrowseFragment fragment) {
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            try {
                // Placeholder for background setup work, if any

                BrowseFragment fragment = fragmentReference.get();
                if (fragment == null || !fragment.isWebViewAvailable) {
                    return;
                }

                assert fragment.getActivity() != null;
                fragment.getActivity().runOnUiThread(() -> {
                    try {
                        WebSettings webSettings = fragment.webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setAllowFileAccess(false); // Disable file access
                        webSettings.setAllowContentAccess(false); // Disable content access

                        fragment.webView.setWebViewClient(new WebViewClient());
                        fragment.webView.loadUrl("https://search.brave.com");
                    } catch (Exception e) {
                        fragment.showError(e.getMessage());
                    }
                });
            } catch (Exception e) {
                BrowseFragment fragment = fragmentReference.get();
                if (fragment != null) {
                    fragment.showError(e.getMessage());
                }
            }
        }
    }

    private void showError(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Error setting up WebView: " + message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isWebViewAvailable = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
