package com.example.movie;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private FirebaseFirestore db;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
        db = FirebaseFirestore.getInstance();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView poster;
        Button btnMarkWatched;

        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            poster = itemView.findViewById(R.id.moviePoster);
            btnMarkWatched = itemView.findViewById(R.id.btnMarkWatched);
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.title.setText(movie.getTitle() != null ? movie.getTitle() : "No Title");

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            Glide.with(holder.poster.getContext())
                    .load(posterUrl)
                    .into(holder.poster);
        }

        // Click entire item -> open detail page
        holder.itemView.setOnClickListener(v -> {
            if (movie.getId() > 0 && movie.getTitle() != null) {
                Intent intent = new Intent(holder.itemView.getContext(), MovieDetailActivity.class);
                intent.putExtra("MOVIE_ID", movie.getId());
                intent.putExtra("MOVIE_TITLE", movie.getTitle());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // Button click: mark as watched
        holder.btnMarkWatched.setOnClickListener(v -> {
            if (movie.getTitle() != null) {
                Map<String, Object> watchedMovie = new HashMap<>();
                watchedMovie.put("title", movie.getTitle());
                watchedMovie.put("note", "Marked as watched");
                watchedMovie.put("timestamp", System.currentTimeMillis());

                db.collection("watchedMovies")
                        .add(watchedMovie)
                        .addOnSuccessListener(docRef ->
                                Toast.makeText(holder.itemView.getContext(),
                                        movie.getTitle() + " marked as watched!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(holder.itemView.getContext(),
                                        "Failed to mark as watched", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
