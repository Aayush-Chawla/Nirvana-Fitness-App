package com.example.nirvana.utils;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class BindingAdapters {
    
    /**
     * Binding adapter for RecyclerView that helps data binding work with RecyclerView
     */
    @BindingAdapter("recyclerAdapter")
    public static void setRecyclerViewAdapter(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }
} 