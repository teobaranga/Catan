package com.mygdx.catan;

public class DiceRollPair {

    private int red, yellow;

    public static DiceRollPair newRoll(int red, int yellow) {
        DiceRollPair diceRoll = new DiceRollPair();
        diceRoll.red = red;
        diceRoll.yellow = yellow;
        return diceRoll;
    }

    public int getRed() {
        return red;
    }

    public int getYellow() {
        return yellow;
    }

    public int getSum() {
        return red + yellow;
    }

    @Override
    public String toString() {
        return String.format("red: %d, yellow: %d", red, yellow);
    }
}
