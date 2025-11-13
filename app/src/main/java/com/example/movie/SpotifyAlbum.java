package com.example.movie;

import java.util.List;

public class SpotifyAlbum {

    private String name;
    private List<SpotifyImage> images;
    private ExternalUrls external_urls;

    public String getName() {
        return name;
    }

    public List<SpotifyImage> getImages() {
        return images;
    }

    public ExternalUrls getExternal_urls() {
        return external_urls;
    }

    public static class ExternalUrls {
        private String spotify;

        public String getSpotify() {
            return spotify;
        }
    }
}
