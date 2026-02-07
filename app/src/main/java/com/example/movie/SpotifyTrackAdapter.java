package com.example.movie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SpotifyTrackAdapter
        extends RecyclerView.Adapter<SpotifyTrackAdapter.TrackViewHolder> {

    public interface OnTrackClick {
        void onClick(SpotifyTrack track);
    }

    private final List<SpotifyTrack> tracks;
    private final OnTrackClick listener;

    public SpotifyTrackAdapter(List<SpotifyTrack> tracks, OnTrackClick listener) {
        this.tracks = tracks;
        this.listener = listener;
    }


    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView image;

        TrackViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.trackTitle);
            artist = itemView.findViewById(R.id.trackArtist);
            image = itemView.findViewById(R.id.trackImage);
        }
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        SpotifyTrack track = tracks.get(position);

        holder.title.setText(track.getName());
        holder.artist.setText(track.getArtistName());

        String imageUrl = track.getImageUrl();
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(track));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
