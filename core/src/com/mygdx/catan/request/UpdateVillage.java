package com.mygdx.catan.request;

import com.mygdx.catan.gameboard.Village;

public class UpdateVillage extends ForwardedRequest {

    private Village village;

    public static UpdateVillage newInstance(String username, Village village) {
        UpdateVillage updateVillage = new UpdateVillage();
        updateVillage.username = username;
        updateVillage.village = village;
        return updateVillage;
    }

    public Village getVillage() {
        return village;
    }
}
