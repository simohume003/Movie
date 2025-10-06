package com.example.movie;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    private FirebaseFirestore db;
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

        // Load watched movies
        db.collection("watchedMovies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        MovieLog log = doc.toObject(MovieLog.class);
                        logList.add(log);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("FIREBASE", "Error loading logs", e)
                );
    }

    public static class MovieLog {
        public String title;
        public String note;
        public long timestamp;

        public MovieLog() {} // empty constructor required by Firestore
    }
}
