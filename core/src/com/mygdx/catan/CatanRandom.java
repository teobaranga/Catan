package com.mygdx.catan;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class CatanRandom {
    private Random rand;
    
    private static CatanRandom instance = null;
    private CatanRandom() {
        rand = new Random();
    }
    
    public static CatanRandom getInstance() {
        if (instance == null) {
            instance = new CatanRandom();
        }
        return instance;
    }
    
    public int nextDie() {
        return rand.nextInt(6) + 1;
    }
    
    public void nextEvent() {
        throw new RuntimeException("nextEvent not yet implemented");
    }
    
    public Pair<Integer, Integer> rollTwoDice() {
        return new ImmutablePair<>(nextDie(), nextDie());
    }

}
