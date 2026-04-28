package com.example.movie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

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
    private RecyclerView recyclerViewPersonal1;
    private RecyclerView recyclerViewPersonal2;

    private SearchView searchView;
    private TextView noSearchResultsText;
    private View homeContentContainer;
    private boolean isSearching = false;
    private RecyclerView recyclerViewRecommended2;
    private TextView tvRecommended2;

    private final List<Movie> recommendedMovies2 = new ArrayList<>();
    private MovieAdapter recommendedAdapter2;


    private MovieAdapter recommendedAdapter;
    private final List<Movie> recommendedMovies = new ArrayList<>();
    private final List<Movie> personalMovies1=new ArrayList<>();
    private final List<Movie> personalMovies2=new ArrayList<>();
    private MovieAdapter personalAdapter1;
    private MovieAdapter personalAdapter2;


    private TextView tvPersonal1;
    private TextView tvPersonal2;
    private static final String PREFS = "movie_filters";
    private static final String KEY_NETFLIX = "filter_netflix";
    private static final String KEY_PRIME = "filter_prime";
    private static final String KEY_DISNEY = "filter_disney";

    private boolean filterNetflix;
    private boolean filterPrime;
    private boolean filterDisney;

    private RecyclerView recyclerViewDirectorBar;
    private TextView tvDirectorBar;
    private final List<Movie> directorMovies = new ArrayList<>();
    private MovieAdapter directorAdapter;

    private RecyclerView recyclerViewActorBar;
    private TextView tvActorBar;
    private final List<Movie> actorMovies = new ArrayList<>();
    private MovieAdapter actorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        filterNetflix = prefs.getBoolean(KEY_NETFLIX, false);
        filterPrime = prefs.getBoolean(KEY_PRIME, false);
        filterDisney = prefs.getBoolean(KEY_DISNEY, false);

        homeContentContainer = findViewById(R.id.homeContentContainer);
        recyclerViewRecommended2 = findViewById(R.id.recyclerViewRecommended2);
        tvRecommended2 = findViewById(R.id.tvRecommended2);


        recyclerViewNowPlaying = findViewById(R.id.recyclerViewNowPlaying);
        recyclerViewUpcoming = findViewById(R.id.recyclerViewUpcoming);
        recyclerViewRecommended = findViewById(R.id.recyclerViewRecommended);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        searchView = findViewById(R.id.searchView);
        noSearchResultsText = findViewById(R.id.noSearchResultsText);
        tvPersonal1=findViewById(R.id.tvPersonal1);
        tvPersonal2=findViewById(R.id.tvPersonal2);

        CheckBox cbNetflix = findViewById(R.id.filterNetflix);
        CheckBox cbPrime = findViewById(R.id.filterPrime);
        CheckBox cbDisney = findViewById(R.id.filterDisney);

