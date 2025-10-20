package com.example.movie;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, overview, runtime, rating, streaming, cexPrice;
    private Button btnMarkWatched;

    private static final String TMDB_API_KEY = "929a54210220df3134196d46a16375b1";
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // UI
        poster = findViewById(R.id.detailPoster);
        title = findViewById(R.id.detailTitle);
        overview = findViewById(R.id.detailOverview);
        runtime = findViewById(R.id.detailRuntime);
        rating = findViewById(R.id.detailRating);
        streaming = findViewById(R.id.detailStreaming);
        cexPrice = findViewById(R.id.detailCexPrice);
        btnMarkWatched = findViewById(R.id.btnMarkWatched);

        // Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get intent extras
        int movieId = getIntent().getIntExtra("MOVIE_ID", 0);
        String movieTitle = getIntent().getStringExtra("MOVIE_TITLE");

        if (movieId > 0 && movieTitle != null) {
            fetchMovieDetails(movieId);
            fetchWatchProviders(movieId);
            fetchCexPrice(movieTitle);

            btnMarkWatched.setOnClickListener(v -> markMovieAsWatched(movieTitle));
        } else {
            title.setText("No Movie Selected");
        }
    }

    private void fetchMovieDetails(int movieId) {
        RetrofitClient.getInstance()
                .getMovieDetails(movieId, TMDB_API_KEY, "en-US")
                .enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Movie movie = response.body();
                            title.setText(movie.getTitle() != null ? movie.getTitle() : "No Title");
                            overview.setText(movie.getOverview() != null ? movie.getOverview() : "No overview");
                            runtime.setText("Runtime: " + (movie.getRuntime() > 0 ? movie.getRuntime() + " min" : "N/A"));
                            rating.setText("Rating: " + movie.getVoteAverage());

                            if (movie.getPosterPath() != null) {
                                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                                Glide.with(poster.getContext())
                                        .load(posterUrl)
                                        .into(poster);
                            }
                        } else {
                            title.setText("Error loading details");
                        }
                    }

                    @Override
                    public void onFailure(Call<Movie> call, Throwable t) {
                        title.setText("Error loading details");
                    }
                });
    }

    private void fetchWatchProviders(int movieId) {
        RetrofitClient.getInstance()
                .getWatchProviders(movieId, TMDB_API_KEY)
                .enqueue(new Callback<WatchProviderResponse>() {
                    @Override
                    public void onResponse(Call<WatchProviderResponse> call, Response<WatchProviderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WatchProviderResponse wpResponse = response.body();
                            WatchProviderResponse.CountryProviders providers = wpResponse.getResults().get("IE");

                            if (providers != null && providers.getFlatrate() != null) {
                                StringBuilder sb = new StringBuilder();
                                for (WatchProviderResponse.Provider p : providers.getFlatrate()) {
                                    sb.append(p.getProviderName()).append(", ");
                                }
                                String providerText = sb.toString();
                                if (providerText.endsWith(", ")) providerText = providerText.substring(0, providerText.length() - 2);
                                streaming.setText("Available on: " + providerText);
                            } else {
                                streaming.setText("Not available on streaming platforms");
                            }
                        } else {
                            streaming.setText("Streaming info not available");
                        }
                    }

                    @Override
                    public void onFailure(Call<WatchProviderResponse> call, Throwable t) {
                        streaming.setText("Streaming info not available");
                    }
                });
    }

    private void fetchCexPrice(String movieTitle) {
        new Thread(() -> {
            try {
                String searchQuery = movieTitle.replace(" ", "+") + "+DVD";
                Document doc = Jsoup.connect("https://uk.webuy.com/search?search=" + searchQuery).get();
                Element priceElement = null;

                for (Element product : doc.select(".product-title")) {
                    if (product.text().equalsIgnoreCase(movieTitle)) {
                        priceElement = product.parent().selectFirst(".product-price");
                        break;
                    }
                }

                final String priceText = priceElement != null ? priceElement.text() : "Nothing found";
                runOnUiThread(() -> cexPrice.setText("CEX Price: " + priceText));
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> cexPrice.setText("CEX Price: Nothing found"));
            }
        }).start();
    }

    private void markMovieAsWatched(String movieTitle) {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to mark movies", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> watchedMovie = new HashMap<>();
        watchedMovie.put("title", movieTitle);
        watchedMovie.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(currentUser.getUid())
                .collection("watchedMovies")
                .add(watchedMovie)
                .addOnSuccessListener(docRef -> Toast.makeText(this, movieTitle + " marked as watched!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to mark as watched", Toast.LENGTH_SHORT).show());
    }
}
