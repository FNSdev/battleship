package com.fnsdev.battleship.Views.Services;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileService {
    public static void createProfile(FirebaseUser user) {
        Map<String, Object> profileData = new HashMap<>();

        profileData.put("email", user.getEmail());
        profileData.put("winsCount", 0);
        profileData.put("lossesCount", 0);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles").document(user.getUid()).set(profileData);
    }
}
