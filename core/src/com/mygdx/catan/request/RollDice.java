package com.mygdx.catan.request;

import com.mygdx.catan.enums.EventKind;
import com.mygdx.catan.DiceRollPair;

/**
 * Request indicating that the sender player rolled the dice.
 */
public class RollDice extends ForwardedRequest {

    /** Pair of integers representing the red and yellow die values */
    private DiceRollPair rollResult;
    private EventKind eventDieResult;

    public static RollDice newInstance(DiceRollPair result, String username) {
        RollDice request = new RollDice();
        
        request.rollResult = result;
        request.username = username;
        request.universal = true;
        return request;
    }
    
    public static RollDice newInstance(DiceRollPair result, EventKind eventResult, String username) {
        RollDice request = new RollDice();
        request.eventDieResult = eventResult;
        request.rollResult = result;
        request.username = username;
        request.universal = true;
        return request;
    }
    
    
    public DiceRollPair getRollResult() {
        return rollResult;
    }
    
    public EventKind getEventDieResult() {
        return eventDieResult;
    }
}
