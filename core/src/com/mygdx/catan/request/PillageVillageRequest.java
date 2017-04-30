package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;

/**
 * Targeted request that pillages one of the target's villages
 * */
public class PillageVillageRequest extends TargetedRequest {
    
    private DiceRollPair diceResults;
    
    public static PillageVillageRequest newInstance(String sender, String target, DiceRollPair diceResults) {
        PillageVillageRequest request = new PillageVillageRequest();
        
        request.sender = sender;
        request.target = target;
        request.diceResults = diceResults;
        
        return request;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }
}
