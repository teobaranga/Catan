package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.enums.EventKind;

public class DrawProgressCard extends ForwardedRequest {
    
    private DiceRollPair diceResults;
    private EventKind event;
    
    public static DrawProgressCard newInstance(EventKind event, DiceRollPair diceResults, String username) {
        DrawProgressCard request = new DrawProgressCard();
        
        request.username = username;
        request.diceResults = diceResults;
        request.event = event;
        request.universal = true;
        
        return request;
    }
    
    public EventKind getEventKind() {
        return event;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }

}
