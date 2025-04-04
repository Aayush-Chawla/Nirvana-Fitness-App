package com.example.nirvana.data.models;

public class Exercise {
    private String name;
    private int imageResource;

    public Exercise(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }
}
