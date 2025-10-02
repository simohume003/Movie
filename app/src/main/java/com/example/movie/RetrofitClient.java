package com.example.movie;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private TMDBApi api;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(TMDBApi.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public Call<MovieResponse> getNowPlaying(String apiKey, String language, int page) {
        return api.getNowPlaying(apiKey, language, page);
    }

    public Call<MovieResponse> getUpcoming(String apiKey, String language, int page) {
        return api.getUpcoming(apiKey, language, page);
    }

    public Call<Movie> getMovieDetails(int movieId, String apiKey, String language) {
        return api.getMovieDetails(movieId, apiKey, language);
    }

    public Call<WatchProviderResponse> getWatchProviders(int movieId, String apiKey) {
        return api.getWatchProviders(movieId, apiKey);
    }

    public Call<MovieResponse> searchMovies(String apiKey, String query, String language, int page) {
        return api.searchMovies(apiKey, query, language, page);
    }
}
