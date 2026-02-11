package com.example.movie;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicToMovieActivity extends AppCompatActivity {

    private EditText songSearchInput;
    private RecyclerView songResultsRecycler;

    private SpotifyTrackAdapter adapter;
    private LinearLayout movieResultCard;
    private TextView movieTitleText;
    private TextView movieReasonText;

    private ImageView moviePoster;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";


    private final List<SpotifyTrack> tracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_to_movie);

        movieResultCard = findViewById(R.id.movieResultCard);
        movieTitleText = findViewById(R.id.movieTitleText);
        movieReasonText = findViewById(R.id.movieReasonText);
        moviePoster = findViewById(R.id.moviePoster);


        songSearchInput = findViewById(R.id.songSearchInput);
        songResultsRecycler = findViewById(R.id.songResultsRecycler);

        songResultsRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SpotifyTrackAdapter(tracks, track -> {

            String song = track.getName() + " - " + track.getArtistName();

            GeminiService.getMovieFromSong(song, new GeminiService.GeminiCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {


                        songResultsRecycler.setVisibility(View.GONE);


                        movieResultCard.setVisibility(View.VISIBLE);



                        String movieTitle = "Movie Recommendation";
                        String reason = result;
                        if (result.contains("Movie:")) {
                            String[] parts = result.split("Reason:");
                            movieTitle = parts[0].replace("Movie:", "").trim();
                            if (parts.length > 1) {
                                reason = parts[1].trim();
                            }
                        }

                        movieTitleText.setText(movieTitle);
                        movieReasonText.setText(reason);

                        fetchMoviePoster(movieTitle);
                    });
                }


                @Override
                public void onError(Exception e) {
                    runOnUiThread(() ->
                            Log.e("GEMINI_RESULT", "Failed", e)
                    );
                }
            });
        });


        songResultsRecycler.setAdapter(adapter);

        setupSearchListener();
    }

    private void setupSearchListener() {
        songSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchSpotify(s.toString().trim());
                }
            }
        });
    }

    private void searchSpotify(String query) {
        SpotifyAuth.fetchAccessToken(new SpotifyAuth.TokenCallback() {
            @Override
            public void onTokenReceived(String token) {

                SpotifyClient.getApi()
                        .searchTracks(
                                "Bearer " + token,
                                query,
                                "track",
                                10
                        )
                        .enqueue(new Callback<SpotifyTrackSearchResponse>() {
                            @Override
                            public void onResponse(
                                    Call<SpotifyTrackSearchResponse> call,
                                    Response<SpotifyTrackSearchResponse> response
                            ) {
                                if (response.isSuccessful()
                                        && response.body() != null
                                        && response.body().getTracks() != null
                                        && response.body().getTracks().getItems() != null) {

                                    tracks.clear();
                                    tracks.addAll(
                                            response.body()
                                                    .getTracks()
                                                    .getItems()
                                    );
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<SpotifyTrackSearchResponse> call, Throwable t) {
                                Log.e("SPOTIFY", "Track search failed", t);
                            }
                        });
            }

            @Override
            public void onError(Throwable t) {
                Log.e("SPOTIFY", "Token fetch failed", t);
            }
        });
    }
    private void fetchMoviePoster(String title) {
        RetrofitClient.getInstance()
                .searchMovies(API_KEY,title, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(
                            Call<MovieResponse> call,
                            Response<MovieResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().getResults().isEmpty()) {

                            String posterPath = response.body()
                                    .getResults()
                                    .get(0)
                                    .getPosterPath();

                            Glide.with(MusicToMovieActivity.this)
                                    .load("https://image.tmdb.org/t/p/w500" + posterPath)
                                    .centerCrop()
                                    .into(moviePoster);
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("TMDB", "Poster fetch failed", t);
                    }
                });
    }

}
