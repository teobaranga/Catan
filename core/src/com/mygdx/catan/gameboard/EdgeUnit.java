package com.mygdx.catan.gameboard;

import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;

public class EdgeUnit {

	private EdgeUnitKind kind;
	private CoordinatePair aFirstCoordinate;
	private CoordinatePair aSecondCoordinate;
	private Player owner;
	
	public EdgeUnit(CoordinatePair pFirstCoordinate, CoordinatePair pSecondCoordinate, EdgeUnitKind pKind, Player pOwner) {
		kind = pKind;
		owner = pOwner;
		aFirstCoordinate = pFirstCoordinate;
		aSecondCoordinate = pSecondCoordinate;
	}
	
	/**
	 * @return either SHIP or ROAD (given the kind of this edge unit)
	 * */
	public EdgeUnitKind getKind() {
		return kind;
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
	 * moves this edge unit to new coordinates. If unit is not a ship, or the two given coordinates are not adjacent, unit is not moved
	 * */
	public void moveShip (CoordinatePair newFirstCoordinate, CoordinatePair newSecondCoordinate) {
		
		boolean areAdjacent = (Math.abs(newFirstCoordinate.getLeft() - newSecondCoordinate.getLeft()) + Math.abs(newFirstCoordinate.getRight() - newSecondCoordinate.getRight()) == 2 && newFirstCoordinate.getRight() != newSecondCoordinate.getRight()); 
		if (kind != EdgeUnitKind.SHIP || !areAdjacent) {return;}
		
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
}
