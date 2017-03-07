package com.mygdx.catan.enums;

public enum GamePhase {
    /**
     * The phase where every player rolls the dice in order
     * to determine which one has the highest roll.
     */
    SETUP_PHASE_ONE,
    /**
     * The phase where every player, in clockwise order, places
     * a settlement and a road.
     */
    SETUP_PHASE_TWO_CLOCKWISE,
    /**
     * The phase where every player, in counter-clockwise order,
     * places a city and a road.
     */
    SETUP_PHASE_TWO_COUNTERCLOCKWISE,
    /**
     * The phase where a player must roll the dice to determine
     * the resource production and the event.
     */
    TURN_FIRST_PHASE,
    /**
     * The phase where a player can build, trade or end his/her
     * turn.
     */
    TURN_SECOND_PHASE,
    Completed
}
