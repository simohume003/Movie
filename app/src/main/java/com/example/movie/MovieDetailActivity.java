package com.example.movie;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, overview, runtime, rating, streaming, cexPrice;
    private static final String TMDB_API_KEY = "929a54210220df3134196d46a16375b1";

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
        cexPrice = findViewById(R.id.detailCexPrice);

        int movieId = getIntent().getIntExtra("MOVIE_ID", 0);
        String movieTitle = getIntent().getStringExtra("MOVIE_TITLE");

        if (movieId > 0 && movieTitle != null) {
            fetchMovieDetails(movieId);
            fetchWatchProviders(movieId);
            fetchCexPrice(movieTitle);
        } else {
            title.setText("No Movie Selected");
        }
    }

    private void fetchMovieDetails(int movieId) {
        RetrofitClient.getInstance()
                .getMovieDetails(movieId, TMDB_API_KEY, "en-US")
                .enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Movie movie = response.body();

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
    }

    private void fetchWatchProviders(int movieId) {
        RetrofitClient.getInstance()
                .getWatchProviders(movieId, TMDB_API_KEY)
                .enqueue(new Callback<WatchProviderResponse>() {
                    @Override
                    public void onResponse(Call<WatchProviderResponse> call, Response<WatchProviderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WatchProviderResponse wpResponse = response.body();
                            WatchProviderResponse.CountryProviders providers = null;

                            // 1. Ireland
                            providers = wpResponse.getResults().get("IE");

                            // 2. US fallback
                            if (providers == null || providers.getFlatrate() == null) {
                                providers = wpResponse.getResults().get("US");
                            }

                            // 3. Any other region
                            if (providers == null || providers.getFlatrate() == null) {
                                for (String region : wpResponse.getResults().keySet()) {
                                    WatchProviderResponse.CountryProviders temp = wpResponse.getResults().get(region);
                                    if (temp != null && temp.getFlatrate() != null) {
                                        providers = temp;
                                        break;
                                    }
                                }
                            }

                            if (providers != null && providers.getFlatrate() != null) {
                                StringBuilder sb = new StringBuilder();
                                for (WatchProviderResponse.Provider p : providers.getFlatrate()) {
                                    sb.append(p.getProviderName()).append(", ");
                                }
                                String providerText = sb.toString();
                                if (providerText.endsWith(", ")) {
                                    providerText = providerText.substring(0, providerText.length() - 2);
                                }
                                streaming.setText("Available on: " + providerText);
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

    private void fetchCexPrice(String movieTitle) {
        new Thread(() -> {
            try {
                // Replace spaces with '+'
                String searchQuery = movieTitle.replace(" ", "+") + "+DVD";
                Document doc = Jsoup.connect("https://uk.webuy.com/search?search=" + searchQuery)
                        .get();

                Element priceElement = null;

                // Loop over all products to find exact title match
                for (Element product : doc.select(".product-title")) {
                    String text = product.text().toLowerCase();
                    if (text.contains(movieTitle.toLowerCase())) {
                        priceElement = product.parent().selectFirst(".product-price");
                        break;
                    }
                }

                String priceText = priceElement != null ? priceElement.text() : "Nothing found";

                runOnUiThread(() -> cexPrice.setText("CEX Price: " + priceText));
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> cexPrice.setText("CEX Price: Nothing found"));
            }
        }).start();
    }
}
