package com.mygdx.catan.session;

import java.util.ArrayList;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
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
	
	public ArrayList<Hex> getHexes() {
		return aGameBoardManager.getHexes();
	}
	
	public Player[] getPlayers() {
		return aSessionManager.getPlayers();
	}
	
	public boolean buildSettlement(CoordinatePair<Integer,Integer> position, boolean fromPeer, Player owner) {
		return false;
	}
	
	public void setSessionScreen(SessionScreen s) {
		aSessionScreen = s;
	}
	
}
