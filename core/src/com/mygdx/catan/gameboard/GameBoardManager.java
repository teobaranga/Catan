package com.mygdx.catan.gameboard;

import java.util.ArrayList;
import java.util.Iterator;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.gameboard.GameBoard;

/**
 * Creates and mutates the state of the GameBoard. Iterator iterates through the hexes of the GameBoard
 * */
public class GameBoardManager implements Iterable<Hex>{

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

	@Override
	public Iterator<Hex> iterator() {
		return aGameBoard.getHexIterator();
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
		// verifies that the intersection is not occupied, and that it is adjacent to a road or ship of player
		// FIXME: it does not verify that the intersection is not in the sea, or that it is not adjacent to another Village
		if (position.isOccupied() || !aGameBoard.isAdjacentToEdgeUnit(position, player)) {return false;}
		
		Village village = new Village(player,position);
		position.putVillage(village);
		return true;
	}
	
	/**
	 * @param player 
	 * */
	public boolean buildEdgeUnit(Player player, CoordinatePair<Integer,Integer> firstPosition, CoordinatePair<Integer,Integer> SecondPosition) {
		return false;
	} 
	
	//TODOe
	public int getCityCount() {
		return 0;
	}
	
	//TODO
	public void moveRobber(CoordinatePair<Integer,Integer> newPosition) {
	
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
