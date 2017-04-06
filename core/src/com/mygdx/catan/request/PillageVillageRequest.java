package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;

/**
 * Targeted request that pillages one of the target's villages
 * */
public class PillageVillageRequest extends TargetedRequest {
    
    private DiceRollPair diceResults;
    private int totalWorstPlayers;
    
    public static PillageVillageRequest newInstance(String sender, String target, DiceRollPair diceResults, int totalWorstPlayers) {
        PillageVillageRequest request = new PillageVillageRequest();
        
        request.sender = sender;
        request.target = target;
        request.diceResults = diceResults;
        request.totalWorstPlayers = totalWorstPlayers;
        
        return request;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }
    
    public int getTotalWorstPlayers() {
        return totalWorstPlayers;
    }
}
