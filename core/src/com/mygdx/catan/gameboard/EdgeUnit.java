package com.mygdx.catan.gameboard;

import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.player.Player;

public class EdgeUnit {

	private EdgeUnitKind kind;
	private CoordinatePair aFirstCoordinate;
	private CoordinatePair aSecondCoordinate;
	private Player owner;

	public static EdgeUnit newEdgeUnit(CoordinatePair pFirstCoordinate, CoordinatePair pSecondCoordinate, EdgeUnitKind pKind, Player pOwner) {
        EdgeUnit edgeUnit = new EdgeUnit();
        edgeUnit.kind = pKind;
        edgeUnit.owner = pOwner;
        edgeUnit.aFirstCoordinate = pFirstCoordinate;
        edgeUnit.aSecondCoordinate = pSecondCoordinate;
        return edgeUnit;
    }
	
	/**
	 * @return either SHIP or ROAD (given the kind of this edge unit)
	 * */
	public EdgeUnitKind getKind() {
		return kind;
	}
	
	/**
	 * @param other EdgeUnit 
	 * @return true iff other and this EdgeUnit share an endpoint
	 * */
	public boolean isAdjacent(EdgeUnit other) {
		return (!this.equals(other) && (this.hasEndpoint(other.getAFirstCoordinate()) || this.hasEndpoint(other.getASecondCoordinate())));
	}
	
	/**
     * @param other EdgeUnit 
     * @return true iff other and this EdgeUnit share an endpoint AND are not separated by opponent village
     * */
	public boolean isConnected(EdgeUnit other) {
	    boolean isAdjacent = this.isAdjacent(other);
	    if (isAdjacent && this.getCommonEndpoint(other).isOccupiedByVillage()) {
	        return this.getCommonEndpoint(other).isOccupied(this.owner);
	    }
	    
	    return isAdjacent;
	}
	
	/**
	 * @return the endpoint this EdgeUnit and other have in common.
	 * @precondition assumes the this and other are adjacent
	 * */
	public CoordinatePair getCommonEndpoint(EdgeUnit other) {
	    if (this.aFirstCoordinate.equals(other.aFirstCoordinate) || this.aFirstCoordinate.equals(other.aSecondCoordinate)) {
	        return aFirstCoordinate;
	    } else {
	        return aSecondCoordinate;
	    }
	}
	
	public boolean hasEndpoint(CoordinatePair intersection) {
		return intersection.equals(aFirstCoordinate) || intersection.equals(aSecondCoordinate);
	}
	
	/**
	 * @return true if this EdgeUnit has end point with coordinates xPos and yPos
	 * */
	public boolean hasEndpoint(int xPos, int yPos) {
	    return (xPos == aFirstCoordinate.getLeft() && yPos == aFirstCoordinate.getRight()) || (xPos == aSecondCoordinate.getLeft() && yPos == aSecondCoordinate.getRight());
	}
	
	/**
	 * moves this edge unit to new coordinates. If the two given coordinates are not adjacent, unit is not moved
	 * */
	public void moveEdge (CoordinatePair newFirstCoordinate, CoordinatePair newSecondCoordinate) {
		
		boolean areAdjacent = (Math.abs(newFirstCoordinate.getLeft() - newSecondCoordinate.getLeft()) + Math.abs(newFirstCoordinate.getRight() - newSecondCoordinate.getRight()) == 2 && newFirstCoordinate.getRight() != newSecondCoordinate.getRight()); 
		if (!areAdjacent) {return;}
		
		aFirstCoordinate = newFirstCoordinate;
		aSecondCoordinate = newSecondCoordinate;
	}
	
	public Player getOwner() {
		return owner;
	}

	public CoordinatePair getAFirstCoordinate() {
		return this.aFirstCoordinate;
	}

	public CoordinatePair getASecondCoordinate() {
		return this.aSecondCoordinate;
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
	    if (!(other instanceof EdgeUnit))return false;
	    
	    EdgeUnit otherEdgeUnit = (EdgeUnit) other;
	    
	    return (this.hasEndpoint(otherEdgeUnit.getAFirstCoordinate()) && this.hasEndpoint(otherEdgeUnit.getASecondCoordinate()));
	}

	@Override
	public String toString() {
		return String.format("%s %s %s %s", aFirstCoordinate, aSecondCoordinate, owner, kind);
	}
}
