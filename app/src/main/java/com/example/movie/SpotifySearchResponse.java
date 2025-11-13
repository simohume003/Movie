package com.example.movie;

import java.util.List;

public class SpotifySearchResponse {

    private Albums albums;

    public Albums getAlbums() {
        return albums;
    }

    public static class Albums {
        private java.util.List<SpotifyAlbum> items;

        public List<SpotifyAlbum> getItems() {
            return items;
        }
    }
}