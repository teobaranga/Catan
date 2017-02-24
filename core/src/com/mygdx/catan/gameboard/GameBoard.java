package com.mygdx.catan.gameboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.TerrainKind;

public class GameBoard {

	private ArrayList<Hex> hexes;
	private ArrayList<CoordinatePair<Integer,Integer>> aIntersectionPositions;
	
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
                CoordinatePair<Integer, Integer> hexCoord = new CoordinatePair<Integer,Integer>(x,y);
                
                // gets the dice number and terrain kind from the hard coded maps in GameRules
                Integer lDiceNumber = aDiceNumberSetup.get(hexCoord.hashCode());
                TerrainKind lTerrainKind = aHexKindSetup.get(hexCoord.hashCode());
                
                // Creates and adds the Hex. If key was not hard coded (or hard coded wrong) the hex is not added and an exception is thrown
                if (lDiceNumber != null || lTerrainKind != null) {
                	hexes.add(new Hex(hexCoord,lTerrainKind,lDiceNumber));
                } else {
                	throw new Exception("Mismatch with GameRules");
                }
                
                
                // Creates the top, and top left points adjacent to current hex
                aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x-1,y*3 - 1));
                aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x, y*3 - 2));                

                // If at last row, create bottom and bottom left points
                if (row == SIZE - 1){
                	aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x-1, y*3 + 1));
                	aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(x, y*3 + 2));
                }
            }
            // If the hex is the last column of a row, creates the top right point
            aIntersectionPositions.add(new CoordinatePair<Integer, Integer>((cols - 1) + 1, (row - half)*3 - 1));
        }
        
        // Create bottom right point of last column and last row
        aIntersectionPositions.add(new CoordinatePair<Integer, Integer>(half + 1, (half)*3 + 1));
	}
	
	public int getNumberofHexes(){
		return hexes.size();
	}

	public Iterator<Hex> getHexIterator() {
		return hexes.iterator();
	}

}


