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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    private RecyclerView recyclerViewNowPlaying;
    private RecyclerView recyclerViewUpcoming;
    private RecyclerView recyclerViewRecommended;
    private RecyclerView recyclerViewSearchResults;

    private SearchView searchView;


    private MovieAdapter recommendedAdapter;
    private final List<Movie> recommendedMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerViewNowPlaying = findViewById(R.id.recyclerViewNowPlaying);
        recyclerViewUpcoming = findViewById(R.id.recyclerViewUpcoming);
        recyclerViewRecommended = findViewById(R.id.recyclerViewRecommended);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        searchView = findViewById(R.id.searchView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        recyclerViewNowPlaying.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        recyclerViewUpcoming.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        recyclerViewRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewSearchResults.setVisibility(View.GONE);


        recommendedAdapter = new MovieAdapter(recommendedMovies, true);
        recyclerViewRecommended.setAdapter(recommendedAdapter);

        // Data
        fetchNowPlayingMovies();
        fetchUpcomingMovies();
        loadRecommendations();

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
                            recyclerViewNowPlaying.setAdapter(
                                    new MovieAdapter(response.body().getResults(), true)
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API", "Now playing failed", t);
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
                            recyclerViewUpcoming.setAdapter(
                                    new MovieAdapter(response.body().getResults(), true)
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API", "Upcoming failed", t);
                    }
                });
    }



    private void loadRecommendations() {

        RecommendationEngine engine = new RecommendationEngine();

        engine.getRecommendations(movieTitles -> {

            Log.d("RECO", "Titles from engine: " + movieTitles);

            if (movieTitles == null || movieTitles.isEmpty()) {
                recyclerViewRecommended.setVisibility(View.GONE);
                return;
            }

            recyclerViewRecommended.setVisibility(View.VISIBLE);
            recommendedMovies.clear();
            recommendedAdapter.notifyDataSetChanged();

            for (String title : movieTitles) {
                RetrofitClient.getInstance()
                        .searchMovies(API_KEY, title, "en-US", 1)
                        .enqueue(new Callback<MovieResponse>() {
                            @Override
                            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                                if (response.isSuccessful()
                                        && response.body() != null
                                        && !response.body().getResults().isEmpty()) {

                                    recommendedMovies.add(response.body().getResults().get(0));
                                    recommendedAdapter.notifyItemInserted(
                                            recommendedMovies.size() - 1
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Call<MovieResponse> call, Throwable t) {
                                Log.e("RECO", "TMDB lookup failed for " + title, t);
                            }
                        });
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
                    recyclerViewRecommended.setVisibility(View.GONE);
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
                            recyclerViewSearchResults.setAdapter(
                                    new MovieAdapter(response.body().getResults(), false)
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API", "Search failed", t);
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
                recyclerViewRecommended.setVisibility(View.VISIBLE);

                recyclerViewNowPlaying.scrollToPosition(0);
                recyclerViewUpcoming.scrollToPosition(0);
                recyclerViewRecommended.scrollToPosition(0);

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
