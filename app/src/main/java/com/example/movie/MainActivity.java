package com.example.movie;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMovies);

        // Show 2 movies per row
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        fetchMovies();
    }

    private void fetchMovies() {
        // Fetch Now Playing
        RetrofitClient.getInstance()
                .getNowPlaying(API_KEY, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> nowPlayingMovies = response.body().getResults();
                            recyclerView.setAdapter(new MovieAdapter(nowPlayingMovies));
                            Log.d("API_DEBUG", "Now Playing: " + nowPlayingMovies.size() + " movies");
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Failure: " + t.getMessage());
                    }
                });

        // Fetch Upcoming / Recently Released
        RetrofitClient.getInstance()
                .getUpcoming(API_KEY, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> upcomingMovies = response.body().getResults();
                            // You could merge both lists if you want to show together
                            Log.d("API_DEBUG", "Upcoming: " + upcomingMovies.size() + " movies");
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Failure: " + t.getMessage());
                    }
                });
    }

}
