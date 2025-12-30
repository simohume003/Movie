package com.example.movie;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private LinearLayout statsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        statsContainer = findViewById(R.id.statsContainer);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) loadStats();
    }

    private void loadStats() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(snap -> {
                    int totalMovies = snap.size();
                    int totalMinutes = 0;
                    Map<String, Integer> genreCount = new HashMap<>();
                    Map<String, Integer> serviceCount = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        Long runtime = doc.getLong("runtime");
                        String genre = doc.getString("genre");
                        String service = doc.getString("service");

                        if (runtime != null) totalMinutes += runtime;
                        if (genre != null)
                            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                        if (service != null)
                            serviceCount.put(service, serviceCount.getOrDefault(service, 0) + 1);
                    }

                    String topGenre = genreCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");

                    String topService = serviceCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");

                    addStatPanel("🎬 Total Movies", totalMovies + " watched");
                    addStatPanel("⏱ Total Hours", (totalMinutes / 60) + " hrs");
                    addStatPanel("📺 Top Genre", topGenre);
                    addStatPanel("🍿 Top Streaming Service", topService);
                });
    }

    private void addStatPanel(String title, String value) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF6A1B9A, 0xFFFF6F00});
        gradient.setCornerRadius(20);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(32, 32, 32, 32);
        panel.setBackground(gradient);
        panel.setElevation(8);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(16f);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFFFFFFF);
        valueView.setTextSize(22f);
        valueView.setPadding(0, 8, 0, 0);

        panel.addView(titleView);
        panel.addView(valueView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 24, 0, 0);

        statsContainer.addView(panel, params);
    }
}
