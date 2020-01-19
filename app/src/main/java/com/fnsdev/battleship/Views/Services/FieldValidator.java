package com.fnsdev.battleship.Views.Services;

import com.fnsdev.battleship.Models.FieldButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FieldValidator {
    public static boolean validateField(List<FieldButton> fieldButtons) {
        int[][] field = new int[10][10];
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                FieldButton btn = fieldButtons.get(i * 10 + j);
                if(btn.isPressed) {
                    field[i][j] = 1;
                }
                else{
                    field[i][j] = 0;
                }
            }
        }
        return FieldValidator.validateField(field);
    }

    private static boolean findShip(int x, int y, int[][] field, Set<String> visited, List<int[]> ships) {
        if(x - 1 > 0) {
            if(y - 1 > 0) {
                if(field[x - 1][y - 1] == 1) {
                    return false;
                }
            }
            if(y + 1 < 10) {
                if(field[x - 1][y + 1] == 1) {
                    return false;
                }
            }
        }

        if(x + 1 < 10) {
            if(y - 1 > 0) {
                if(field[x + 1][y - 1] == 1) {
                    return false;
                }
            }
            if(y + 1 < 10) {
                if(field[x + 1][y + 1] == 1) {
                    return false;
                }
            }
        }

        int k = 0, l = 0, m = 0;
        for(k = 0; k < ships.size(); k++) {
            for(l = 0; l < ships.size(); l++) {
                if(y + l >= 10 || field[x][y + l] == 0) {
                    break;
                }
                if(l > 0 && x + 1 < 10 && field[x + 1][y + l] == 1) {
                    return false;
                }
                visited.add(String.valueOf(x) + String.valueOf(y + l));
            }

            for(m = 0; m < ships.size(); m++) {
                if(x + m >= 10 || field[x + m][y] == 0) {
                    break;
                }
                if(m > 0 && y + 1 < 10 && field[x + m][y + 1] == 1) {
                    return false;
                }
                visited.add(String.valueOf(x + m) + String.valueOf(y));
            }
        }

        ships.get(Math.max(l, m) - 1)[1] -= 1;
        return !(l > 1 && m > 1);
    }

    private static boolean validateField(int[][] field) {
        List<int[]> sheeps = new ArrayList<>();
        sheeps.add(new int[] {1, 4});
        sheeps.add(new int[] {2, 3});
        sheeps.add(new int[] {3, 2});
        sheeps.add(new int[] {4, 1});

        Set<String> visited = new HashSet<>();
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                if(!visited.contains(String.valueOf(i) + String.valueOf(j)) && field[i][j] == 1) {
                    if(!FieldValidator.findShip(i, j, field, visited, sheeps)) {
                        return false;
                    }
                }
            }
        }
        for(int[] sheep : sheeps) {
            if(sheep[1] != 0) {
                return false;
            }
        }
        return true;
    }
}