// restore state
        cbNetflix.setChecked(filterNetflix);
        cbPrime.setChecked(filterPrime);
        cbDisney.setChecked(filterDisney);

        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS, MODE_PRIVATE).edit();

        cbNetflix.setOnCheckedChangeListener((b, checked) -> {
            filterNetflix = checked;
            editor.putBoolean(KEY_NETFLIX, checked).apply();
        });

        cbPrime.setOnCheckedChangeListener((b, checked) -> {
            filterPrime = checked;
            editor.putBoolean(KEY_PRIME, checked).apply();
        });

        cbDisney.setOnCheckedChangeListener((b, checked) -> {
            filterDisney = checked;
            editor.putBoolean(KEY_DISNEY, checked).apply();
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewPersonal1 = findViewById(R.id.recyclerViewPersonal1);
        recyclerViewPersonal2 = findViewById(R.id.recyclerViewPersonal2);

        recyclerViewPersonal1.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewPersonal2.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        personalAdapter1 = new MovieAdapter(personalMovies1, true);
        personalAdapter2 = new MovieAdapter(personalMovies2, true);

        recyclerViewPersonal1.setAdapter(personalAdapter1);
        recyclerViewPersonal2.setAdapter(personalAdapter2);

        recyclerViewRecommended2.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );recyclerViewRecommended2.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

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

        recyclerViewDirectorBar = findViewById(R.id.recyclerViewDirectorBar);


        tvDirectorBar = findViewById(R.id.tvDirectorBar);

        recyclerViewDirectorBar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        directorAdapter = new MovieAdapter(directorMovies, true);
        recyclerViewDirectorBar.setAdapter(directorAdapter);

        tvActorBar = findViewById(R.id.tvActorBar);
        recyclerViewActorBar = findViewById(R.id.recyclerViewActorBar);
        recyclerViewActorBar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        actorAdapter = new MovieAdapter(actorMovies, true);
        recyclerViewActorBar.setAdapter(actorAdapter);

        recommendedAdapter2 = new MovieAdapter(recommendedMovies2, true);
        recyclerViewRecommended2.setAdapter(recommendedAdapter2);

        // Data
        fetchNowPlayingMovies();
        fetchUpcomingMovies();
        loadRecommendations();
        loadPersonalisedBars();
        setupSearch();
        setupBottomNavigation(bottomNavigationView);
        loadDirectorBar();
        loadActorBar();
        loadSimilarUserRecommendations();
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


//main engine users also liek
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
    //flexible bars
    private void loadPersonalisedBars() {

        RecommendationEngine engine = new RecommendationEngine();

        engine.getPersonalisedDiscoverRequests(requests -> {


            tvPersonal1.setVisibility(View.GONE);
            tvPersonal2.setVisibility(View.GONE);
            recyclerViewPersonal1.setVisibility(View.GONE);
            recyclerViewPersonal2.setVisibility(View.GONE);

            if (requests == null || requests.isEmpty()) return;

            // BAR 1
            RecommendationEngine.DiscoverRequest r1 = requests.get(0);
            tvPersonal1.setText(r1.heading);
            tvPersonal1.setVisibility(View.VISIBLE);
            recyclerViewPersonal1.setVisibility(View.VISIBLE);
            discoverIntoBar(r1, personalMovies1, personalAdapter1, recyclerViewPersonal1);

            // BAR 2
            if (requests.size() > 1) {
                RecommendationEngine.DiscoverRequest r2 = requests.get(1);
                tvPersonal2.setText(r2.heading);
                tvPersonal2.setVisibility(View.VISIBLE);
                recyclerViewPersonal2.setVisibility(View.VISIBLE);
                discoverIntoBar(r2, personalMovies2, personalAdapter2, recyclerViewPersonal2);
            }
        });
    }

    private void discoverIntoBar(
            RecommendationEngine.DiscoverRequest req,
            List<Movie> target,
            MovieAdapter adapter,
            RecyclerView rv
    ) {
        String genreId = TmdbMapper.genreToId(req.genre);
        String providerId = TmdbMapper.serviceToProvider(req.service);

        if (genreId == null || providerId == null) {
            rv.setVisibility(View.GONE);
            return;
        }

        rv.setVisibility(View.VISIBLE);
        target.clear();
        adapter.notifyDataSetChanged();
//dynamic call here
        RetrofitClient.getInstance()
                .discoverMovies(
                        API_KEY,
                        genreId,
                        providerId,
                        "flatrate",
                        "IE",
                        "popularity.desc"

                )
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            target.addAll(response.body().getResults());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("DISCOVER", "Failed", t);
                    }
                });
    }



    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty()) return false;

                isSearching = true;

                homeContentContainer.setVisibility(View.GONE);
                recyclerViewSearchResults.setVisibility(View.VISIBLE);

                searchMovies(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                if (isSearching && (newText == null || newText.trim().isEmpty())) {
                    isSearching = false;

                    recyclerViewSearchResults.setVisibility(View.GONE);
                    homeContentContainer.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });
    }



    private void searchMovies(String query) {
        RetrofitClient.getInstance()
                .searchMovies(API_KEY, query, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call,
                                           Response<MovieResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        List<Movie> results = response.body().getResults();

                        if (!filterNetflix && !filterPrime && !filterDisney) {
                            recyclerViewSearchResults.setAdapter(
                                    new MovieAdapter(results, false)
                            );
                            return;
                        }

                        List<Movie> filtered = new ArrayList<>();
                        noSearchResultsText.setVisibility(View.GONE);
                        recyclerViewSearchResults.setVisibility(View.VISIBLE);

                        recyclerViewSearchResults.postDelayed(() -> {
                            if (filtered.isEmpty() && (filterNetflix || filterPrime || filterDisney)) {
                                recyclerViewSearchResults.setVisibility(View.GONE);
                                noSearchResultsText.setText("No movies found on selected service");
                                noSearchResultsText.setVisibility(View.VISIBLE);
                            }
                        }, 100);

                        for (Movie movie : results) {
                            checkProviders(movie, filtered);
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API", "Search failed", t);
                    }
                });
    }
    private void checkProviders(Movie movie, List<Movie> filtered) {
        RetrofitClient.getInstance()
                .getWatchProviders((movie.getId()), API_KEY)
                .enqueue(new Callback<WatchProviderResponse>() {
                    @Override
                    public void onResponse(
                            Call<WatchProviderResponse> call,
                            Response<WatchProviderResponse> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getResults() == null
                                || response.body().getResults().get("IE") == null
                                || response.body().getResults().get("IE").getFlatrate() == null)
                            return;

                        List<WatchProviderResponse.Provider> providers =
                                response.body().getResults().get("IE").getFlatrate();

                        for (WatchProviderResponse.Provider p : providers) {
                            if ((filterNetflix && p.getProviderName().equals("Netflix")) ||
                                    (filterPrime && p.getProviderName().toLowerCase().contains("amazon prime video")) ||
                                    (filterDisney && p.getProviderName().toLowerCase().contains("disney"))) {

                                filtered.add(movie);

                                runOnUiThread(() -> {
                                    noSearchResultsText.setVisibility(View.GONE);
                                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                                    recyclerViewSearchResults.setAdapter(
                                            new MovieAdapter(filtered, false)
                                    );
                                });
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<WatchProviderResponse> call, Throwable t) {}
                });
    }

    private void loadDirectorBar() {

        RecommendationEngine engine = new RecommendationEngine();

        engine.getTopDirectors(directors -> {

            if (directors == null || directors.isEmpty()) return;

            tvDirectorBar.setText("More from your favourite directors");
            tvDirectorBar.setVisibility(View.VISIBLE);
            recyclerViewDirectorBar.setVisibility(View.VISIBLE);

            directorMovies.clear();

            for (String director : directors) {
                fetchMoviesByDirector(director);
            }
        });
    }
    private void fetchMoviesByDirector(String directorName) {

        RetrofitClient.getInstance()
                .searchPerson(API_KEY, directorName)
                .enqueue(new Callback<PersonApiResponse.PersonSearchResponse>() {

                    @Override
                    public void onResponse(
                            Call<PersonApiResponse.PersonSearchResponse> call,
                            Response<PersonApiResponse.PersonSearchResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getResults().isEmpty()) {
                            return;
                        }

                        int personId =
                                response.body().getResults().get(0).getId();

                        fetchDirectorCredits(personId);
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonSearchResponse> call,
                            Throwable t) {

                        Log.e("DIRECTOR_BAR", "Person search failed", t);
                    }
                });
    }
    private void fetchDirectorCredits(int personId) {

        RetrofitClient.getInstance()
                .getPersonCredits(personId, API_KEY)
                .enqueue(new Callback<PersonApiResponse.PersonCreditsResponse>() {

                    @Override
                    public void onResponse(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Response<PersonApiResponse.PersonCreditsResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        for (PersonApiResponse.PersonCreditsResponse.Crew crew
                                : response.body().getCrew()) {

                            if ("Director".equals(crew.getJob())
                                    && crew.getPosterPath() != null) {

                                Movie movie = new Movie();
                                movie.setId(crew.getId());
                                movie.setTitle(crew.getTitle());
                                movie.setPosterPath(crew.getPosterPath());

                                directorMovies.add(movie);
                            }
                        }

                        directorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Throwable t) {

                        Log.e("DIRECTOR_BAR", "fail", t);
                    }
                });
    }

    private void loadActorBar() {

        RecommendationEngine engine = new RecommendationEngine();

        engine.getTopActor(actors -> {

            if (actors == null || actors.isEmpty()) return;

            tvActorBar.setText("More from your favourite Actors");
            tvActorBar.setVisibility(View.VISIBLE);
            recyclerViewActorBar.setVisibility(View.VISIBLE);

            actorMovies.clear();

            for (String actor: actors) {
                fetchMoviesByActor(actor);
            }
        });
    }
    private void fetchMoviesByActor(String actorName) {

        RetrofitClient.getInstance()
                .searchPerson(API_KEY, actorName)
                .enqueue(new Callback<PersonApiResponse.PersonSearchResponse>() {

                    @Override
                    public void onResponse(
                            Call<PersonApiResponse.PersonSearchResponse> call,
                            Response<PersonApiResponse.PersonSearchResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getResults().isEmpty()) {
                            return;
                        }

                        int personId =
                                response.body().getResults().get(0).getId();

                        fetchActorCredits(personId);
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonSearchResponse> call,
                            Throwable t) {

                        Log.e("Actor_BAR", "Person search failed", t);
                    }
                });
    }
    private void fetchActorCredits(int personId) {

        RetrofitClient.getInstance()
                .getPersonCredits(personId, API_KEY)
                .enqueue(new Callback<PersonApiResponse.PersonCreditsResponse>() {

                    @Override
                    public void onResponse(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Response<PersonApiResponse.PersonCreditsResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        for (PersonApiResponse.PersonCreditsResponse.Cast cast
                                : response.body().getCast()) {

                            if (cast.getPosterPath() != null) {

                                Movie movie = new Movie();

                                movie.setId(cast.getId());
                                movie.setTitle(cast.getTitle());
                                movie.setPosterPath(cast.getPosterPath());
                                actorMovies.add(movie);
                            }
                        }

                        actorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Throwable t) {

                        Log.e("Actor_BAR", "fail", t);
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
            } else if (id == R.id.moviem) {
                startActivity(new Intent(this, MovieMarathonActivity.class));
                return true;
            }
            else if (id == R.id.nav_music) {
                startActivity(new Intent(this, MusicToMovieActivity.class));
                return true;
            }


            return false;
        });
    }
    private void loadSimilarUserRecommendations() {

        RecommendationEngine engine = new RecommendationEngine();

        engine.getSimilarUserRecommendations(movieTitles -> {

            Log.d("SIMILAR_RECO", "Titles from engine: " + movieTitles);

            if (movieTitles == null || movieTitles.isEmpty()) {
                recyclerViewRecommended2.setVisibility(View.GONE);
                tvRecommended2.setVisibility(View.GONE);
                return;
            }

            tvRecommended2.setText("Users like you also like");
            tvRecommended2.setVisibility(View.VISIBLE);
            recyclerViewRecommended2.setVisibility(View.VISIBLE);

            recommendedMovies2.clear();
            recommendedAdapter2.notifyDataSetChanged();

            for (String title : movieTitles) {
                RetrofitClient.getInstance()
                        .searchMovies(API_KEY, title, "en-US", 1)
                        .enqueue(new Callback<MovieResponse>() {
                            @Override
                            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                                if (response.isSuccessful()
                                        && response.body() != null
                                        && !response.body().getResults().isEmpty()) {

                                    recommendedMovies2.add(response.body().getResults().get(0));
                                    recommendedAdapter2.notifyItemInserted(
                                            recommendedMovies2.size() - 1
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Call<MovieResponse> call, Throwable t) {
                                Log.e("SIMILAR_RECO", "TMDB lookup failed for " + title, t);
                            }
                        });
            }
        });
    }
}
