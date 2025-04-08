package com.example.nirvana.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {
    private List<String> recommendations;

    public RecommendationAdapter() {
        this.recommendations = new ArrayList<>();
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        String recommendation = recommendations.get(position);
        holder.tvRecommendationText.setText(recommendation);
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecommendationIcon;
        TextView tvRecommendationText;

        RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecommendationIcon = itemView.findViewById(R.id.ivRecommendationIcon);
            tvRecommendationText = itemView.findViewById(R.id.tvRecommendationText);
        }
    }
} 