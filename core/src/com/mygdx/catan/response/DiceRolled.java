package com.mygdx.catan.response;

import com.mygdx.catan.session.Session;
import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.enums.EventKind;

/**
 * Response sent as result of a player rolling the dice.
 * In the situations where it applies, f the sender player is the last
 * one to roll the dice, a session is also sent which contains an updated
 * state as processed by the server.
 */
public class DiceRolled implements Response {

    /** Username of the player that rolled the dice */
    private String username;

    /** The sum of the dice numbers */
    private DiceRollPair diceRoll;
    
    /** The event die result */
    private EventKind eventKindResult;

    private Session session;

    public static DiceRolled newInstance(String username, DiceRollPair diceRoll, EventKind eventKindResult) {
        final DiceRolled response = new DiceRolled();
        response.username = username;
        response.diceRoll = diceRoll;
        response.eventKindResult = eventKindResult;
        return response;
    }

    public static DiceRolled newInstance(String username, DiceRollPair diceRoll, Session session, EventKind eventKindResult) {
        final DiceRolled response = newInstance(username, diceRoll, eventKindResult);
        response.session = session;
        return response;
    }

    public String getUsername() {
        return username;
    }

    public DiceRollPair getDiceRoll() {
        return diceRoll;
    }

    public boolean isLastRoll() {
        return session != null;
    }

    public Session getSession() {
        return session;
    }
    
    public EventKind getEventKindResult() {
        return eventKindResult;
    }
}
