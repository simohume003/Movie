package com.example.movie;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private LinearLayout statsContainer;
    private Map<String,Integer> serviceCount= new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        statsContainer = findViewById(R.id.statsContainer);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) loadStats();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_log) {
                startActivity(new Intent(this, LogActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            }
            else if (id == R.id.nav_music) {
                startActivity(new Intent(this, MusicToMovieActivity.class));
                return true;
            }

            return false;
        });


        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(StatsActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });



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
                    serviceCount.clear();

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

                    String topGenre = "N/A";
                    int maxGenre = 0;

                    for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
                        if (entry.getValue() > maxGenre) {
                            maxGenre = entry.getValue();
                            topGenre = entry.getKey();
                        }
                    }


                    String topService = serviceCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");

                    addExpandableStatPanel("🎬 Total Movies", totalMovies + " watched");
                    addExpandableStatPanel("⏱ Total Hours", (totalMinutes / 60) + " hrs");
                    LinearLayout genrePanel =
                            addExpandableStatPanel("📺 Top Genre", topGenre);
                    populateTextBreakdown(genrePanel, genreCount);

                    LinearLayout servicePanel =
                            addExpandableStatPanel("🍿 Top Streaming Service", topService);
                    populateTextBreakdown(servicePanel, serviceCount);

                });
    }

    private LinearLayout addExpandableStatPanel(
            String title,
            String value
    ) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF6A1B9A, 0xFFFF6F00});
        gradient.setCornerRadius(20);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32, 32, 32, 32);
        card.setBackground(gradient);
        card.setElevation(8);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(16f);

        TextView valueView = new TextView(this);
        valueView.setText(value + "  ▶");
        valueView.setTextColor(0xFFFFFFFF);
        valueView.setTextSize(22f);
        valueView.setPadding(0, 8, 0, 0);


        LinearLayout expandableContainer = new LinearLayout(this);
        expandableContainer.setOrientation(LinearLayout.VERTICAL);
        expandableContainer.setVisibility(View.GONE);
        expandableContainer.setPadding(0, 16, 0, 0);

        card.addView(titleView);
        card.addView(valueView);
        card.addView(expandableContainer);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 24, 0, 0);

        statsContainer.addView(card, params);

        card.setOnClickListener(v -> {
            boolean expanded = expandableContainer.getVisibility() == View.VISIBLE;
            expandableContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
            valueView.setText(value + (expanded ? "  ▶" : "  ▼"));
        });

        return expandableContainer;
    }

    private void addStatPanel(String title, String value, Runnable onClick) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF6A1B9A, 0xFFFF6F00});
        gradient.setCornerRadius(20);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(32, 32, 32, 32);
        panel.setBackground(gradient);
        panel.setElevation(8);

        panel.setOnClickListener(v -> onClick.run());

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(16f);

        TextView valueView = new TextView(this);
        valueView.setText(value + "  ▶");
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
    private void showStreamingBreakdown() {
        if (serviceCount.isEmpty()) return;


        List<Map.Entry<String, Integer>> sorted =
                new ArrayList<>(serviceCount.entrySet());

        sorted.sort((a, b) -> b.getValue() - a.getValue());

        StringBuilder breakdown = new StringBuilder();

        for (Map.Entry<String, Integer> entry : sorted) {
            breakdown.append(entry.getKey())
                    .append(" — ")
                    .append(entry.getValue())
                    .append(" movies\n");
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Streaming Breakdown")
                .setMessage(breakdown.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void populateTextBreakdown(
            LinearLayout container,
            Map<String, Integer> data
    ) {
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            TextView tv = new TextView(this);
            tv.setText("• " + entry.getKey() + ": " + entry.getValue());
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(14f);
            tv.setPadding(0, 4, 0, 4);

            container.addView(tv);
        }
    }



}
