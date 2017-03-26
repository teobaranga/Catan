package com.mygdx.catan;

import com.mygdx.catan.enums.EventKind;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class CatanRandom {
    private static CatanRandom instance = null;

    private final Random rand;

    private CatanRandom() {
        rand = new Random();
    }

    public static CatanRandom getInstance() {
        if (instance == null)
            instance = new CatanRandom();
        return instance;
    }

    /** Get two random dice rolls */
    public Pair<Integer, Integer> rollTwoDice() {
        return new ImmutablePair<>(nextDie(), nextDie());
    }

    /** Get a random event */
    public EventKind rollEventDie() {
        return nextEvent();
    }

    /** TEST METHOD, REMOVE THIS WHEN DONE */
    public EventKind rollEventDieBarbarian() {
        return EventKind.BARBARIAN;
    }

    private int nextDie() {
        return rand.nextInt(6) + 1;
    }

    private EventKind nextEvent() {
        //p(barbarian) = 3/6
        //p(trade/politics/science) = 1/6
        int range = rand.nextInt(6) + 1;
        if (range == 1) {
            return EventKind.POLITICS;
        } else if (range == 2) {
            return EventKind.SCIENCE;
        } else if (range == 3) {
            return EventKind.TRADE;
        } else {
            return EventKind.BARBARIAN;
        }
    }
}
