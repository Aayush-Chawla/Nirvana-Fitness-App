package com.example.nirvana.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Exercise> results;

    public static class Exercise {
        @SerializedName("id")
        private String id;  // Changed to String to match Firebase data type

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("category")
        private String category;  // Changed to String to match Firebase data type

        @SerializedName("muscles")
        private List<Integer> muscles;

        @SerializedName("muscles_secondary")
        private List<Integer> musclesSecondary;

        @SerializedName("equipment")
        private List<Integer> equipment;

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public List<Integer> getMuscles() { return muscles; }
        public List<Integer> getMusclesSecondary() { return musclesSecondary; }
        public List<Integer> getEquipment() { return equipment; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setCategory(String category) { this.category = category; }
        public void setMuscles(List<Integer> muscles) { this.muscles = muscles; }
        public void setMusclesSecondary(List<Integer> musclesSecondary) { this.musclesSecondary = musclesSecondary; }
        public void setEquipment(List<Integer> equipment) { this.equipment = equipment; }
    }

    // Getters
    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<Exercise> getResults() { return results; }
}

class ExerciseInfoResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("results")
    private List<ExerciseInfo> results;

    public static class ExerciseInfo {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("category")
        private ExerciseCategory category;

        @SerializedName("description")
        private String description;

        @SerializedName("muscles")
        private List<Muscle> muscles;

        @SerializedName("muscles_secondary")
        private List<Muscle> musclesSecondary;

        @SerializedName("equipment")
        private List<Equipment> equipment;

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public ExerciseCategory getCategory() { return category; }
        public String getDescription() { return description; }
        public List<Muscle> getMuscles() { return muscles; }
        public List<Muscle> getMusclesSecondary() { return musclesSecondary; }
        public List<Equipment> getEquipment() { return equipment; }
    }

    public static class ExerciseCategory {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
    }

    public static class Muscle {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("is_front")
        private boolean isFront;

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public boolean isFront() { return isFront; }
    }

    public static class Equipment {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
    }

    // Getters
    public int getCount() { return count; }
    public List<ExerciseInfo> getResults() { return results; }
} 