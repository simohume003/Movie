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
    public interface ActorCallback {
        void onResult(List<String> actors);
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
    public static class DiscoverRequest {
        public final String genre;
        public final String service;
        public final String heading;

        public DiscoverRequest(String heading, String genre, String service) {
            this.heading = heading;
            this.genre = genre;
            this.service = service;
        }
    }
    public List<DiscoverRequest> buildPersonalisedDiscoverRequests(
            String favouriteGenre,
            String topService,
            String leastUsedService
    ) {
        List<DiscoverRequest> list = new ArrayList<>();

        if (favouriteGenre == null || topService == null) return list;

        list.add(new DiscoverRequest(
                "Because you like " + favouriteGenre + " on " + topService,
                favouriteGenre,
                topService
        ));

        if (leastUsedService != null && !leastUsedService.equalsIgnoreCase(topService)) {
            list.add(new DiscoverRequest(
                    "You love " + favouriteGenre + " — try it on " + leastUsedService,
                    favouriteGenre,
                    leastUsedService
            ));
        }

        return list;
    }
    public interface DiscoverCallback {
        void onResult(List<DiscoverRequest> requests);
    }
    public void getPersonalisedDiscoverRequests(@NonNull DiscoverCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onResult(new ArrayList<>());
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(snap -> {

                    Map<String, Integer> genreCount = new HashMap<>();
                    Map<String, Integer> serviceCount = new HashMap<>();

                    for (DocumentSnapshot doc : snap) {
                        String genre = doc.getString("genre");
                        String service = doc.getString("service");
                        Long rating = doc.getLong("rating");

                        if (rating == null || rating < 3) continue;

                        if (genre != null) {
                            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                        }
                        if (service != null && !service.equalsIgnoreCase("Other")) {
                            serviceCount.put(service, serviceCount.getOrDefault(service, 0) + 1);
                        }
                    }

                    if (genreCount.isEmpty() || serviceCount.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    String favGenre = maxKey(genreCount);
                    String topService = maxKey(serviceCount);
                    String leastService = minKey(serviceCount);

                    callback.onResult(
                            buildPersonalisedDiscoverRequests(
                                    favGenre,
                                    topService,
                                    leastService
                            )
                    );
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    private String maxKey(Map<String, Integer> map) {
        String bestKey = "N/A";
        int bestVal = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() != null && e.getValue() > bestVal) {
                bestVal = e.getValue();
                bestKey = e.getKey();
            }
        }
        return bestKey;
    }

    private String minKey(Map<String, Integer> map) {
        String bestKey = "N/A";
        int bestVal = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() != null && e.getValue() < bestVal) {
                bestVal = e.getValue();
                bestKey = e.getKey();
            }
        }
        return bestKey;
    }

    private List<String> sortTop(Map<String, Integer> scoreMap, int limit) {
        List<String> list = new ArrayList<>(scoreMap.keySet());
        list.sort((a, b) -> scoreMap.get(b) - scoreMap.get(a));

        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }
    public interface DirectorCallback {
        void onResult(List<String> directors);
    }

    public void getTopDirectors(@NonNull DirectorCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onResult(new ArrayList<>());
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(snap -> {

                    Map<String, Integer> directorCount = new HashMap<>();

                    for (DocumentSnapshot doc : snap) {
                        String director = doc.getString("director");
                        Long rating = doc.getLong("rating");

                        if (director == null || rating == null || rating < 3) continue;

                        directorCount.put(
                                director,
                                directorCount.getOrDefault(director, 0) + 1
                        );
                    }

                    if (directorCount.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    List<String> sorted = new ArrayList<>(directorCount.keySet());
                    sorted.sort((a, b) -> directorCount.get(b) - directorCount.get(a));

                    if (sorted.size() > 3) {
                        sorted = sorted.subList(0, 3);
                    }

                    callback.onResult(sorted);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }
    public void getTopActor(@NonNull ActorCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onResult(new ArrayList<>());
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(snap -> {

                    Map<String, Integer> actorCount = new HashMap<>();

                    for (DocumentSnapshot doc : snap) {
                        String actor = doc.getString("actor");
                        Long rating = doc.getLong("rating");

                        if (actor == null || rating == null || rating < 3) continue;

                        actorCount.put(
                                actor,
                                actorCount.getOrDefault(actor, 0) + 1
                        );
                    }

                    if (actorCount.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    List<String> sorted = new ArrayList<>(actorCount.keySet());
                    sorted.sort((a, b) -> actorCount.get(b) - actorCount.get(a));

                    if (sorted.size() > 3) {
                        sorted = sorted.subList(0, 3);
                    }

                    callback.onResult(sorted);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }
    public void getSimilarUserRecommendations(@NonNull RecommendationCallback callback) {

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No logged-in user");
            callback.onResult(new ArrayList<>());
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(currentUserId)
                .collection("watchedMovies")
                .get()
                .addOnSuccessListener(myMoviesSnapshot -> {

                    Set<String> myLikedMovies = new HashSet<>();
                    Map<String, Integer> myGenreCount = new HashMap<>();

                    for (DocumentSnapshot doc : myMoviesSnapshot.getDocuments()) {
                        String title = doc.getString("title");
                        String genre = doc.getString("genre");
                        Long rating = doc.getLong("rating");

                        if (title == null || rating == null) continue;

                        if (rating >= 3) {
                            myLikedMovies.add(title);

                            if (genre != null) {
                                myGenreCount.put(genre, myGenreCount.getOrDefault(genre, 0) + 1);
                            }
                        }
                    }

                    if (myLikedMovies.isEmpty() || myGenreCount.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    String favouriteGenre = maxKey(myGenreCount);

                    db.collectionGroup("watchedMovies")
                            .get()
                            .addOnSuccessListener(allMoviesSnapshot -> {

                                Map<String, Integer> scoreMap = new HashMap<>();

                                for (DocumentSnapshot doc : allMoviesSnapshot.getDocuments()) {

                                    DocumentReference ref = doc.getReference();
                                    if (ref == null ||
                                            ref.getParent() == null ||
                                            ref.getParent().getParent() == null) {
                                        continue;
                                    }

                                    String userId = ref.getParent().getParent().getId();

                                    if (currentUserId.equals(userId)) continue;

                                    String title = doc.getString("title");
                                    String genre = doc.getString("genre");
                                    Long rating = doc.getLong("rating");

                                    if (title == null || rating == null) continue;
                                    if (rating < 3) continue;
                                    if (myLikedMovies.contains(title)) continue;
                                    if (genre == null || !genre.equalsIgnoreCase(favouriteGenre)) continue;

                                    scoreMap.put(title, scoreMap.getOrDefault(title, 0) + 1);
                                }

                                List<String> recommendations = new ArrayList<>(scoreMap.keySet());

                                recommendations.sort((a, b) ->
                                        scoreMap.get(b) - scoreMap.get(a)
                                );

                                if (recommendations.size() > 10) {
                                    recommendations = recommendations.subList(0, 10);
                                }

                                Log.d(TAG, "Similar users also like (" + favouriteGenre + "): " + recommendations);

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
