package com.example.nirvana.fragments.auth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;

/**
 * Adapter class for the ViewPager2 to handle the different pages of profile setup
 */
public class ProfilePagerAdapter extends RecyclerView.Adapter<ProfilePagerAdapter.PageViewHolder> {

    private final FragmentActivity context;
    private final int pageCount;

    public ProfilePagerAdapter(FragmentActivity context, int pageCount) {
        this.context = context;
        this.pageCount = pageCount;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0: // Basic Information
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_basic_information, parent, false);
                break;
            case 1: // Body Metrics
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_body_metrics, parent, false);
                break;
            case 2: // Fitness Goals
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_fitness_goals, parent, false);
                break;
            case 3: // Dietary Preferences
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_dietary_preferences, parent, false);
                break;
            case 4: // Health Information
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_health_information, parent, false);
                break;
            case 5: // Schedule Preferences
                view = LayoutInflater.from(context).inflate(
                        R.layout.page_schedule_preferences, parent, false);
                break;
            default:
                throw new IllegalStateException("Unexpected page type: " + viewType);
        }
        
        // Set tag to identify the page
        view.setTag("page_" + viewType);
        
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        // No binding needed as views are static
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    @Override
    public int getItemViewType(int position) {
        // Using position as the view type
        return position;
    }
    
    /**
     * Creates a new page view for the given position.
     * This is used when we need to inflate a view for validation before displaying it.
     * 
     * @param inflater LayoutInflater to use
     * @param position Position of the page to create
     * @return The inflated view
     */
    public View createPageView(LayoutInflater inflater, int position) {
        View view;
        switch (position) {
            case 0: // Basic Information
                view = inflater.inflate(R.layout.page_basic_information, null, false);
                break;
            case 1: // Body Metrics
                view = inflater.inflate(R.layout.page_body_metrics, null, false);
                break;
            case 2: // Fitness Goals
                view = inflater.inflate(R.layout.page_fitness_goals, null, false);
                break;
            case 3: // Dietary Preferences
                view = inflater.inflate(R.layout.page_dietary_preferences, null, false);
                break;
            case 4: // Health Information
                view = inflater.inflate(R.layout.page_health_information, null, false);
                break;
            case 5: // Schedule Preferences
                view = inflater.inflate(R.layout.page_schedule_preferences, null, false);
                break;
            default:
                throw new IllegalStateException("Unexpected page position: " + position);
        }
        
        view.setTag("page_" + position);
        return view;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 