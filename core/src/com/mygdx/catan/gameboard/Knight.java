package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.player.Player;

public class Knight {

    /** Owner of this knight */
    private Player owner;

    /** Position of the knight on the game board */
    private CoordinatePair position;

    private boolean active;
    private boolean movedThisTurn;

    /** Strength of the knight. */
    private Strength strength;

    public static Knight newInstance(Player owner, CoordinatePair position) {
        Knight knight = new Knight();
        knight.owner = owner;
        knight.position = position;
        knight.strength = Strength.BASIC;
        return knight;
    }
    
    public Knight() {
        
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

    public CoordinatePair getPosition() { return position; }

    /** Upgrade this knight */
    public void upgrade() {
        // Check for max strength
        if (strength == Strength.BASIC)
            strength = Strength.STRONG;
        else if (strength == Strength.STRONG)
            strength = Strength.MIGHTY;
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
