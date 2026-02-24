package com.example.movie;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Movie {

    private int id;
    private String title;

    // TMDB provides genres as a list of objects in detailed responses
    private List<Genre> genres;

    @SerializedName("poster_path")
    private String posterPath;

    private String overview;
    private Integer runtime; // nullable
    @SerializedName("vote_average")
    private Float voteAverage; // nullable

    // Inner class for Genre
    public static class Genre {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setId(int id) {
        this.id = id;
    }
}
