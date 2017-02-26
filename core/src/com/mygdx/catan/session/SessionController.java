package com.mygdx.catan.session;

import java.util.ArrayList;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;

public class SessionController {
	private final GameBoardManager aGameBoardManager;
	private final SessionManager aSessionManager;
	private SessionScreen aSessionScreen;
	
	public SessionController(GameBoardManager gbm, SessionManager sm) {
		aGameBoardManager = gbm;
		aSessionManager = sm;
	}
	
	public void setSessionScreen(SessionScreen s) {
		aSessionScreen = s;
	}
	
	public ArrayList<Hex> getHexes() {
		return aGameBoardManager.getHexes();
	}
	
	public ArrayList<CoordinatePair<Integer,Integer>> getIntersectionsAndEdges() {
		return aGameBoardManager.getIntersectionsAndEdges();
	}
	
	public Player[] getPlayers() {
		return aSessionManager.getPlayers();
	}
	
	/**
	 * Requests the GameBoardManager to build settlement on given coordinate. If fromPeer is false, the SessionController verifies that the position is valid, else it simply places the settlement. SessionScreen is notified of any boardgame changes.
	 * @param position of new settlement
	 * @param owner of new settlement
	 * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
	 * @return true if building the settlement was successful, false otherwise 
	 * */
	public boolean buildSettlement(CoordinatePair<Integer,Integer> position, Player owner, boolean fromPeer) {
		// verifies that the intersection is not occupied, and that it is adjacent to a road or ship of player
		// TODO: it does not verify that the intersection is not in the sea, or that it is not adjacent to another Village
		// if (position.isOccupied() || !aGameBoard.isAdjacentToEdgeUnit(position, player)) {return false;}
		return false;
	}
	
	/**
	 * Requests the GameBoardManager to build edge unit on given coordinates. If fromPeer is false, the SessionController verifies that the position is valid, else it simply places the settlement. SessionScreen is notified of any boardgame changes.
	 * Determines if new edge unit piece increases the players longest road, and takes appropriate action.
	 * @param owner of edgeUnit
	 * @param first end point of road or ship
	 * @param second end point of road or ship
	 * @param edge unit kind: ROAD or SHIP
	 * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
	 * @return true if building the unit was successful, false otherwise
	 * */
	public boolean buildEdgeUnit(Player player, CoordinatePair<Integer,Integer> firstPosition, CoordinatePair<Integer,Integer> SecondPosition, EdgeUnitKind kind, boolean fromPeer) {
		//TODO: verify that edgeUnit is between two adjacent coordinates, that those coordinates are free
		// and that the adjacent hexes are compatible with the EdgeUnitKind (in SessionController)
		
		//TODO: longest road
		
		return false;
	}
	
	/**
	 * Requests the GameBoardManager to move the robber to given location. If fromPeer is false, the SessionController verifies that the position is valid.
	 * If valid finds adjacent players to the new position and initiates prompts player who moved the robber to choose a victim.
	 * Informs SessionScreen of new robber position
	 * */
	public boolean moveRobber(Hex newPosition, boolean fromPeer) {
		//TODO: as described above
		return false;
	}
}
