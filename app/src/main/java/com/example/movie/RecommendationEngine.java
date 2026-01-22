package com.example.movie;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecommendationEngine {

    private static final String TAG = "RECO_ENGINE";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface RecommendationCallback {
        void onResult(List<String> movieTitles);
    }

    public void getRecommendations(@NonNull RecommendationCallback callback) {

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No logged-in user");
            callback.onResult(new ArrayList<>());
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        // Load mY liked movies rating >= 3
        db.collection("users")
                .document(currentUserId)
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(myMoviesSnapshot -> {

                    Set<String> myLikedMovies = new HashSet<>();

                    for (DocumentSnapshot doc : myMoviesSnapshot.getDocuments()) {
                        String title = doc.getString("title");
                        Long rating = doc.getLong("rating");

                        if (title == null || rating == null) continue;

                        if (rating >= 3) {
                            myLikedMovies.add(title);
                        }
                    }

                    Log.d(TAG, "My liked movies (rating >= 3): " + myLikedMovies);

                    if (myLikedMovies.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    //  scan all watchedMovies
                    db.collectionGroup("watchedMovies")
                            .get()
                            .addOnSuccessListener(allMoviesSnapshot -> {

                                Map<String, Integer> scoreMap = new HashMap<>();

                                for (DocumentSnapshot doc : allMoviesSnapshot.getDocuments()) {

                                    DocumentReference ref = doc.getReference();
                                    if (ref == null ||
                                            ref.getParent() == null ||
                                            ref.getParent().getParent() == null) {
                                        Log.w(TAG, "Skipping orphan watchedMovie doc");
                                        continue;
                                    }

                                    String userId = ref.getParent().getParent().getId();

                                    // skip myself
                                    if (currentUserId.equals(userId)) continue;

                                    String title = doc.getString("title");
                                    Long rating = doc.getLong("rating");

                                    if (title == null || rating == null) continue;

                                    if (rating < 3) continue;

                                    if (myLikedMovies.contains(title)) continue;

                                    // collaborative signal
                                    scoreMap.put(title, scoreMap.getOrDefault(title, 0) + 1);
                                }

                                List<String> recommendations = new ArrayList<>(scoreMap.keySet());

                                recommendations.sort((a, b) ->
                                        scoreMap.get(b) - scoreMap.get(a)
                                );

                                if (recommendations.size() > 10) {
                                    recommendations = recommendations.subList(0, 10);
                                }

                                Log.d(TAG, "Final recommendations (rating-aware): " + recommendations);

                                callback.onResult(recommendations);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed collectionGroup query", e);
                                callback.onResult(new ArrayList<>());
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed loading my watched movies", e);
                    callback.onResult(new ArrayList<>());
                });
    }
}
