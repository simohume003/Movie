package com.example.movie;

import java.util.List;

public class SpotifyTrack {

    private String name;
    private List<Artist> artists;
    private Album album;

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artists != null && !artists.isEmpty()
                ? artists.get(0).getName()
                : "Unknown Artist";
    }

    public String getImageUrl() {
        return album != null
                && album.getImages() != null
                && !album.getImages().isEmpty()
                ? album.getImages().get(0).getUrl()
                : null;
    }


    public static class Artist {
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class Album {
        private List<SpotifyImage> images;

        public List<SpotifyImage> getImages() {
            return images;
        }
    }
}