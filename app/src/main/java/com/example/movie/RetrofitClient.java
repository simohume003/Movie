package com.example.movie;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private TMDBApi tmdbApi;
    private CexApi cexApi;

    private RetrofitClient() {
        Retrofit tmdbRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tmdbApi = tmdbRetrofit.create(TMDBApi.class);

        Retrofit cexRetrofit = new Retrofit.Builder()
                .baseUrl("https://uk.webuy.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        cexApi = cexRetrofit.create(CexApi.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // TMDB Calls
    public Call<MovieResponse> getNowPlaying(String apiKey, String language, int page) {
        return tmdbApi.getNowPlaying(apiKey, language, page);
    }

    public Call<MovieResponse> getUpcoming(String apiKey, String language, int page) {
        return tmdbApi.getUpcoming(apiKey, language, page);
    }

    public Call<Movie> getMovieDetails(int movieId, String apiKey, String language) {
        return tmdbApi.getMovieDetails(movieId, apiKey, language);
    }

    public Call<WatchProviderResponse> getWatchProviders(int movieId, String apiKey) {
        return tmdbApi.getWatchProviders(movieId, apiKey);
    }

    public Call<MovieResponse> searchMovies(String apiKey, String query, String language, int page) {
        return tmdbApi.searchMovies(apiKey, query, language, page);
    }

    // CeX Call
    public Call<CexResponse> searchCexProduct(String query) {
        return cexApi.searchProduct(query);
    }
}
