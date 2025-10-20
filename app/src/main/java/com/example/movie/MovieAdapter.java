package com.example.movie;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final List<Movie> movieList;
    private final boolean isHorizontal;

    public MovieAdapter(List<Movie> movieList, boolean isHorizontal) {
        this.movieList = movieList;
        this.isHorizontal = isHorizontal;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView poster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            poster = itemView.findViewById(R.id.moviePoster);
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (isHorizontal) {
            params.width = (int) (parent.getWidth() * 0.42);
            params.setMargins(16, 8, 16, 8);
        } else {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.setMargins(16, 16, 16, 16);
        }
        view.setLayoutParams(params);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.title.setText(movie.getTitle() != null ? movie.getTitle() : "No Title");

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            Glide.with(holder.poster.getContext())
                    .load(posterUrl)
                    .into(holder.poster);
        }

        // Click poster -> MovieDetailActivity
        holder.poster.setOnClickListener(v -> {
            if (movie.getId() > 0 && movie.getTitle() != null) {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra("MOVIE_ID", movie.getId());
                intent.putExtra("MOVIE_TITLE", movie.getTitle());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
