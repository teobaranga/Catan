package com.mygdx.catan.player;

import com.mygdx.catan.enums.CityImprovementTypePolitics;
import com.mygdx.catan.enums.CityImprovementTypeScience;
import com.mygdx.catan.enums.CityImprovementTypeTrade;

public class CityImprovements {
    /** Maximum level of improvement */
    private static final int MAX_LEVEL = 5;

    private int trade, science, politics;

//    private CityImprovementTypeTrade trade;
//    private CityImprovementTypePolitics science;
//    private CityImprovementTypeScience politics;

    public int getTradeLevel() {
        return trade;
    }

    public int getPoliticsLevel() {
        return politics;
    }

    public int getScienceLevel() {
        return science;
    }
    
    public void setTradeLevel(int newTrade) {
        trade = newTrade;
    }
    
    public void setPoliticsLevel(int newPolitics) {
        politics = newPolitics;
    }
    
    public void setScienceLevel(int newScience) {
        science = newScience;
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
