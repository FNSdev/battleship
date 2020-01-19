package com.fnsdev.battleship.Views.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.fnsdev.battleship.R;
import com.fnsdev.battleship.Views.Fragments.MainScreenFragment;
import com.fnsdev.battleship.Views.Fragments.SignUpFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(auth.getCurrentUser() != null && fragmentManager.findFragmentByTag("MainScreenFragment") == null) {
            Fragment fragment = new MainScreenFragment();
            transaction.replace(R.id.frameLayout, fragment, "MainScreenFragment");
        }
        else if(fragmentManager.findFragmentByTag("MainScreenFragment") == null && fragmentManager.findFragmentByTag("SignUpFragment") == null){
            Fragment fragment = new SignUpFragment();
            transaction.replace(R.id.frameLayout, fragment, "SignUpFragment");
        }
        transaction.commit();

        setContentView(R.layout.activity_main);
    }
}
