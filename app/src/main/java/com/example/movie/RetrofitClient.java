package com.example.movie;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // Now Playing Movies
    public Call<MovieResponse> getNowPlaying(String apiKey, String language, int page) {
        return api.getNowPlaying(apiKey, language, page);
    }

    // Upcoming Movies
    public Call<MovieResponse> getUpcoming(String apiKey, String language, int page) {
        return api.getUpcoming(apiKey, language, page);
    }

    // Movie Details
    public Call<Movie> getMovieDetails(int movieId, String apiKey, String language) {
        return api.getMovieDetails(movieId, apiKey, language);
    }

    // Watch Providers (Streaming Info)
    public Call<WatchProviderResponse> getWatchProviders(int movieId, String apiKey) {
        return api.getWatchProviders(movieId, apiKey);
    }

    // Interface for TMDB API
    public interface TMDBApi {

        @GET("movie/now_playing")
        Call<MovieResponse> getNowPlaying(
                @Query("api_key") String apiKey,
                @Query("language") String language,
                @Query("page") int page
        );

        @GET("movie/upcoming")
        Call<MovieResponse> getUpcoming(
                @Query("api_key") String apiKey,
                @Query("language") String language,
                @Query("page") int page
        );

        @GET("movie/{movie_id}")
        Call<Movie> getMovieDetails(
                @Path("movie_id") int movieId,
                @Query("api_key") String apiKey,
                @Query("language") String language
        );

        @GET("movie/{movie_id}/watch/providers")
        Call<WatchProviderResponse> getWatchProviders(
                @Path("movie_id") int movieId,
                @Query("api_key") String apiKey
        );
    }
}
