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
            fetchWatchProviders(movieId);
        } else {
            title.setText("No Movie Selected");
            streaming.setText("");
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

                            title.setText(movie.getTitle() != null ? movie.getTitle() : "No Title");
                            overview.setText(movie.getOverview() != null ? movie.getOverview() : "No overview available");
                            runtime.setText("Runtime: " + (movie.getRuntime() > 0 ? movie.getRuntime() + " min" : "N/A"));
                            rating.setText("Rating: " + movie.getVoteAverage());

                            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
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
                .getWatchProviders(movieId, API_KEY)
                .enqueue(new Callback<WatchProviderResponse>() {
                    @Override
                    public void onResponse(Call<WatchProviderResponse> call, Response<WatchProviderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WatchProviderResponse.CountryProviders providersIE = response.body().getResults().get("IE"); // Ireland
                            WatchProviderResponse.CountryProviders providersUS = response.body().getResults().get("US"); // US

                            StringBuilder sb = new StringBuilder();
                            if (providersIE != null && providersIE.getFlatrate() != null) {
                                for (WatchProviderResponse.Provider p : providersIE.getFlatrate()) {
                                    sb.append(p.getProviderName()).append(" ");
                                }
                                streaming.setText("Available in Ireland: " + sb.toString().trim());
                            } else if (providersUS != null && providersUS.getFlatrate() != null) {
                                sb.setLength(0);
                                for (WatchProviderResponse.Provider p : providersUS.getFlatrate()) {
                                    sb.append(p.getProviderName()).append(" ");
                                }
                                streaming.setText("Available in US: " + sb.toString().trim());
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
}
