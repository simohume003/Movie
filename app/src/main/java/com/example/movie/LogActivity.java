package com.example.movie;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<MovieLog> logList = new ArrayList<>();
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        recyclerView = findViewById(R.id.recyclerViewLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserLogs();
        } else {
            Log.e("FIREBASE", "No logged-in user found");
        }
    }

    private void loadUserLogs() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        MovieLog log = doc.toObject(MovieLog.class);
                        logList.add(log);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Error loading user logs", e));
    }

    // MovieLog class
    public static class MovieLog {
        public String title;
        public long timestamp;

        public MovieLog() {} // empty constructor required by Firestore
    }
}
