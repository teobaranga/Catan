package com.mygdx.catan.gameboard;

import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.CoordinatePair;

public class EdgeUnit {

	private EdgeUnitKind kind;
	private CoordinatePair<Integer,Integer> aFirstCoordinate;
	private CoordinatePair<Integer,Integer> aSecondCoordinate;
	
	public EdgeUnit(CoordinatePair<Integer,Integer> pFirstCoordinate, CoordinatePair<Integer,Integer> pSecondCoordinate, EdgeUnitKind pKind) {
		kind = pKind;
		aFirstCoordinate = pFirstCoordinate;
		aSecondCoordinate = pSecondCoordinate;
	}
	
	/**
	 * @return either SHIP or ROAD (given the kind of this edge unit)
	 * */
	public EdgeUnitKind getKind() {
		return kind;
	}
	
	public boolean hasEndpoint(CoordinatePair<Integer,Integer> intersection) {
		return intersection.equals(aFirstCoordinate) || intersection.equals(aSecondCoordinate);
	}
	
	/**
	 * moves this edge unit to new coordinates. If unit is not a ship, or the two given coordinates are not adjacent, unit is not moved
	 * */
	public void moveShip (CoordinatePair<Integer,Integer> newFirstCoordinate, CoordinatePair<Integer,Integer> newSecondCoordinate) {
		
		boolean areAdjacent = (Math.abs(newFirstCoordinate.getLeft() - newSecondCoordinate.getLeft()) + Math.abs(newFirstCoordinate.getRight() - newSecondCoordinate.getRight()) == 2); 
		if (kind != EdgeUnitKind.SHIP || !areAdjacent) {return;}
		
		aFirstCoordinate = newFirstCoordinate;
		aSecondCoordinate = newSecondCoordinate;
	}
}
