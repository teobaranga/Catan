package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.enums.TerrainKind;

public class Hex {

	private CoordinatePair aCoordinates;
	private TerrainKind aKind;
	private int aDiceNumber;
	
	
	/**
	 * @param pCoordinates Coordinate Pair of the Hex
	 * @param pKind its Terrain Kind
	 * @param pDiceNumber its dice number
	 * */
	public Hex(CoordinatePair pCoordinates, TerrainKind pKind, int pDiceNumber) {
		aCoordinates = pCoordinates;
		aKind = pKind;
		aDiceNumber = pDiceNumber;
	}
	
	public TerrainKind getKind() {
		return aKind;
	}
	
	public int getDiceNumber() {
		return aDiceNumber;
	}
	
	public void setDiceNumber(int newDiceNumber) {
	    aDiceNumber = newDiceNumber;
	}
	
	public int getLeftCoordinate() {
		return aCoordinates.getLeft();
	}
	
	public int getRightCoordinate() {
		return aCoordinates.getRight();
	}
	
	/**
	 * @param aIntersectionCoordinate a Pair<Integer,Integer> representing a pair of coordinates for an intersection
	 * @return true if aIntersectionCoordinate is a vertex of this Hex, false otherwise
	 * */
	public boolean isAdjacent(CoordinatePair aIntersectionCoordinate) {
		int xHex = getLeftCoordinate();
		int yHex = getRightCoordinate();
		int xInter = aIntersectionCoordinate.getLeft();
		int yInter = aIntersectionCoordinate.getRight();
		
		boolean isAdjacent = (
				(xInter == (xHex - 1) && yInter == (3*yHex - 1)) ||
				(xInter == (xHex) && yInter == (3*yHex - 2))     ||
				(xInter == (xHex + 1) && yInter == (3*yHex - 1)) ||
				(xInter == (xHex + 1) && yInter == (3*yHex + 1)) ||
				(xInter == (xHex) && yInter == (3*yHex + 2))     ||
				(xInter == (xHex - 1) && yInter == (3*yHex + 1)) );
		
		return isAdjacent;
	}
}
