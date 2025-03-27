package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.Exercise;
import com.example.nirvana.data.models.ExerciseResponse;
import com.example.nirvana.network.ApiClient;
import com.example.nirvana.network.ExerciseApiService;
import com.example.nirvana.ui.adapters.ExerciseAdapter;
import java.util.List;
import retrofit2.Call;
import com.google.gson.Gson;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeWorkoutFragment extends Fragment {

    private RecyclerView recyclerExerciseList;
    private ExerciseAdapter exerciseAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerExerciseList = view.findViewById(R.id.recyclerExerciseList);
        recyclerExerciseList.setLayoutManager(new LinearLayoutManager(requireContext()));

        fetchExercises(); // Fetch exercises from API
    }

    private void fetchExercises() {
        Retrofit retrofit = ApiClient.getClient();
        ExerciseApiService apiService = retrofit.create(ExerciseApiService.class);

        Call<ExerciseResponse> call = apiService.getExercises();
        call.enqueue(new Callback<ExerciseResponse>() {
            @Override
//            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Exercise> exerciseList = response.body().getResults();
//                    exerciseAdapter = new ExerciseAdapter(exerciseList);
//                    recyclerExerciseList.setAdapter(exerciseAdapter);
//                } else {
//                    Toast.makeText(requireContext(), "Failed to load exercises", Toast.LENGTH_SHORT).show();
//                }
//            }
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exerciseList = response.body().getResults();

                    // Log the response data
                    Log.d("API_RESPONSE", "Exercises: " + new Gson().toJson(exerciseList));

                    if (exerciseList != null && !exerciseList.isEmpty()) {
                        exerciseAdapter = new ExerciseAdapter(exerciseList);
                        recyclerExerciseList.setAdapter(exerciseAdapter);
                    } else {
                        Toast.makeText(requireContext(), "No exercises found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load exercises", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error fetching exercises: " + t.getMessage());
                Toast.makeText(requireContext(), "Error fetching exercises", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
