package com.example.movie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.LinearLayout;
import android.net.Uri;
import android.content.Intent;





public class MovieDetailActivity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, overview, runtime, rating, streaming, cexPrice;
    private int currentRuntime = 0;
    private String currentGenre = "Unknown";
    private String currentDirector = "Unknown";
    //ebay
    private LinearLayout ebayContainer;

    private TextView ebayPriceText;


    // Spotify UI
    private ImageView spotifyAlbumArt;
    private TextView spotifyAlbumTitle;
    private Button btnOpenSpotify;

    private Button btnMarkWatched;

    private static final String TMDB_API_KEY = "929a54210220df3134196d46a16375b1";
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    //ebay key
    private static final String EBAY_APP_ID = "SIMONHUM-CineScop-PRD-788b448e1-08289e48";

    //logos tmdb
    private LinearLayout streamingLogoContainer;

    // store Spotify album URL so we can open it
    private String spotifyAlbumUrl = null;

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
        btnMarkWatched = findViewById(R.id.btnMarkWatched);
        //ebayui
        ebayContainer = findViewById(R.id.ebayContainer);
        //logos
        streamingLogoContainer = findViewById(R.id.streamingLogoContainer);
        ebayContainer.setVisibility(View.GONE);

        // Spotify UI
        spotifyAlbumArt = findViewById(R.id.spotifyAlbumArt);
        spotifyAlbumTitle = findViewById(R.id.spotifyAlbumTitle);
        btnOpenSpotify = findViewById(R.id.btnOpenSpotify);


        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        int movieId = getIntent().getIntExtra("MOVIE_ID", 0);
        String movieTitle = getIntent().getStringExtra("MOVIE_TITLE");

        if (movieId > 0 && movieTitle != null) {
            fetchMovieDetails(movieId);
            fetchWatchProviders(movieId);
            fetchEbayPrice(movieTitle);


            btnMarkWatched.setOnClickListener(v ->
                    markMovieAsWatched(movieTitle, currentRuntime, currentGenre,currentDirector));



            searchSpotifySoundtrack(movieTitle);
        } else {
            title.setText("No Movie Selected");
        }

        // open in Spotify button
        btnOpenSpotify.setOnClickListener(v -> {
            if (spotifyAlbumUrl != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(spotifyAlbumUrl));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No Spotify album found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovieDetails(int movieId) {
        RetrofitClient.getInstance()
                .getMovieDetails(movieId, TMDB_API_KEY, "en-US")
                .enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Movie movie = response.body();

                            // Save locally for later
                            currentRuntime = movie.getRuntime();
                            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                                currentGenre = movie.getGenres().get(0).getName();
                            }

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
        RetrofitClient.getInstance()
                .getMovieCredits(movieId, TMDB_API_KEY)
                .enqueue(new Callback<PersonApiResponse.PersonCreditsResponse>() {
                    @Override
                    public void onResponse(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Response<PersonApiResponse.PersonCreditsResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        for (PersonApiResponse.PersonCreditsResponse.Crew crew :
                                response.body().getCrew()) {

                            if ("Director".equals(crew.getJob())) {
                                currentDirector = crew.getName();
                                break;
                            }
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<PersonApiResponse.PersonCreditsResponse> call,
                            Throwable t) {
                    }
                });
    }

    //ebay
    private void fetchEbayPrice(String movieTitle) {
        runOnUiThread(() -> {
            ebayContainer.setVisibility(View.VISIBLE);
            ebayContainer.setOnClickListener(v -> {
                String url = "https://www.ebay.com/sch/i.html?_nkw="
                        + Uri.encode(movieTitle + " DVD")+"&_sop=15";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            });
        });
    }

    private void fetchWatchProviders(int movieId) {
        RetrofitClient.getInstance()
                .getWatchProviders(movieId, TMDB_API_KEY)
                .enqueue(new Callback<WatchProviderResponse>() {
                    @Override
                    public void onResponse(Call<WatchProviderResponse> call,
                                           Response<WatchProviderResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        WatchProviderResponse.CountryProviders providers =
                                response.body().getResults().get("IE");
                        boolean netflixIE = false;
                        boolean netflixUS = false;

                        streamingLogoContainer.removeAllViews();
                        streamingLogoContainer.setVisibility(View.VISIBLE);

                        List<WatchProviderResponse.Provider> ieProviders =
                                providers != null ? providers.getFlatrate() : null;

                        if (ieProviders != null) {

                            for (WatchProviderResponse.Provider provider :ieProviders) {
                                if ("Netflix".equals(provider.getProviderName())) {
                                    netflixIE = true;
                                }
                                ImageView logo = new ImageView(MovieDetailActivity.this);

                                LinearLayout.LayoutParams params =
                                        new LinearLayout.LayoutParams(120, 120);
                                params.setMarginEnd(24);
                                logo.setLayoutParams(params);
                                logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                String logoUrl =
                                        "https://image.tmdb.org/t/p/w500" + provider.getLogoPath();

                                Glide.with(MovieDetailActivity.this)
                                        .load(logoUrl)
                                        .into(logo);

                                streamingLogoContainer.addView(logo);
                            }
                        }
                        WatchProviderResponse.CountryProviders usProviders =
                                response.body().getResults().get("US");

                            List<WatchProviderResponse.Provider> usList =
                                    usProviders != null ? usProviders.getFlatrate() : null;

                            if (usList != null) {

                                for (WatchProviderResponse.Provider provider : usList) {

                                if ("Netflix".equals(provider.getProviderName())) {
                                    netflixUS = true;
                                }
                            }
                        }
                        if (!netflixIE && netflixUS) {
                            ImageView usNetflixLogo = new ImageView(MovieDetailActivity.this);

                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(120, 120);
                            params.setMarginEnd(24);

                            usNetflixLogo.setLayoutParams(params);
                            usNetflixLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            usNetflixLogo.setImageResource(R.drawable.nam);

                            streamingLogoContainer.addView(usNetflixLogo);

                        }
                    }

                    @Override
                    public void onFailure(Call<WatchProviderResponse> call, Throwable t) {
                        // silent fail
                    }
                });
    }
            private void markMovieAsWatched(String movieTitle, int runtime, String genre,String Director) {

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to mark movies", Toast.LENGTH_SHORT).show();
            return;
        }

        // STEP 1 — pick streaming service
        String[] services = {"Netflix", "Prime Video", "Disney+", "Apple TV", "Other"};

        new AlertDialog.Builder(this)
                .setTitle("Where did you watch it?")
                .setItems(services, (dialog, which) -> {

                    String selectedService = services[which];

                    // STEP 2 — inflate rating dialog
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View ratingView = inflater.inflate(R.layout.dialog_rating, null);
                    RatingBar ratingBar = ratingView.findViewById(R.id.ratingBar);

                    new AlertDialog.Builder(this)
                            .setTitle("Your rating")
                            .setView(ratingView)
                            .setPositiveButton("Save", (d, w) -> {

                                int rating = (int) ratingBar.getRating();

                                // STEP 3 — build Firestore object
                                Map<String, Object> watchedMovie = new HashMap<>();
                                watchedMovie.put("title", movieTitle);
                                watchedMovie.put("genre", genre != null ? genre : "Unknown");
                                watchedMovie.put("runtime", runtime > 0 ? runtime : 0);
                                watchedMovie.put("service", selectedService);
                                watchedMovie.put("rating", rating);
                                watchedMovie.put("director", currentDirector != null ? currentDirector : "Unknown");
                                watchedMovie.put("timestamp", System.currentTimeMillis());

                                // STEP 4 — save to Firestore
                                db.collection("users")
                                        .document(currentUser.getUid())
                                        .collection("watchedMovies")
                                        .add(watchedMovie)
                                        .addOnSuccessListener(docRef ->
                                                Toast.makeText(this,
                                                        movieTitle + " saved (" + rating + "★)",
                                                        Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this,
                                                        "Failed to save movie",
                                                        Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }






    private void searchSpotifySoundtrack(String movieTitle) {
        // Nice heuristic: movie title + "original motion picture soundtrack"
        String query = movieTitle + " original motion picture soundtrack";

        SpotifyAuth.fetchAccessToken(new SpotifyAuth.TokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                SpotifyClient.getApi()
                        .searchAlbums("Bearer " + token, query, "album", 1)
                        .enqueue(new Callback<SpotifySearchResponse>() {
                            @Override
                            public void onResponse(Call<SpotifySearchResponse> call, Response<SpotifySearchResponse> response) {
                                if (response.isSuccessful() && response.body() != null &&
                                        response.body().getAlbums() != null &&
                                        response.body().getAlbums().getItems() != null &&
                                        !response.body().getAlbums().getItems().isEmpty()) {

                                    SpotifyAlbum album = response.body().getAlbums().getItems().get(0);
                                    spotifyAlbumTitle.setText(album.getName());

                                    // get first image if available
                                    if (album.getImages() != null && !album.getImages().isEmpty()) {
                                        String imgUrl = album.getImages().get(0).getUrl();
                                        Glide.with(MovieDetailActivity.this)
                                                .load(imgUrl)
                                                .into(spotifyAlbumArt);
                                    }

                                    if (album.getExternal_urls() != null &&
                                            album.getExternal_urls().getSpotify() != null) {
                                        spotifyAlbumUrl = album.getExternal_urls().getSpotify();
                                        btnOpenSpotify.setVisibility(Button.VISIBLE);
                                    } else {
                                        btnOpenSpotify.setVisibility(Button.GONE);
                                    }
                                } else {
                                    spotifyAlbumTitle.setText("No soundtrack found on Spotify");
                                    btnOpenSpotify.setVisibility(Button.GONE);
                                }
                            }

                            @Override
                            public void onFailure(Call<SpotifySearchResponse> call, Throwable t) {
                                spotifyAlbumTitle.setText("Spotify search failed");
                                btnOpenSpotify.setVisibility(Button.GONE);
                            }
                        });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        spotifyAlbumTitle.setText("Spotify auth failed")
                );
            }
        });
    }
}
