package com.example.ssderive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private TextView tvProfile;
    private Button btnLogout;
    private ProgressBar progressBar;

    private static final String PROFILE_URL = "https://media-note.ct.ws/profile.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        tvProfile = findViewById(R.id.tv_profile);
        btnLogout = findViewById(R.id.btn_logout);
        progressBar = findViewById(R.id.progress_bar_home);

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        fetchProfile(token);
    }

    private void fetchProfile(String token) {
        progressBar.setVisibility(View.VISIBLE);
        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvProfile.setText("Failed to load profile: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body() != null ? response.body().string() : "Empty response";
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        tvProfile.setText(body);
                    } else {
                        tvProfile.setText("Server error: " + response.code() + "\n" + body);
                    }
                });
            }
        });
    }
}
