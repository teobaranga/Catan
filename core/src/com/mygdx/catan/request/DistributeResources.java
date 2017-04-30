package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;

public class DistributeResources extends ForwardedRequest {

    private DiceRollPair diceResults;
    
    public static DistributeResources newInstance(DiceRollPair diceResults, String username) {
        DistributeResources request = new DistributeResources();
        
        request.diceResults = diceResults;
        request.universal = true;
        request.username = username;
        
        return request;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }
}
