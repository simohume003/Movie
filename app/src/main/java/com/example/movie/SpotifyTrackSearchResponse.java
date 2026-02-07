package com.example.movie;

import java.util.List;

public class SpotifyTrackSearchResponse {

    private Tracks tracks;

    public Tracks getTracks() {
        return tracks;
    }

    public static class Tracks {
        private List<SpotifyTrack> items;

        public List<SpotifyTrack> getItems() {
            return items;
        }
    }
}
