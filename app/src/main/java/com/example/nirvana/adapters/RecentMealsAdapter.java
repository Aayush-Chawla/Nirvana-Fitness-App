package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecentMealsAdapter extends RecyclerView.Adapter<RecentMealsAdapter.MealViewHolder> {
    
    private List<Map<String, Object>> meals = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    
    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_meal, parent, false);
        return new MealViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Map<String, Object> meal = meals.get(position);
        
        // Get meal details
        String name = (String) meal.get("name");
        String servingSize = (String) meal.get("servingSize");
        Object caloriesObj = meal.get("calories");
        int calories = 0;
        if (caloriesObj instanceof Number) {
            calories = ((Number) caloriesObj).intValue();
        }
        String mealType = (String) meal.get("mealType");
        
        // Handle timestamp which could be either Long or Timestamp
        Date timestampDate = null;
        Object timestampObj = meal.get("timestamp");
        if (timestampObj instanceof Long) {
            timestampDate = new Date((Long) timestampObj);
        } else if (timestampObj instanceof Timestamp) {
            timestampDate = ((Timestamp) timestampObj).toDate();
        }
        
        // Format time
        String time = "";
        if (timestampDate != null) {
            time = timeFormat.format(timestampDate);
        }
        
        // Set text
        holder.tvMealName.setText(name != null ? name : "Unknown Food");
        holder.tvMealDetails.setText(String.format("%s • %d calories", 
                servingSize != null ? servingSize : "1 serving", 
                calories));
        holder.tvMealTime.setText(time);
        holder.tvMealType.setText(mealType != null ? mealType : "Meal");
    }
    
    @Override
    public int getItemCount() {
        return meals.size();
    }
    
    public void setMeals(List<Map<String, Object>> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }
    
    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName;
        TextView tvMealDetails;
        TextView tvMealTime;
        TextView tvMealType;
        
        MealViewHolder(View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDetails = itemView.findViewById(R.id.tvMealDetails);
            tvMealTime = itemView.findViewById(R.id.tvMealTime);
            tvMealType = itemView.findViewById(R.id.tvMealType);
        }
    }
} 