package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;

/**
 * targeted request that informs target that the sender has chosen a village to downgrade as a result of barbarian attack
 * */
public class VillagePillaged extends TargetedRequest {
    
    private DiceRollPair diceResults;
    
    public static VillagePillaged newInstance(String sender, String target, DiceRollPair diceResults) {
        VillagePillaged request = new VillagePillaged();
        
        request.sender = sender;
        request.target = target;
        request.diceResults = diceResults;
        
        return request;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }
}
