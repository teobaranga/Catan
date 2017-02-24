package com.mygdx.catan.gameboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.ProgressCardKind;

public class GameBoard {

	private ArrayList<Hex> hexes;
	private ArrayList<CoordinatePair<Integer,Integer>> aIntersectionPositions;
	private int aBarbarianPosition;
	private Hex aRobberPosition;
	private Hex aMerchantPosition;
	private Stack<ProgressCardKind> aProgressCardStack = new Stack<ProgressCardKind>();
	
	private final int SIZE = GameRules.getGameRulesInstance().getSize();
	
	public GameBoard() throws Exception {
		hexes = new ArrayList<Hex>();
		aIntersectionPositions = new ArrayList<CoordinatePair<Integer, Integer>>();

		HashMap<Integer,TerrainKind> aHexKindSetup = GameRules.getGameRulesInstance().getDefaultTerrainKindMap();
		HashMap<Integer,Integer> aDiceNumberSetup = GameRules.getGameRulesInstance().getDefaultDiceNumberMap();
		
        int half = SIZE / 2;

        // initialize hex position coordinates, where x=(aHexPositions[i].getLeft()) and y=(aHexPositions[i].getRight())
        // the coordinates describe the offset from the center.
        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = -cols + 2 * col + 1;
                int y = (row - half);
                CoordinatePair<Integer, Integer> hexCoord = new CoordinatePair<Integer,Integer>(x,y,HarbourKind.NONE);
                
                // gets the dice number and terrain kind from the hard coded maps in GameRules
                Integer lDiceNumber = aDiceNumberSetup.get(hexCoord.hashCode());
                TerrainKind lTerrainKind = aHexKindSetup.get(hexCoord.hashCode());
                
                // Creates and adds the Hex. If key was not hard coded (or hard coded wrong) the hex is not added and an exception is thrown
                if (lDiceNumber != null || lTerrainKind != null) {
                	Hex hex = new Hex(hexCoord,lTerrainKind,lDiceNumber);
                	hexes.add(hex);
                	// set the robber to the desert position
                	if (lTerrainKind == TerrainKind.DESERT) {
                		aRobberPosition = hex;
                	}
                } else {
                	throw new Exception("Mismatch with GameRules");
                }
                
                
                // Creates the top, and top left points adjacent to current hex
                aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x-1,y*3 - 1,HarbourKind.NONE));
                aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x, y*3 - 2,HarbourKind.NONE));                

                // If at last row, create bottom and bottom left points
                if (row == SIZE - 1){
                	aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x-1, y*3 + 1,HarbourKind.NONE));
                	aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x, y*3 + 2,HarbourKind.NONE));
                }
            }
            // If the hex is the last column of a row, creates the top right point
            aIntersectionPositions.add(new CoordinatePair<Integer, Integer>((cols - 1) + 1, (row - half)*3 - 1,HarbourKind.NONE));
        }
        
        // Create bottom right point of last column and last row
        aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(half + 1, (half)*3 + 1,HarbourKind.NONE));
        
        // Initializes barbarian position to 7
        aBarbarianPosition = 7;
        
        // Fills up the progress card stack and shuffles it
        for (ProgressCardKind kind : ProgressCardKind.values()) {
        	int occurence = GameRules.getGameRulesInstance().getProgressCardOccurence(kind);
        	for (int i = 0; i<occurence; i++) {
        		aProgressCardStack.push(kind);
        	}
        }
        Collections.shuffle(aProgressCardStack);
	}
	
	public int getNumberofHexes(){
		return hexes.size();
	}

	public Iterator<Hex> getHexIterator() {
		return hexes.iterator();
	}
	
	public ArrayList<CoordinatePair<Integer,Integer>> getIntersectionsAndEdges() {
		return aIntersectionPositions;
	}
	
	public ArrayList<Hex> getHexes() {
		return hexes;
	}
	
	public void setBarbarianPosition(int newBarbarianPosition) {
		aBarbarianPosition = newBarbarianPosition;
	}
	
	public int getBarbarianPosition() {
		return aBarbarianPosition;
	}
	
	
	/**
	 * @return top card of the progress card stack. returns null if empty
	 * */
	public ProgressCardKind popProgressCardStack() {
		return aProgressCardStack.pop();
	}
	
	/**
	 * @param Hex for the new robber position
	 * sets robber to newRobberPosition
	 * */
	public void setRobberPosition(Hex newRobberPosition) {
		if (newRobberPosition != null) {
			aRobberPosition = newRobberPosition;
		}
	}
	
	/**
	 * @return Hex: robber position. If null some error occured during Game Board setup.
	 * */
	public Hex getRobberPosition() {
		return aRobberPosition;
	}
	
	/**
	 * @param Hex for the new merchant position
	 * sets merchant to newMerchantPosition
	 * */
	public void setMerchantPosition(Hex newMerchantPosition) {
		if (newMerchantPosition != null) {
			aMerchantPosition = newMerchantPosition;
		}
	} 

	/**
	 * @return Hex: robber position. If null Merchant not in play yet.
	 * */
	public Hex getMerchantPosition() {
		return aMerchantPosition;
	}
	
	
	/**
	 * @param position of intersection
	 * @param player whose edge unit needs to be adjacent to given intersection
	 * @return true if given player has an edge unit with end point at given intersection
	 * */
	public boolean isAdjacentToEdgeUnit(CoordinatePair<Integer,Integer> intersection, Player owner) {
		//TODO
		return false;
	}
}


