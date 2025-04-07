package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.WeekDayViewHolder> {
    private List<String> weekDays;
    private OnDaySelectedListener listener;
    private int selectedPosition = 0;

    public interface OnDaySelectedListener {
        void onDaySelected(String day);
    }

    public WeekDayAdapter(List<String> weekDays, OnDaySelectedListener listener) {
        this.weekDays = weekDays;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        String day = weekDays.get(position);
        holder.txtDay.setText(day);
        
        holder.cardDay.setCardBackgroundColor(holder.itemView.getContext().getColor(
            position == selectedPosition ? R.color.colorPrimary : R.color.card_background
        ));
        
        holder.txtDay.setTextColor(holder.itemView.getContext().getColor(
            position == selectedPosition ? R.color.white : R.color.text_primary
        ));

        holder.cardDay.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onDaySelected(day);
        });
    }

    @Override
    public int getItemCount() {
        return weekDays.size();
    }

    static class WeekDayViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardDay;
        TextView txtDay;

        WeekDayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDay = itemView.findViewById(R.id.cardDay);
            txtDay = itemView.findViewById(R.id.txtDay);
        }
    }
} 