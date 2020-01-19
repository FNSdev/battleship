package com.fnsdev.battleship.Views.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.fnsdev.battleship.R;
import com.fnsdev.battleship.ViewModels.GameViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainScreenFragment extends Fragment {
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Button logOutButton;
    private Button newGameButton;
    private Button connectToGameButton;
    private Button leaderBoardButton;
    private EditText gameIdEditText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameIdEditText = view.findViewById(R.id.gameIdEditText);

        logOutButton = view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener((View v) -> {
            auth.signOut();

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = new SignUpFragment();
            transaction.replace(R.id.frameLayout, fragment, "SignUpFragment");
            transaction.commit();
        });

        newGameButton = view.findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener((View v) ->
            newGame()
        );


        connectToGameButton = view.findViewById(R.id.connectToGameButton);
        connectToGameButton.setOnClickListener((View v) ->
            connectToGame()
        );

        leaderBoardButton = view.findViewById(R.id.leaderBoardButton);
        leaderBoardButton.setOnClickListener((View v) -> {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment fragment = new LeaderBordFragment();
                transaction.addToBackStack(null);
                transaction.replace(R.id.frameLayout, fragment, "LeaderBoardFragment");
                transaction.commit();
            }
        );
    }

    private Map<String, Object> getUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if(user == null) {
            return null;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getUid());

        return userData;
    }

    private void toBattleScreen(String gameId, boolean isHost) {
        GameViewModel gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        gameViewModel.gameId = gameId;
        gameViewModel.isHost = isHost;

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = new BattleScreenFragment();
        transaction.addToBackStack(null);
        transaction.replace(R.id.frameLayout, fragment, "BattleScreenFragment");
        transaction.commit();
    }

    private void newGame() {
        Map<String, Object> userData = getUserData();
        if(userData == null) {
            return;
        }

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("started", false);

        //UUID gameId = UUID.randomUUID();
        String gameId = "1";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("games").document(gameId.toString())
                .set(gameData);
        db.collection("games").document(gameId.toString())
                .collection("users")
                .document("host")
                .set(userData);

        toBattleScreen(gameId.toString(), true);
    }

    private void connectToGame() {
        FirebaseUser user = auth.getCurrentUser();
        if(user == null) {
            return;
        }

        String gameId = gameIdEditText.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.get().addOnCompleteListener((Task<DocumentSnapshot> task) -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if(!doc.exists() || doc.getBoolean("started")) {
                    Toast.makeText(getActivity(), "Game does not exist or has been started already", Toast.LENGTH_SHORT).show();
                }
                else {
                    Map<String, Object> userData = getUserData();
                    if(userData == null) {
                        return;
                    }

                    Map<String, Object> gameData = new HashMap<>();
                    gameData.put("started", true);

                    db.collection("games").document(gameId)
                            .update(gameData);

                    db.collection("games").document(gameId)
                            .collection("users")
                            .document("client")
                            .set(userData);

                    toBattleScreen(gameId, false);
                }
            }
            else {
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
