package com.mygdx.catan;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.EventKind;

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
    
    public EventKind nextEvent() {
        //p(barbarian) = 3/6
        //p(trade/politics/science) = 1/6
        int range = rand.nextInt(6) + 1;
        if (range == 1) {
            return EventKind.POLITICS;
        } else if (range == 2) {
            return EventKind.SCIENCE;
        } else if (range == 3 ) {
            return EventKind.TRADE;
        } else {
            return EventKind.BARBARIAN;
        }
    }
    
    public Pair<Integer, Integer> rollTwoDice() {
        return new ImmutablePair<>(nextDie(), nextDie());
    }
    
    public EventKind rollEventDie() {
        return nextEvent();
    }

}
