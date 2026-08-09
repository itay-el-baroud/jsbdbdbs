package com.example.ssderive;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private WebView webView;
    private View errorLayout;
    private Button retryButton;
    private ProgressBar progressBar;
    private TextView errorText;

    private static final String LOGIN_URL = "https://media-note.ct.ws/login.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If token exists go to HomeActivity
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.getToken() != null && !tokenManager.getToken().isEmpty()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        webView = findViewById(R.id.webview);
        errorLayout = findViewById(R.id.web_error_layout);
        retryButton = findViewById(R.id.btn_retry);
        progressBar = findViewById(R.id.progress_bar);
        errorText = findViewById(R.id.error_text);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new AppWebViewClient());

        retryButton.setOnClickListener(v -> {
            showWebView();
            webView.reload();
        });

        loadLogin();
    }

    private void loadLogin() {
        webView.loadUrl(LOGIN_URL);
    }

    private void showError(String message) {
        webView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorText.setText(message);
        progressBar.setVisibility(View.GONE);
    }

    private void showWebView() {
        errorLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private class AppWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Intercept custom scheme redirect
            if (url != null && url.startsWith("myapp://auth-success")) {
                Uri uri = Uri.parse(url);
                String token = uri.getQueryParameter("token");
                if (token != null && !token.isEmpty()) {
                    TokenManager tokenManager = new TokenManager(LoginActivity.this);
                    tokenManager.saveToken(token);

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showError("Connection problem");
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            // Show custom error UI
            showError("Connection problem");
        }
    }
    }
