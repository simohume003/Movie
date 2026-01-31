package com.example.movie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<LogActivity.MovieLog> logList;
    private static final String API_KEY = "929a54210220df3134196d46a16375b1";
    public LogAdapter(List<LogActivity.MovieLog> logList) {
        this.logList = logList;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar ratingBar;


        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.logPoster);
            title = itemView.findViewById(R.id.logTitle);
            ratingBar = itemView.findViewById(R.id.logRatingBar);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_watched_grid, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogActivity.MovieLog log = logList.get(position);

        holder.title.setText(log.title);
        holder.ratingBar.setRating(log.rating);


        holder.poster.setImageDrawable(null);

        // Fetch poster from TMDB
        RetrofitClient.getInstance()
                .searchMovies(API_KEY, log.title, "en-US", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().getResults().isEmpty()) {

                            String posterPath = response.body()
                                    .getResults()
                                    .get(0)
                                    .getPosterPath();

                            Glide.with(holder.itemView.getContext())
                                    .load("https://image.tmdb.org/t/p/w500" + posterPath)
                                    .centerCrop()
                                    .into(holder.poster);
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }
}
