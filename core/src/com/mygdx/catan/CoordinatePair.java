package com.mygdx.catan;

import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.player.Player;

import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("serial")
public class CoordinatePair extends Pair<Integer, Integer> {

    private Integer x;
    private Integer y;
    private HarbourKind aHarbourKind;
    private Village occupyingVillage;
    private Knight occupyingKnight;

    public static CoordinatePair of(Integer x, Integer y, HarbourKind harbourKind) {
        final CoordinatePair coordinatePair = new CoordinatePair();
        coordinatePair.x = x;
        coordinatePair.y = y;
        coordinatePair.aHarbourKind = harbourKind;
        return coordinatePair;
    }

    /**
     * <p>Throws {@code UnsupportedOperationException}.</p>
     * <p>
     * <p>This pair is immutable, so this operation is not supported.</p>
     *
     * @param value the value to set
     * @return never
     * @throws UnsupportedOperationException as this operation is not supported
     */
    @Override
    public Integer setValue(final Integer value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getLeft() {
        return x;
    }

    @Override
    public Integer getRight() {
        return y;
    }

    @Override
    public int hashCode() {
        return (getKey() == null ? 0 : (getKey().hashCode()+30) * 10) +
                (getValue() == null ? 0 : (getValue().hashCode()+30));
    }

    /**
     * Check if this CoordinatePair is adjacent to another Coordinate pair.
     *
     * @param other the other CoordinatePair
     */
    public boolean isAdjacentTo(CoordinatePair other) {
        return (Math.abs(getLeft() - other.getLeft()) + Math.abs(getRight() - other.getRight()) == 2 &&
                !getRight().equals(other.getRight()));
    }

    public HarbourKind getHarbourKind() {
        return aHarbourKind;
    }

    public boolean isOccupied() {
        return occupyingVillage != null || occupyingKnight != null;
    }
    
    /**
     * @return true iff this CoordinatePair is occupied by a village or a knight owned by given Player
     * */
    public boolean isOccupied(Player owner) {
        if (occupyingVillage != null) {
            return occupyingVillage.getOwner().equals(owner);
        } else if (occupyingKnight != null) {
            return occupyingKnight.getOwner().equals(owner);
        } else {
            return false;
        }
    }

    /**
     * @return the occupying village or null in case this coordinate pair does not
     * represent an intersection or if the intersection is not occupied
     */
    public Village getOccupyingVillage() {
        return occupyingVillage;
    }

    public Knight getOccupyingKnight() {
        return occupyingKnight;
    }

    /**
     * Puts a village on this coordinate pair. Assert that this CoordinatePair represents
     * an intersection. Does nothing if the intersection is already occupied. Sets the
     * intersection to occupied as a result if successful.
     */
    public void putVillage(Village village) {
        occupyingVillage = village;
    }

    public void putKnight(Knight knight) {
        occupyingKnight = knight;
    }

    public boolean hasKnight() {
        for(Knight k: GameBoardManager.getInstance().getGameBoard().getKnights()) {
            if ((x == (k.getPosition().getLeft())) && (y == (k.getPosition().getRight()))) {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }
}
