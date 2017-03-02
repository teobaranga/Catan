package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import java.util.ArrayList;

/**
 * Creates and mutates the state of the GameBoard.
 * */
public class GameBoardManager{

	private static GameBoard aGameBoard;
	
	public GameBoardManager() {
		
		try {
			aGameBoard = new GameBoard();
		}  catch (NullPointerException o) {
			o.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<CoordinatePair<Integer,Integer>> getIntersectionsAndEdges() {
		return aGameBoard.getIntersectionsAndEdges();
	}
	
	public ArrayList<EdgeUnit> getRoadsAndShips() {
	    return aGameBoard.getRoadsAndShips();
	}
	
	public ArrayList<Hex> getHexes() {
		return aGameBoard.getHexes();
	}
	
	public Hex getRobberPosition() {
	    return aGameBoard.getRobberPosition();
	}
	
	/**
	 * decreases the barbarian position by 1. Asserts that it is strictly positive
	 * @return true if barbarian position becomes 0, false otherwise. 
	 * */
	public boolean decreaseBarbarianPosition() {
		assert aGameBoard.getBarbarianPosition() > 0;
		
		int bPos = aGameBoard.getBarbarianPosition() - 1;
		aGameBoard.setBarbarianPosition(bPos);
		
		return bPos == 0;
	}
	
	/**
	 * sets the barbarian position to 7
	 * */
	public void resetBarbarianPosition() {
		aGameBoard.setBarbarianPosition(7);
	}
	
	/**
	 * @return top of progress card stack, null if stack is empty
	 * */
	public ProgressCardKind drawProgressCard() {
		return aGameBoard.popProgressCardStack();
	}
	
	/**
	 * @param player whose merchant point is requested
	 * @return 0 if player does not own merchant, 1 otherwise
	 * */
	public int getMerchantPoint(Player player) {
	    if (aGameBoard.getMerchantOwner().equals(player)) {
	        return 1;
	    } else {
	        return 0;
	    }
	}
	
	//TODO
	public int LongestRoadPoint(Player player) {
		return 0;
	}
	
	/**
	 * builds a settlement for player at given position. Adds the settlement to the player's collection of settlements, and associates it to the given position 
	 * @param player of village
	 * @param position of village
	 * @return true if building the village was successful, false otherwise
	 * */
	public boolean buildSettlement(Player player, CoordinatePair<Integer,Integer> position) {
		Village village = new Village(player,position);
		position.putVillage(village);
		player.addVillage(village);
		return true;
	}
	
	/**
	 * Builds the edge unit according to corresponding input. Adds the edge unit to the player's collection of edge units, as well as the gameboard's collection of edge units
	 * @param player of edgeUnit
	 * @param firstPosition end point of road or ship
	 * @param SecondPosition end point of road or ship
	 * @param kind unit kind: ROAD or SHIP
	 * */
	public void buildEdgeUnit(Player player, CoordinatePair<Integer,Integer> firstPosition, CoordinatePair<Integer,Integer> SecondPosition, EdgeUnitKind kind) {		
		EdgeUnit edgeunit = new EdgeUnit(firstPosition, SecondPosition, kind, player);
		player.addEdgeUnit(edgeunit);
		aGameBoard.addRoadOrShip(edgeunit);
	} 
	
	/**
	 * @return total number of cities on the gameboard
	 * */
	public int getCityCount() {
		int cityCount = 0;
		
		ArrayList<CoordinatePair<Integer,Integer>> edgesAndIntersections = aGameBoard.getIntersectionsAndEdges(); 
		
		for (CoordinatePair<Integer,Integer> coordinate : edgesAndIntersections) {
			if (coordinate.isOccupied()) {
				Village v = coordinate.getOccupyingVillage();
				if (v.getVillageKind().equals(VillageKind.CITY)) {
					cityCount++;
				}
			}
		}
		
		return cityCount;
	}
	
	/**
	 * Places the robber on newPosition
	 * 
	 * @param newPosition Hex to position robber on
	 * */
	public void moveRobber(Hex newPosition) {
	    aGameBoard.setRobberPosition(newPosition);
	}
	
	/**
	 * @param hex to find adjacent intersections of
	 * @return a list of adjacent intersections
	 * */
	public ArrayList<CoordinatePair<Integer,Integer>> getAdjacentIntersections(Hex hex) {
		ArrayList<CoordinatePair<Integer,Integer>> adjacentIntersections = new ArrayList<CoordinatePair<Integer, Integer>>();
	    for (CoordinatePair<Integer,Integer> intersection : aGameBoard.getIntersectionsAndEdges()) {
	        if (hex.isAdjacent(intersection)) {
	            adjacentIntersections.add(intersection);
	        }
	    }
	    return adjacentIntersections;
	}
	
	/**
	 * @param diceNumber of dice roll
	 * @return a list of hexes whose dice number equals diceNumber
	 * */
	public ArrayList<Hex> getProducingHexes(int diceNumber) {
		ArrayList<Hex> producingHexes = new ArrayList<Hex>();
		for (Hex hex : aGameBoard.getHexes()) {
		    if (hex.getDiceNumber() == diceNumber) {
		        producingHexes.add(hex);
		    }
		}
		return producingHexes;
	}

    /**
     * @param intersection checks if this intersection is on land
     * @return true if on land
     * */
	public boolean isOnLand (CoordinatePair<Integer, Integer> intersection){
	    for (Hex h: aGameBoard.getHexes()) {
            for (Hex i : aGameBoard.getHexes()) {
                for (Hex j:aGameBoard.getHexes()) {
                    if (((h.getLeftCoordinate() - 1) == (intersection.getLeft())) &&
                            ((h.getRightCoordinate() - 1) == (intersection.getRight())) &&
                            h.getKind().equals(TerrainKind.SEA) &&

                            (i.getLeftCoordinate() + 1) == (intersection.getLeft()) &&
                            ((i.getRightCoordinate() - 1) == (intersection.getRight())) &&
                            i.getKind().equals(TerrainKind.SEA) &&

                            ((j.getLeftCoordinate()) == (intersection.getLeft())) &&
                            ((j.getRightCoordinate() + 2) == (intersection.getRight())) &&
                            j.getKind().equals(TerrainKind.SEA)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
