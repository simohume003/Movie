package com.example.movie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<LogActivity.MovieLog> logList;

    public LogAdapter(List<LogActivity.MovieLog> logList) {
        this.logList = logList;
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView title, note;

        public LogViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.logTitleText);
            note = itemView.findViewById(R.id.logNoteText);
        }
    }

    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        LogActivity.MovieLog log = logList.get(position);
        holder.title.setText(log.title);

    }

    @Override
    public int getItemCount() {
        return logList.size();
    }
}
