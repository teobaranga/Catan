package com.mygdx.catan;

import com.mygdx.catan.enums.FishTokenType;

import java.util.EnumMap;

public class FishTokenMap extends EnumMap<FishTokenType, Integer> {

    public FishTokenMap() {
        super(FishTokenType.class);
        // To avoid null pointer exceptions
        put(FishTokenType.ONE_FISH, 0);
        put(FishTokenType.TWO_FISH, 0);
        put(FishTokenType.THREE_FISH, 0);
    }

    public int getOneFish() {
        return get(FishTokenType.ONE_FISH);
    }

    public int getTwoFish() {
        return get(FishTokenType.TWO_FISH);
    }

    public int getThreeFish() {
        return get(FishTokenType.THREE_FISH);
    }
}
