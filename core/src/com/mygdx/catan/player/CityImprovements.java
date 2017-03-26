package com.mygdx.catan.player;

public class CityImprovements {
    /** Maximum level of improvement */
    private static final int MAX_LEVEL = 4;

    private int trade, science, politics;

    public int getTradeLevel() {
        return trade;
    }

    public int getPoliticsLevel() {
        return politics;
    }

    public int getScienceLevel() {
        return science;
    }

    public void upgradeTradeLevel() {
        if (trade != MAX_LEVEL)
            trade++;
    }

    public void upgradePoliticsLevel() {
        if (politics != MAX_LEVEL)
            politics++;
    }

    public void upgradeScienceLevel() {
        if (science != MAX_LEVEL)
            science++;
    }
}
