package com.example.movie;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalMovies, tvTotalHours, tvTopGenre;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotalMovies = findViewById(R.id.tvTotalMovies);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        tvTopGenre = findViewById(R.id.tvTopGenre);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            loadStats();
        } else {
            tvTotalMovies.setText("Please log in to view your stats.");
        }
    }

    private void loadStats() {
        db.collection("users")
                .document(user.getUid())
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(query -> {
                    int totalMovies = query.size();
                    int totalMinutes = 0;
                    Map<String, Integer> genreCount = new HashMap<>();

                    for (QueryDocumentSnapshot doc : query) {
                        Long runtime = doc.getLong("runtime");
                        String genre = doc.getString("genre");

                        if (runtime != null)
                            totalMinutes += runtime;

                        if (genre != null) {
                            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                        }
                    }

                    int hours = totalMinutes / 60;
                    String topGenre = "N/A";

                    if (!genreCount.isEmpty()) {
                        topGenre = genreCount.entrySet()
                                .stream()
                                .max(Map.Entry.comparingByValue())
                                .get()
                                .getKey();
                    }

                    tvTotalMovies.setText("Total Movies Watched: " + totalMovies);
                    tvTotalHours.setText("Estimated Hours Watched: " + hours + "h");
                    tvTopGenre.setText("Most Watched Genre: " + topGenre);
                })
                .addOnFailureListener(e -> Log.e("STATS", "Error fetching stats", e));
    }
}
