package com.example.nirvana.data.models;

public class WorkoutCategory {
    private String name;
    private int iconResId;

    public WorkoutCategory(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}
