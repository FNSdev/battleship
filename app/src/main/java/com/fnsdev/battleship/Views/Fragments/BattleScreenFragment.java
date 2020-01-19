package com.fnsdev.battleship.Views.Fragments;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fnsdev.battleship.R;
import com.fnsdev.battleship.ViewModels.GameViewModel;
import com.fnsdev.battleship.Logic.Game;
import com.fnsdev.battleship.Models.FieldButton;
import com.fnsdev.battleship.Views.Services.FieldValidator;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleScreenFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private List<FieldButton> yourField = new ArrayList<>();
    private List<FieldButton> opponentField = new ArrayList<>();
    private Button readyButton;

    private boolean isHost;
    private String gameId;
    private boolean hostIsReady = false;
    private boolean clientIsReady = false;

    private Game game;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battle_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GameViewModel gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        isHost = gameViewModel.isHost;
        gameId = gameViewModel.gameId;
        game = new Game(gameId);

        initFields(view);
        readyButton = view.findViewById(R.id.readyButton);
        readyButton.setOnClickListener((View v) ->
            ready()
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void ready() {
        boolean valid = FieldValidator.validateField(yourField);
        if(valid) {
            Toast.makeText(getActivity(), "Valid",
                    Toast.LENGTH_SHORT).show();
            for(FieldButton button : yourField) {
                button.button.setClickable(false);
            }
            if(isHost) {
                hostReady();
            }
            else {
                clientReady();
            }
            readyButton.setEnabled(false);
        }
        else {
            Toast.makeText(getActivity(), "Invalid",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void hostReady() {
        game.setYourField(yourField);

        db.collection("games").document(gameId).collection("turns")
            .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                if(e != null) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(DocumentChange dc : snapshots.getDocumentChanges()) {
                    if(dc.getType() != DocumentChange.Type.ADDED) {
                        continue;
                    }
                    processClientTurn(dc.getDocument());
                    String turnType = dc.getDocument().getString("turnType");
                }
            });

        hostIsReady = true;
        if(clientIsReady) {
            startGame();
        }
    }

    private void clientReady() {
        Map<String, Object> data = new HashMap<>();
        data.put("turnType", "ready");
        data.put("timestamp", System.currentTimeMillis() / 1000L);

        List<Integer> field = new ArrayList<>();
        for(FieldButton fb : yourField) {
            if(fb.isPressed) {
                field.add(1);
            }
            else {
                field.add(0);
            }
        }

        data.put("field", field);

        db.collection("games").document(gameId).collection("turns")
                .add(data);

        db.collection("games").document(gameId).collection("states")
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if(e != null) {
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for(DocumentChange dc : snapshots.getDocumentChanges()) {
                        if(dc.getType() != DocumentChange.Type.ADDED) {
                            continue;
                        }
                        processNewState(dc.getDocument());
                    }
                });

        clientIsReady = true;
        if(hostIsReady) {
            startGame();
        }
    }

    private void startGame() {
        setOpponentFieldEnabled(true);
        Toast.makeText(getActivity(), "Your turn!", Toast.LENGTH_LONG).show();
    }

    // Called on a host side
    private void processClientTurn(QueryDocumentSnapshot document) {
        String turnType = document.getString("turnType");
        switch (turnType) {
            case "ready":
                game.setOpponentField((List<Long>)document.get("field"));
                clientIsReady = true;
                if(hostIsReady) {
                    startGame();
                }
                break;
            case "shot":
                int i = document.getLong("i").intValue();
                int j = document.getLong("j").intValue();

                String result = game.processOpponentShot(i, j);
                processShotResult(i, j, result, yourField, true);
                break;
        }
    }

    // Called on a client side
    private void processNewState(QueryDocumentSnapshot document) {
        String stateType = document.getString("stateType");

        int i = document.getLong("i").intValue();
        int j = document.getLong("j").intValue();
        String user = document.getString("user");

        // This field means who made the shot
        if(user.equalsIgnoreCase("client")) {
            processShotResult(i, j, stateType, opponentField, false);
        }
        else {
            processShotResult(i, j, stateType, yourField, true);
        }

        if(!hostIsReady) {
            hostIsReady = true;
            if(clientIsReady) {
                startGame();
            }
        }
    }

    private void processShotResult(int i, int j, String result, List<FieldButton> field, boolean enableField) {
        boolean hit = !result.equalsIgnoreCase("miss");
        FieldButton fieldButton = field.get(i * 10 + j);
        updateUiOnHit(hit, fieldButton);

        switch (result) {
            case "miss":
                Toast.makeText(getActivity(), "Miss!", Toast.LENGTH_LONG).show();
                break;
            case "hit":
                Toast.makeText(getActivity(), "Hit!", Toast.LENGTH_LONG).show();
                break;
            case "kill":
                Toast.makeText(getActivity(), "Ship was destroyed!", Toast.LENGTH_LONG).show();
                break;
            case "win":
                processGameOver(true);
                return;
            case "loss":
                processGameOver(false);
                return;
        }


        if(enableField && result.equalsIgnoreCase("miss")) {
            setOpponentFieldEnabled(true);
        }
        else if(!enableField && !result.equalsIgnoreCase("miss")) {
            setOpponentFieldEnabled(true);
        }
    }

    private void pressYourFieldButton(FieldButton fieldButton) {
        if(!fieldButton.isPressed) {
            fieldButton.button.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
            fieldButton.isPressed = true;
        }
        else {
            fieldButton.button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.grey));
            fieldButton.isPressed = false;
        }
    }

    private void pressOpponentFieldButton(FieldButton fieldButton) {
        if(!fieldButton.isPressed) {
            setOpponentFieldEnabled(false);
            fieldButton.isPressed = true;
            int index = opponentField.indexOf(fieldButton);
            int i = index / 10;
            int j = index % 10;

            if(isHost) {
                String result = game.proccessYourShot(i, j);
                processShotResult(i, j, result, opponentField, false);
            }
            else {
                Map<String, Object> data = new HashMap<>();
                data.put("turnType", "shot");
                data.put("timestamp", System.currentTimeMillis() / 1000L);
                data.put("i", i);
                data.put("j", j);
                db.collection("games").document(gameId).collection("turns")
                        .add(data);
            }
        }
        else {
            Toast.makeText(getActivity(), "You have already shot here!", Toast.LENGTH_LONG).show();
        }
    }

    private void initFields(View view) {
        Resources res = getResources();

        for(int i = 0; i < 100; i++) {
            int id = res.getIdentifier("fieldButton" + i, "id", getContext().getPackageName());
            Button button = view.findViewById(id);

            FieldButton fb = new FieldButton();
            fb.button = button;
            fb.isPressed = false;

            button.setOnClickListener((View v) ->
                    pressOpponentFieldButton(fb)
            );
            button.setEnabled(false);

            opponentField.add(fb);
        }

        for(int i = 100; i < 200; i++) {
            int id = res.getIdentifier("fieldButton" + i, "id", getContext().getPackageName());
            Button button = view.findViewById(id);

            FieldButton fb = new FieldButton();
            fb.button = button;
            fb.isPressed = false;

            button.setOnClickListener((View v) ->
                pressYourFieldButton(fb)
            );

            yourField.add(fb);
        }
    }

    private void setOpponentFieldEnabled(boolean enabled) {
        for(FieldButton fb : opponentField) {
            fb.button.setEnabled(enabled);
        }
    }

    private void updateUiOnHit(boolean hit, FieldButton fieldButton) {
        if(hit) {
            fieldButton.button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.red));
        }
        else {
            fieldButton.button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.yellow));
        }
    }

    private void processGameOver(boolean isWin) {
        setOpponentFieldEnabled(false);

        if(isWin) {
            Toast.makeText(getActivity(), "You have won!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getActivity(), "You have lost!", Toast.LENGTH_LONG).show();
        }


        FirebaseUser user = auth.getCurrentUser();
        Map<String, Object> userData = new HashMap<>();
        userData.put("winsCount", user.getUid());
        DocumentReference docRef = db.collection("profiles").document(user.getUid());
        docRef.get().addOnCompleteListener((Task<DocumentSnapshot> task) -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                Map<String, Object> data = new HashMap<>();
                if(isWin) {
                    data.put("winsCount", doc.getLong("winsCount") + 1);
                }
                else {
                    data.put("lossesCount", doc.getLong("lossesCount") + 1);
                }
                docRef.update(data);
            }
            else {
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
