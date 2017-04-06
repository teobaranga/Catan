package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Forwarded request that updates the village at given villageCoord according to barbarians attack rules
 * */
public class UpdateVillage extends ForwardedRequest {

    private Pair<Integer,Integer> villageCoord;

    public static UpdateVillage newInstance(String username, Pair<Integer,Integer> villageCoord) {
        UpdateVillage updateVillage = new UpdateVillage();
        updateVillage.username = username;
        updateVillage.villageCoord = villageCoord;
        return updateVillage;
    }

    public Pair<Integer,Integer> getVillageCoord() {
        return villageCoord;
    }
}
