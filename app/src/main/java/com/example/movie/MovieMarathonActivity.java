package com.example.movie;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileWriter;
import androidx.core.content.FileProvider;
import java.util.HashMap;
import java.util.Map;

public class MovieMarathonActivity extends AppCompatActivity {

    private static final String API_KEY = "929a54210220df3134196d46a16375b1";

    private EditText editDirectorName;
    private Button btnSearchDirector;
    private Button btnDownloadCsv;
    private RecyclerView recyclerViewMarathonMovies;

    private final List<Movie> marathonMovies = new ArrayList<>();
    private MovieAdapter marathonAdapter;
    private final Map<Integer, String> movieYears = new HashMap<>();
    private String currentDirectorName = "movie_marathon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marathon_activity);

        editDirectorName = findViewById(R.id.editDirectorName);
        btnSearchDirector = findViewById(R.id.btnSearchDirector);
        btnDownloadCsv = findViewById(R.id.btnDownloadCsv);
        recyclerViewMarathonMovies = findViewById(R.id.recyclerViewMarathonMovies);

        recyclerViewMarathonMovies.setLayoutManager(new GridLayoutManager(this, 2));
        marathonAdapter = new MovieAdapter(marathonMovies, false);
        recyclerViewMarathonMovies.setAdapter(marathonAdapter);

        btnDownloadCsv.setOnClickListener(v -> {
            if (marathonMovies.isEmpty()) {
                return;
            }
            exportMoviesToCsv();
        });
        btnSearchDirector.setOnClickListener(v -> {
            String directorName = editDirectorName.getText().toString().trim();

            if (!directorName.isEmpty()) {
                currentDirectorName = directorName;
                marathonMovies.clear();
                movieYears.clear();
                marathonAdapter.notifyDataSetChanged();
                fetchMoviesByDirector(directorName);
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
                            Log.e("MARATHON", "No director found");
                            return;
                        }

                        int personId = response.body().getResults().get(0).getId();
                        fetchDirectorCredits(personId);
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonSearchResponse> call,
                            Throwable t) {
                        Log.e("MARATHON", "Person search failed", t);
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

                        if (!response.isSuccessful() || response.body() == null) {
                            return;
                        }

                        marathonMovies.clear();

                        for (PersonApiResponse.PersonCreditsResponse.Crew crew : response.body().getCrew()) {
                            if ("Director".equals(crew.getJob()) && crew.getPosterPath() != null) {
                                Movie movie = new Movie();
                                movie.setId(crew.getId());
                                movie.setTitle(crew.getTitle());
                                movie.setPosterPath(crew.getPosterPath());

                                marathonMovies.add(movie);
                                String releaseDate = crew.getReleaseDate();
                                String year = "N/A";
                                if (releaseDate != null && releaseDate.length() >= 4) {
                                    year = releaseDate.substring(0, 4);
                                }
                                movieYears.put(movie.getId(), year);
                            }
                        }

                        marathonAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Throwable t) {
                        Log.e("MARATHON", "Credits fetch failed", t);
                    }
                });
    }
}