package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.EventKind;

/**
 * Request indicating that the sender player rolled the dice.
 */
public class RollDice extends ForwardedRequest {

    /** Pair of integers representing the red and yellow die values */
    private Pair<Integer, Integer> rollResult;
    private EventKind eventDieResult;

    public static RollDice newInstance(Pair<Integer, Integer> result, String username) {
        RollDice request = new RollDice();
        
        request.rollResult = result;
        request.username = username;
        request.universal = true;
        return request;
    }
    
    public static RollDice newInstance(Pair<Integer, Integer> result, EventKind eventResult, String username) {
        RollDice request = new RollDice();
        request.eventDieResult = eventResult;
        request.rollResult = result;
        request.username = username;
        request.universal = true;
        return request;
    }
    
    
    public Pair<Integer, Integer> getRollResult() {
        return rollResult;
    }
    
    public EventKind getEventDieResult() {
        return eventDieResult;
    }
}
