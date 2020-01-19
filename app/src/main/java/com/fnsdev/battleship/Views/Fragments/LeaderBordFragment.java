package com.fnsdev.battleship.Views.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fnsdev.battleship.Models.Profile;
import com.fnsdev.battleship.R;
import com.fnsdev.battleship.Views.Adapters.LeaderBoardAdapter;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderBordFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leader_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        List<Profile> profiles = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LeaderBoardAdapter adapter = new LeaderBoardAdapter(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles")
                .orderBy("winsCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener((Task<QuerySnapshot> task) -> {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            Profile profile = new Profile(
                                document.getString("email"),
                                document.getLong("winsCount").intValue(),
                                document.getLong("lossesCount").intValue()
                            );
                            System.out.println(profile.email);
                            profiles.add(profile);
                        }
                        adapter.setProfiles(profiles);
                    }
                    else {
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
