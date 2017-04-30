package com.mygdx.catan.request;

import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.enums.EventKind;

public class EventDieHandled extends TargetedRequest {

    private DiceRollPair diceResults;
    private EventKind eventDieResult;
    
    public static EventDieHandled newInstance(DiceRollPair diceResults, EventKind eventDieResult, String sender, String target) {
        EventDieHandled request = new EventDieHandled();
        
        request.diceResults = diceResults;
        request.eventDieResult = eventDieResult;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public DiceRollPair getDiceResults() {
        return diceResults;
    }
    
    public EventKind getEventDieResult() {
        return eventDieResult;
    }
}
