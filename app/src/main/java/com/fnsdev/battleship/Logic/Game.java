package com.fnsdev.battleship.Logic;

import com.fnsdev.battleship.Models.FieldButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String id;
    private int[][] yourField = new int[10][10];
    private int[][] opponentField = new int[10][10];


    public Game(String id) {
        this.id = id;
    }

    public void setYourField(List<FieldButton> fieldButtons) {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                FieldButton btn = fieldButtons.get(i * 10 + j);
                if(btn.isPressed) {
                    yourField[i][j] = 1;
                }
                else{
                    yourField[i][j] = 0;
                }
            }
        }
    }

    public void setOpponentField(List<Long> field) {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                opponentField[i][j] = field.get(i * 10 + j).intValue();
            }
        }
    }

    public String processOpponentShot(int i, int j) {
        String stateType = getStateType(i, j, yourField, -123, -123);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis() / 1000L);
        data.put("i", i);
        data.put("j", j);
        data.put("user", "client");

        if(stateType.equalsIgnoreCase("kill") && hasFinished(yourField)) {
            stateType = "loss";
            data.put("stateType", "win");
        }
        else {
            data.put("stateType", stateType);
        }

        db.collection("games").document(id).collection("states").add(data);

        return stateType;
    }

    public String proccessYourShot(int i, int j) {
        String stateType = getStateType(i, j, opponentField, -123, -123);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis() / 1000L);
        data.put("i", i);
        data.put("j", j);
        data.put("user", "host");

        if(stateType.equalsIgnoreCase("kill") && hasFinished(opponentField)) {
            stateType = "win";
            data.put("stateType", "loss");
        }
        else {
            data.put("stateType", stateType);
        }

        db.collection("games").document(id).collection("states").add(data);

        return stateType;
    }

    private String getStateType(int i, int j, int[][] field, int doNotCheckI, int doNotCheckJ) {
        if(field[i][j] == 0) {
            return "miss";
        }

        field[i][j] = 2;
        String[] states = new String[]{"kill", "kill", "kill", "kill"};

        if(i + 1 < 10 && (i + 1 != doNotCheckI || j != doNotCheckJ)) {
            if(field[i + 1][j] == 1) {
                return "hit";
            }
            else if(field[i + 1][j] == 2) {
                states[0] = getStateType(i + 1, j, field, i, j);
            }
        }

        if(i - 1 >= 0 && (i - 1 != doNotCheckI || j != doNotCheckJ)) {
            if(field[i - 1][j] == 1) {
                return "hit";
            }
            else if(field[i - 1][j] == 2) {
                states[1] = getStateType(i - 1, j, field, i, j);
            }
        }

        if(j + 1 < 10 && (i != doNotCheckI || j + 1 != doNotCheckJ)) {
            if(field[i][j + 1] == 1) {
                return "hit";
            }
            else if(field[i][j + 1] == 2) {
                states[2] = getStateType(i, j + 1, field, i, j);
            }
        }

        if(j - 1 >= 0 && (i != doNotCheckI || j - 1 != doNotCheckJ)) {
            if(field[i][j - 1] == 1) {
                return "hit";
            }
            else if(field[i][j - 1] == 2) {
                states[3] = getStateType(i, j - 1, field, i, j);
            }
        }

        for(String state : states) {
            if(state.equalsIgnoreCase("hit")) {
                return "hit";
            }
        }

        return "kill";
    }

    private boolean hasFinished(int[][] field) {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                if(field[i][j] == 1) {
                    return false;
                }
            }
        }
        return true;
    }
}
