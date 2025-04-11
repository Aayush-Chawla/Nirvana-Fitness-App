package com.example.nirvana.ml;

import java.util.HashMap;
import java.util.Map;

public class FoodNutritionData {
    private static final Map<String, NutritionInfo> FOOD_NUTRITION_DATABASE = new HashMap<>();

    static {
        // Initialize with Food101 dataset items and their nutritional values
        // These values are per 100g serving
        addFood("apple_pie", 265, 3.5, 41, 10);
        addFood("baby_back_ribs", 277, 23, 0, 20);
        addFood("baklava", 428, 6, 54, 20);
        addFood("beef_carpaccio", 120, 22, 0, 4);
        addFood("beef_tartare", 150, 25, 0, 5);
        addFood("beet_salad", 43, 1.6, 9, 0.2);
        addFood("beignets", 421, 5, 50, 23);
        addFood("bibimbap", 450, 15, 65, 15);
        addFood("bread_pudding", 320, 8, 45, 12);
        addFood("breakfast_burrito", 350, 15, 45, 12);
        addFood("bruschetta", 180, 5, 25, 7);
        addFood("caesar_salad", 180, 8, 10, 12);
        addFood("cannoli", 320, 5, 45, 12);
        addFood("caprese_salad", 150, 7, 8, 9);
        addFood("carrot_cake", 340, 4, 45, 16);
        addFood("ceviche", 120, 20, 5, 3);
        addFood("cheesecake", 321, 5, 25, 22);
        addFood("cheese_plate", 350, 20, 2, 28);
        addFood("chicken_curry", 250, 20, 15, 12);
        addFood("chicken_quesadilla", 330, 20, 25, 18);
        addFood("chicken_wings", 290, 27, 0, 19);
        addFood("chocolate_cake", 371, 4, 45, 20);
        addFood("chocolate_mousse", 225, 4, 25, 12);
        addFood("churros", 380, 5, 50, 18);
        addFood("clam_chowder", 180, 10, 15, 8);
        addFood("club_sandwich", 450, 25, 45, 20);
        addFood("crab_cakes", 250, 15, 15, 15);
        addFood("creme_brulee", 280, 4, 25, 18);
        addFood("croissant", 406, 8, 45, 23);
        addFood("cup_cakes", 305, 3, 45, 12);
        addFood("curry_waffle", 320, 8, 45, 12);
        addFood("donuts", 452, 5, 50, 25);
        addFood("dumplings", 250, 8, 35, 8);
        addFood("edamame", 121, 11, 9, 5);
        addFood("eggs_benedict", 550, 25, 45, 30);
        addFood("escargots", 90, 16, 2, 1);
        addFood("falafel", 333, 13, 31, 18);
        addFood("filet_mignon", 271, 26, 0, 17);
        addFood("fish_and_chips", 450, 15, 45, 25);
        addFood("foie_gras", 462, 11, 4, 44);
        addFood("french_fries", 312, 4, 41, 15);
        addFood("french_onion_soup", 250, 12, 25, 12);
        addFood("french_toast", 229, 7, 25, 11);
        addFood("garlic_bread", 350, 8, 45, 15);
        addFood("gnocchi", 150, 5, 30, 1);
        addFood("greek_salad", 150, 5, 10, 10);
        addFood("grilled_cheese_sandwich", 380, 15, 35, 20);
        addFood("grilled_salmon", 208, 22, 0, 13);
        addFood("guacamole", 160, 2, 8, 15);
        addFood("gyoza", 250, 8, 35, 8);
        addFood("hamburger", 295, 17, 30, 14);
        addFood("hot_and_sour_soup", 90, 5, 10, 3);
        addFood("hot_dog", 290, 12, 25, 18);
        addFood("huevos_rancheros", 350, 15, 35, 18);
        addFood("hummus", 166, 8, 14, 10);
        addFood("ice_cream", 207, 4, 23, 11);
        addFood("lasagna", 135, 8, 15, 5);
        addFood("lobster_bisque", 180, 8, 15, 10);
        addFood("lobster_roll_sandwich", 350, 15, 35, 15);
        addFood("macaroni_and_cheese", 371, 13, 35, 20);
        addFood("macarons", 371, 4, 65, 12);
        addFood("miso_soup", 34, 2, 5, 1);
        addFood("mussels", 86, 12, 3, 2);
        addFood("nachos", 346, 13, 35, 18);
        addFood("omelette", 154, 11, 1, 12);
        addFood("onion_rings", 411, 4, 45, 25);
        addFood("oysters", 69, 9, 3, 2);
        addFood("pad_thai", 357, 14, 57, 8);
        addFood("paella", 150, 8, 20, 4);
        addFood("pancakes", 227, 5, 35, 7);
        addFood("panna_cotta", 267, 3, 25, 17);
        addFood("peking_duck", 337, 19, 0, 28);
        addFood("pho", 150, 8, 20, 4);
        addFood("pizza", 266, 11, 33, 10);
        addFood("pork_chop", 231, 25, 0, 14);
        addFood("poutine", 550, 15, 55, 30);
        addFood("prime_rib", 273, 25, 0, 18);
        addFood("pulled_pork_sandwich", 350, 15, 35, 15);
        addFood("ramen", 436, 14, 65, 12);
        addFood("ravioli", 203, 7, 35, 4);
        addFood("red_velvet_cake", 367, 4, 45, 20);
        addFood("risotto", 320, 8, 55, 8);
        addFood("samosa", 262, 5, 35, 10);
        addFood("sashimi", 60, 12, 0, 1);
        addFood("scallops", 88, 17, 2, 1);
        addFood("seaweed_salad", 45, 1, 8, 1);
        addFood("shrimp_and_grits", 350, 15, 35, 15);
        addFood("spaghetti_bolognese", 158, 7, 25, 3);
        addFood("spaghetti_carbonara", 450, 15, 45, 20);
        addFood("spring_rolls", 110, 3, 18, 3);
        addFood("steak", 271, 26, 0, 17);
        addFood("strawberry_shortcake", 318, 4, 45, 12);
        addFood("sushi", 150, 6, 25, 2);
        addFood("tacos", 226, 9, 25, 10);
        addFood("takoyaki", 150, 5, 20, 5);
        addFood("tiramisu", 280, 4, 35, 12);
        addFood("tuna_tartare", 120, 20, 5, 3);
        addFood("waffles", 291, 7, 35, 14);
    }

    private static void addFood(String name, double calories, double protein, double carbs, double fat) {
        FOOD_NUTRITION_DATABASE.put(name.toLowerCase(), new NutritionInfo(calories, protein, carbs, fat));
    }

    public static NutritionInfo getNutritionInfo(String foodName) {
        return FOOD_NUTRITION_DATABASE.get(foodName.toLowerCase());
    }

    public static class NutritionInfo {
        private final double calories;
        private final double protein;
        private final double carbs;
        private final double fat;

        public NutritionInfo(double calories, double protein, double carbs, double fat) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }

        public double getCalories() {
            return calories;
        }

        public double getProtein() {
            return protein;
        }

        public double getCarbs() {
            return carbs;
        }

        public double getFat() {
            return fat;
        }
    }
} 