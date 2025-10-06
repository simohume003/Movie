package com.example.movie;

import com.google.firebase.Timestamp;

public class MovieLogItem {
    private String title;
    private Timestamp watchedAt;

    public MovieLogItem() {} // Needed for Firestore

    public MovieLogItem(String title, Timestamp watchedAt) {
        this.title = title;
        this.watchedAt = watchedAt;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Timestamp getWatchedAt() { return watchedAt; }
    public void setWatchedAt(Timestamp watchedAt) { this.watchedAt = watchedAt; }
}
