package com.example.movie;

import com.google.gson.annotations.SerializedName;

public class Movie {

    private int id;
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    private String overview;
    private Integer runtime;           // nullable
    @SerializedName("vote_average")
    private Float voteAverage;         // nullable

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getOverview() { return overview; }
    public Integer getRuntime() { return runtime; }
    public Float getVoteAverage() { return voteAverage; }
}
