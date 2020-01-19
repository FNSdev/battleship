package com.fnsdev.battleship.Models;

public class Profile {
    public String email;
    public int winsCount;
    public int lossesCount;

    public Profile(String email, int winsCount, int lossesCount) {
        this.email = email;
        this.winsCount = winsCount;
        this.lossesCount = lossesCount;
    }
}
