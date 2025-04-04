package com.example.nirvana.fragments.diet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.FoodSearchAdapter;
import com.example.nirvana.api.ApiClient;
import com.example.nirvana.api.FatSecretApi;
import com.example.nirvana.api.FoodSearchResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoodSearchDialog extends DialogFragment {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private FatSecretApi fatSecretApi;
    private List<FoodSearchResponse.FoodItem> searchResults = new ArrayList<>();
    private FoodSearchAdapter adapter;
    private MealFragment parentFragment;

    public FoodSearchDialog() {}

    public static FoodSearchDialog newInstance(MealFragment fragment) {
        FoodSearchDialog dialog = new FoodSearchDialog();
        dialog.parentFragment = fragment;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_food_search, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        progressBar = view.findViewById(R.id.progressBar);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodSearchAdapter(searchResults, foodItem -> {
            showServingSelectionDialog(foodItem);
        });
        rvSearchResults.setAdapter(adapter);

        fatSecretApi = ApiClient.getFatSecretApi(requireContext());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    searchFoods(s.toString());
                } else if (s.length() == 0) {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchFoods(String query) {
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        if (query.length() < 2) {
            Toast.makeText(getContext(), "Please enter at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);

        fatSecretApi.searchFoods("foods.search", query, 0, 20, "json")
            .enqueue(new Callback<FoodSearchResponse>() {
                @Override
                public void onResponse(Call<FoodSearchResponse> call, Response<FoodSearchResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        searchResults.clear();
                        if (response.body().foods != null && !response.body().foods.isEmpty()) {
                            searchResults.addAll(response.body().foods);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No food items found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMessage = "Failed to search food items";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("FoodSearch", "Error response: " + errorBody);
                                // Check if it's an XML error response
                                if (errorBody.contains("<error>") && errorBody.contains("<message>")) {
                                    int start = errorBody.indexOf("<message>") + 9;
                                    int end = errorBody.indexOf("</message>");
                                    if (start > 8 && end > start) {
                                        errorMessage = errorBody.substring(start, end);
                                    }
                                }
                            } catch (IOException e) {
                                Log.e("FoodSearch", "Error parsing response", e);
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FoodSearchResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);
                    String errorMessage = "Network error: " + t.getMessage();
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("FoodSearch", "Search failed", t);
                }
            });
    }

    private void showServingSelectionDialog(FoodSearchResponse.FoodItem foodItem) {
        ServingSelectionDialog dialog = ServingSelectionDialog.newInstance(
                foodItem.food_id,
                foodItem.food_name,
                parentFragment
        );
        dialog.show(getChildFragmentManager(), "ServingSelectionDialog");
    }
}