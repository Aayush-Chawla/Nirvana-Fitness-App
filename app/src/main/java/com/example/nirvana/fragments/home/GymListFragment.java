package com.example.nirvana.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.data.models.GymModel;
import com.example.nirvana.ui.adapters.GymAdapter;

import java.util.ArrayList;
import java.util.List;

public class GymListFragment extends Fragment {
    private RecyclerView gymListRecyclerView;
    private List<GymModel> gymList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gym_list, container, false);

        gymListRecyclerView = view.findViewById(R.id.gymListRecyclerView);
        gymListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample Gym Data
        gymList = new ArrayList<>();
        gymList.add(new GymModel("Fit Gym", "4.5 ⭐", "Downtown"));
        gymList.add(new GymModel("Power Gym", "4.8 ⭐", "City Center"));
        gymList.add(new GymModel("Elite Fitness", "4.7 ⭐", "Westside"));
        gymList.add(new GymModel("StrongBody Gym", "4.2 ⭐", "Eastside"));

        // Set Adapter
        GymAdapter gymAdapter = new GymAdapter(gymList);
        gymListRecyclerView.setAdapter(gymAdapter);

        return view;
    }
}
