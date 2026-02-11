package com.example.movie;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private TMDBApi tmdbApi;


    private RetrofitClient() {
        Retrofit tmdbRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tmdbApi = tmdbRetrofit.create(TMDBApi.class);


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
public Call<MovieResponse >discoverMovies (String apiKey,String genreId,String providerId,String monetization,String region,String sortBy){
        return tmdbApi.discoverMovies(apiKey,genreId,providerId,monetization,region,sortBy);
}

}
