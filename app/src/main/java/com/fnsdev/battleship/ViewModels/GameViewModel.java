package com.fnsdev.battleship.ViewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

public class GameViewModel extends AndroidViewModel {
    public String gameId;
    public boolean isHost;

    public GameViewModel(Application application) {
        super(application);
    }
}
