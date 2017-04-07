package com.mygdx.catan;

import com.mygdx.catan.enums.EventKind;

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

    /**
     * Get a pair of integers representing the roll of the red and yellow dice.
     *
     * @return a pair of integers, first one being the red die, second one being the yellow die
     */
    public DiceRollPair rollTwoDice() {
        return DiceRollPair.newRoll(nextDie(), nextDie());
    }

    /** Get a random event */
    public EventKind rollEventDie() {
        return nextEvent();
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
