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
}