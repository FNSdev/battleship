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

import com.fnsdev.battleship.R;
import com.fnsdev.battleship.Views.Services.ProfileService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {
    private FirebaseAuth auth;

    private Button signUpButton;
    private Button signInButton;
    private EditText email;
    private EditText password;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        email = view.findViewById(R.id.emailEditText);
        password = view.findViewById(R.id.passwordEditText);

        signUpButton = view.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener((View v) ->
                auth.createUserWithEmailAndPassword(
                        email.getText().toString(),
                        password.getText().toString()
                ).addOnCompleteListener((Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        ProfileService.createProfile(user);

                        toMainScreen();
                    } else {
                        authFailed();
                    }
                })
        );

        signInButton = view.findViewById(R.id.signInButton);
        signInButton.setOnClickListener((View v) ->
            auth.signInWithEmailAndPassword(
                    email.getText().toString(),
                    password.getText().toString()
            ).addOnCompleteListener((Task<AuthResult> task) -> {
                if (task.isSuccessful()) {
                    toMainScreen();
                } else {
                    authFailed();
                }
            })
        );
    }

    private void toMainScreen() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = new MainScreenFragment();
        transaction.replace(R.id.frameLayout, fragment, "MainScreenFragment");
        transaction.commit();
    }

    private void authFailed() {
        Toast.makeText(getActivity(), "Authentication failed",
                Toast.LENGTH_SHORT).show();
    }
}
