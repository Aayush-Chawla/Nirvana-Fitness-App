package com.example.nirvana.fragments.diet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.FoodSearchAdapter;
import com.example.nirvana.api.FoodSearchResponse;
import com.example.nirvana.models.FoodItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FoodSearchDialog extends DialogFragment {
    private EditText editSearch;
    private RecyclerView recyclerResults;
    private FoodSearchAdapter adapter;
    private OnFoodSelectedListener listener;

    public static FoodSearchDialog newInstance(Fragment fragment) {
        FoodSearchDialog dialog = new FoodSearchDialog();
        if (fragment instanceof OnFoodSelectedListener) {
            dialog.setOnFoodSelectedListener((OnFoodSelectedListener) fragment);
        }
        return dialog;
    }

    public interface OnFoodSelectedListener {
        void onFoodSelected(FoodItem foodItem, String servingSize);
    }

    public void setOnFoodSelectedListener(OnFoodSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_food_search, null);

        editSearch = view.findViewById(R.id.editSearch);
        recyclerResults = view.findViewById(R.id.recyclerResults);
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodSearchAdapter(new ArrayList<>(), this::showServingSelectionDialog);
        recyclerResults.setAdapter(adapter);

        builder.setView(view)
               .setTitle("Search Food")
               .setPositiveButton("Close", null);

        // Setup search functionality with TextWatcher for real-time search
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load initial data
        performSearch("");

        return builder.create();
    }

    private void performSearch(String query) {
        List<FoodSearchResponse.FoodItem> apiResults = getDummySearchResults();
        // Filter results based on search query
        if (!query.isEmpty()) {
            apiResults = apiResults.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
        List<FoodItem> modelResults = convertApiResultsToModelItems(apiResults);
        adapter.updateFoodItems(modelResults);
    }

    private List<FoodItem> convertApiResultsToModelItems(List<FoodSearchResponse.FoodItem> apiResults) {
        return apiResults.stream()
                .map(apiItem -> new FoodItem(
                    apiItem.getId(),
                    apiItem.getName(),
                    apiItem.getCalories(),
                    apiItem.getServingSize(),
                    apiItem.getProtein(),
                    apiItem.getCarbs(),
                    apiItem.getFat()
                ))
                .collect(Collectors.toList());
    }

    private void showServingSelectionDialog(FoodItem foodItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_serving_size, null);
        
        TextView tvServingSize = view.findViewById(R.id.tvServingSize);
        SeekBar seekBarServing = view.findViewById(R.id.seekBarServing);
        
        seekBarServing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvServingSize.setText(progress + "g");
                }

                @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view)
               .setTitle("Select Serving Size")
               .setPositiveButton("Confirm", (dialog, which) -> {
                   String servingSize = seekBarServing.getProgress() + "g";
                   if (listener != null) {
                       listener.onFoodSelected(foodItem, servingSize);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private List<FoodSearchResponse.FoodItem> getDummySearchResults() {
        List<FoodSearchResponse.FoodItem> results = new ArrayList<>();
        
        // Add dummy food items
        FoodSearchResponse.FoodItem item1 = new FoodSearchResponse.FoodItem();
        item1.setId("1");
        item1.setName("Chicken Breast");
        item1.setCalories(165);
        item1.setServingSize("100g");
        item1.setProtein(31);
        item1.setCarbs(0);
        item1.setFat(3.6);
        results.add(item1);

        FoodSearchResponse.FoodItem item2 = new FoodSearchResponse.FoodItem();
        item2.setId("2");
        item2.setName("Brown Rice");
        item2.setCalories(111);
        item2.setServingSize("100g");
        item2.setProtein(2.6);
        item2.setCarbs(23);
        item2.setFat(0.9);
        results.add(item2);

        FoodSearchResponse.FoodItem item3 = new FoodSearchResponse.FoodItem();
        item3.setId("3");
        item3.setName("Broccoli");
        item3.setCalories(55);
        item3.setServingSize("100g");
        item3.setProtein(3.7);
        item3.setCarbs(11.2);
        item3.setFat(0.6);
        results.add(item3);

        FoodSearchResponse.FoodItem item4 = new FoodSearchResponse.FoodItem();
        item4.setId("4");
        item4.setName("Salmon");
        item4.setCalories(208);
        item4.setServingSize("100g");
        item4.setProtein(22);
        item4.setCarbs(0);
        item4.setFat(13);
        results.add(item4);

        FoodSearchResponse.FoodItem item5 = new FoodSearchResponse.FoodItem();
        item5.setId("5");
        item5.setName("Sweet Potato");
        item5.setCalories(86);
        item5.setServingSize("100g");
        item5.setProtein(1.6);
        item5.setCarbs(20);
        item5.setFat(0.1);
        results.add(item5);

        return results;
    }
}