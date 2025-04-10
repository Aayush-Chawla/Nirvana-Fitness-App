package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private final List<String> suggestions;
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    public SuggestionAdapter(List<String> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvSuggestion.setText(suggestions.get(position));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(suggestions.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSuggestion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
        }
    }
} 