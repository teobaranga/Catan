package com.mygdx.catan;

public class Knight {

    private boolean active;
    private boolean movedThisTurn;

    /**
     * Strength of the knight. <p>
     * 0 - Basic
     * 1 - Strong
     * 2 - Mighty
     */
    private int level;

    public Knight() {
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
    }

    public boolean hasMovedThisTurn() {
        return movedThisTurn;
    }

    public void setHasMovedThisTurn(boolean moved) {
    }

    public int getLevel() {
		return level;
    }

    /** Upgrade this knight */
    public void upgrade() {
        // Check for max level
        if (level == 2)
            return;
        level++;
    }
}
