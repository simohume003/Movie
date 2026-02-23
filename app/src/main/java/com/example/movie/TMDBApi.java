package com.example.movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );
    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreId,
            @Query("with_watch_providers") String providerId,
            @Query("with_watch_monetization_types") String monetization,
            @Query("watch_region") String region,
            @Query("sort_by") String sort
    );
    @GET("search/person")
    Call<PersonApiResponse.PersonSearchResponse> searchPerson(
            @Query("api_key") String apiKey,
            @Query("query") String name
    );

    @GET("person/{person_id}/movie_credits")
    Call<PersonApiResponse.PersonCreditsResponse> getPersonCredits(
            @Path("person_id") int personId,
            @Query("api_key") String apiKey
    );

}
