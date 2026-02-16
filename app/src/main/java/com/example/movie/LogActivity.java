package com.example.movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private final List<MovieLog> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        recyclerView = findViewById(R.id.recyclerViewLogs);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserLogs();
        } else {
            Log.e("LOG", "No logged-in user");
        }
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

    }

    private void loadUserLogs() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(snapshot -> {
                    logList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        MovieLog log = doc.toObject(MovieLog.class);
                        logList.add(log);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("LOG", "Failed to load logs", e));
    }


    public static class MovieLog {
        public String title;
        public float rating;
        public String service;
        public long timestamp;

        public MovieLog() {}
    }
}
