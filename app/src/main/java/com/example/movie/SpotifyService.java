package com.example.movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SpotifyService {

    @GET("v1/search")
    Call<SpotifySearchResponse> searchAlbums(
            @Header("Authorization") String authHeader,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );

    @GET("v1/search")
    Call<SpotifyTrackSearchResponse> searchTracks(
            @Header("Authorization") String authHeader,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );

}
