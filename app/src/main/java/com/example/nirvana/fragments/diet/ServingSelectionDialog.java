package com.example.nirvana.fragments.diet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.nirvana.R;
import com.example.nirvana.api.ApiClient;
import com.example.nirvana.api.FatSecretApi;
import com.example.nirvana.api.FoodGetResponse;
import com.example.nirvana.data.models.FoodItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ServingSelectionDialog extends DialogFragment {

    private static final String ARG_FOOD_ID = "foodId";
    private static final String ARG_FOOD_NAME = "foodName";

    private String foodId;
    private String foodName;
    private MealFragment parentFragment;
    private FatSecretApi fatSecretApi;
    private List<FoodGetResponse.Serving> servings = new ArrayList<>(); // Fixed here
    private FoodGetResponse.Serving selectedServing;

    private Spinner spinnerServings;
    private TextView tvNutritionInfo;
    private Button btnAdd;

    public ServingSelectionDialog() {}

    public static ServingSelectionDialog newInstance(String foodId, String foodName, MealFragment fragment) {
        ServingSelectionDialog dialog = new ServingSelectionDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FOOD_ID, foodId);
        args.putString(ARG_FOOD_NAME, foodName);
        dialog.setArguments(args);
        dialog.parentFragment = fragment;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            foodId = getArguments().getString(ARG_FOOD_ID);
            foodName = getArguments().getString(ARG_FOOD_NAME);
        }
        fatSecretApi = ApiClient.getFatSecretApi(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_serving_selection, container, false);

        spinnerServings = view.findViewById(R.id.spinnerServings);
        tvNutritionInfo = view.findViewById(R.id.tvNutritionInfo);
        btnAdd = view.findViewById(R.id.btnAdd);

        // Set food title
        TextView tvFoodTitle = view.findViewById(R.id.tvFoodTitle);
        tvFoodTitle.setText(foodName);

        loadServings();

        spinnerServings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServing = servings.get(position);
                updateNutritionInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAdd.setOnClickListener(v -> {
            if (selectedServing != null) {
                addFoodItem();
                dismiss();
            }
        });

        return view;
    }

    private void loadServings() {
        fatSecretApi.getFood(
                "food.get",  // method parameter
                foodId,      // food_id
                "json"       // format
    ).enqueue(new Callback<FoodGetResponse>() {
            @Override
            public void onResponse(Call<FoodGetResponse> call, Response<FoodGetResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().food != null &&
                        response.body().food.servings != null &&
                        response.body().food.servings.servingList != null) {

                    servings.clear();
                    servings.addAll(response.body().food.servings.servingList);

                    List<String> servingDescriptions = new ArrayList<>();
                    for (FoodGetResponse.Serving serving : servings) {
                        servingDescriptions.add(serving.description);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            servingDescriptions
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerServings.setAdapter(adapter);

                    if (!servings.isEmpty()) {
                        selectedServing = servings.get(0);
                        updateNutritionInfo();
                    }
                }
            }

            @Override
            public void onFailure(Call<FoodGetResponse> call, Throwable t) {
                // Handle error
            }
        });
    }

    private void updateNutritionInfo() {
        if (selectedServing != null) {
            String info = String.format(
                    "Calories: %s\nProtein: %sg\nCarbs: %sg\nFat: %sg",
                    selectedServing.calories,
                    selectedServing.protein,
                    selectedServing.carbs,
                    selectedServing.fat
            );
            tvNutritionInfo.setText(info);
        }
    }

    private void addFoodItem() {
        FoodItem foodItem = new FoodItem(
                foodId,
                foodName,
                selectedServing.servingId,
                selectedServing.description,
                Double.parseDouble(selectedServing.calories),
                Double.parseDouble(selectedServing.protein),
                Double.parseDouble(selectedServing.carbs),
                Double.parseDouble(selectedServing.fat),
                parentFragment.getMealType()
        );

        parentFragment.addFoodItem(foodItem);
    }
}