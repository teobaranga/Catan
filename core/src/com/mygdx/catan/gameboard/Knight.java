package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.player.Player;

public class Knight {

    /** Owner of this knight */
    private Player owner;

    /** Position of the knight on the game board */
    private CoordinatePair position;

    /** ID of this knight */
    private int id;

    private boolean active;
    private boolean movedThisTurn;

    /** Strength of the knight. */
    private Strength strength;

    /** Flag indicating whether the knight was promoted this turn */
    private boolean promotedThisTurn;

    /** Flag indicating whether the knight was activated this turn */
    private boolean activatedThisTurn;

    public static Knight newInstance(Player owner, CoordinatePair position, int id) {
        Knight knight = new Knight();
        knight.owner = owner;
        knight.position = position;
        knight.id = id;
        knight.strength = Strength.BASIC;
        return knight;
    }
    
    public Knight() {
        
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasMovedThisTurn() {
        return movedThisTurn;
    }

    public void setHasMovedThisTurn(boolean moved) {
    }

    public Player getOwner() {
        return this.owner;
    }

    public int getStrength() {
        return strength.value;
    }

    public boolean is(Strength strength) {
        return this.strength == strength;
    }
    
    public boolean is(int strength) {
        return this.strength.value == strength;
    }

    public CoordinatePair getPosition() { return position; }

    /** Move the knight */
    public void setPosition(CoordinatePair position) {
        this.position = position;
    }

    /** Activate this knight */
    public void activate() {
        active = true;
        activatedThisTurn = true;
    }

    /** Check if the knight was already activated this turn */
    public boolean isActivatedThisTurn() {
        return activatedThisTurn;
    }

    /** Promote this knight */
    public void promote() {
        // Check for max strength
        if (strength == Strength.BASIC) {
            strength = Strength.STRONG;
            promotedThisTurn = true;
        } else if (strength == Strength.STRONG) {
            strength = Strength.MIGHTY;
            promotedThisTurn = true;
        }
    }

    /** Check if the knight was already promoted this turn */
    public boolean isPromotedThisTurn() {
        return promotedThisTurn;
    }

    /**
     * Reset the knight's flags so that it can be promoted and activated again.
     * Must be done every new turn.
     */
    public void reset() {
        promotedThisTurn = false;
        activatedThisTurn = false;
    }

    public enum Strength {
        BASIC(1),
        STRONG(2),
        MIGHTY(3);

        private int value;

        Strength(int value) {
            this.value = value;
        }
    }
}
