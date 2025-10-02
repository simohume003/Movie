package com.example.movie;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, overview, runtime, rating, streaming;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        poster = findViewById(R.id.detailPoster);
        title = findViewById(R.id.detailTitle);
        overview = findViewById(R.id.detailOverview);
        runtime = findViewById(R.id.detailRuntime);
        rating = findViewById(R.id.detailRating);
        streaming = findViewById(R.id.detailStreaming);

        int movieId = getIntent().getIntExtra("MOVIE_ID", 0);

        if (movieId > 0) {
            fetchMovieDetails(movieId);
        } else {
            title.setText("No Movie Selected");
        }
    }

    private void fetchMovieDetails(int movieId) {
        RetrofitClient.getInstance()
                .getMovieDetails(movieId, API_KEY, "en-US")
                .enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Movie movie = response.body();

                            title.setText(movie.getTitle());
                            overview.setText(movie.getOverview() != null ? movie.getOverview() : "No overview available");
                            runtime.setText("Runtime: " + movie.getRuntime() + " min");
                            rating.setText("Rating: " + movie.getVoteAverage());

                            // Poster only
                            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                                Glide.with(poster.getContext())
                                        .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                                        .into(poster);
                            }

                            streaming.setText("Streaming info coming soon...");
                        }
                    }

                    @Override
                    public void onFailure(Call<Movie> call, Throwable t) {
                        title.setText("Error loading movie details");
                    }
                });
    }
}
