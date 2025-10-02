package com.example.movie;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMovies);
        searchView = findViewById(R.id.searchView);

        // Grid layout: 2 movies per row
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        fetchNowPlayingMovies();
        fetchUpcomingMovies(); // optional logging for now

        setupSearch();
    }

    private void fetchNowPlayingMovies() {
        RetrofitClient.getInstance()
                .getNowPlaying(API_KEY, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> movies = response.body().getResults();
                            recyclerView.setAdapter(new MovieAdapter(movies));
                            Log.d("API_DEBUG", "Now Playing: " + movies.size() + " movies");
                        } else {
                            Log.e("API_ERROR", "Now Playing error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Now Playing failure: " + t.getMessage());
                    }
                });
    }

    private void fetchUpcomingMovies() {
        RetrofitClient.getInstance()
                .getUpcoming(API_KEY, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> upcomingMovies = response.body().getResults();
                            Log.d("API_DEBUG", "Upcoming: " + upcomingMovies.size() + " movies");
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Upcoming failure: " + t.getMessage());
                    }
                });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchMovies(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: live search while typing
                return false;
            }
        });
    }

    private void searchMovies(String query) {
        RetrofitClient.getInstance()
                .searchMovies(API_KEY, query, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> movies = response.body().getResults();
                            recyclerView.setAdapter(new MovieAdapter(movies));
                            Log.d("API_DEBUG", "Search results: " + movies.size() + " movies for query: " + query);
                        } else {
                            Log.e("API_ERROR", "Search error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Search failure: " + t.getMessage());
                    }
                });
    }
}
