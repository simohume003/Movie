package com.example.movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNowPlaying;
    private RecyclerView recyclerViewUpcoming;
    private RecyclerView recyclerViewSearchResults;
    private SearchView searchView;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewNowPlaying = findViewById(R.id.recyclerViewNowPlaying);
        recyclerViewUpcoming = findViewById(R.id.recyclerViewUpcoming);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        searchView = findViewById(R.id.searchView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewNowPlaying.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewUpcoming.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewSearchResults.setVisibility(View.GONE);

        fetchNowPlayingMovies();
        fetchUpcomingMovies();
        setupSearch();
        setupBottomNavigation(bottomNavigationView);
    }

    private void fetchNowPlayingMovies() {
        RetrofitClient.getInstance()
                .getNowPlaying(API_KEY, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Movie> movies = response.body().getResults();
                            recyclerViewNowPlaying.setAdapter(new MovieAdapter(movies, true));
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
                            List<Movie> movies = response.body().getResults();
                            recyclerViewUpcoming.setAdapter(new MovieAdapter(movies, true));
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
                    recyclerViewNowPlaying.setVisibility(View.GONE);
                    recyclerViewUpcoming.setVisibility(View.GONE);
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    searchMovies(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
                            recyclerViewSearchResults.setAdapter(new MovieAdapter(movies, false));
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Search failure: " + t.getMessage());
                    }
                });
    }

    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                recyclerViewSearchResults.setVisibility(View.GONE);
                recyclerViewNowPlaying.setVisibility(View.VISIBLE);
                recyclerViewUpcoming.setVisibility(View.VISIBLE);
                recyclerViewNowPlaying.scrollToPosition(0);
                recyclerViewUpcoming.scrollToPosition(0);
                searchView.setQuery("", false);
                searchView.clearFocus();
                return true;
            } else if (id == R.id.nav_log) {
                startActivity(new Intent(this, LogActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            }
            return false;
        });
    }
}
