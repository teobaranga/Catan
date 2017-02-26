package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.VillageKind;
import java.util.ArrayList;

/**
 * Creates and mutates the state of the GameBoard.
 * */
public class GameBoardManager{

	private static GameBoard aGameBoard;
	//private ArrayList<SessionScreen> sessionScreens = new ArrayList<SessionScreen>();
	
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
	
	public ArrayList<Hex> getHexes() {
		return aGameBoard.getHexes();
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
	
	//TODO
	public int getMerchantPoint(Player player) {
		return 0;
	}
	
	//TODO
	public int LongestRoadPoint(Player player) {
		return 0;
	}
	
	/**
	 * @param owner of village
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
	 * Builds the edge unit according to corresponding input. 
	 * @param owner of edgeUnit
	 * @param first end point of road or ship
	 * @param second end point of road or ship
	 * @param edge unit kind: ROAD or SHIP
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
	
	//TODO
	public void moveRobber(Hex newPosition) {
	}
	
	//TODO
	public ArrayList<CoordinatePair<Integer,Integer>> getAdjacentIntersections(Hex hex) {
		return null;
	}
	
	//TODO
	public ArrayList<Hex> getProducingHexes(int diceNumber) {
		return null;
	}

}
