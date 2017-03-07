package com.mygdx.catan;

import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.gameboard.Village;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("serial")
public class CoordinatePair extends Pair<Integer, Integer> {

    private Integer x;
    private Integer y;
    private HarbourKind aHarbourKind;
    private Village occupyingVillage;

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
        return occupyingVillage != null;
    }

    /**
     * @return the occupying village or null in case this coordinate pair does not
     * represent an intersection or if the intersection is not occupied
     */
    public Village getOccupyingVillage() {
        return occupyingVillage;
    }

    /**
     * Puts a village on this coordinate pair. Assert that this CoordinatePair represents
     * an intersection. Does nothing if the intersection is already occupied. Sets the
     * intersection to occupied as a result if successful.
     */
    public void putVillage(Village village) {
        occupyingVillage = village;
    }
}
