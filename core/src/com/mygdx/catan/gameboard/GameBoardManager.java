package com.mygdx.catan.gameboard;

import com.mygdx.catan.gameboard.GameBoard;

public class GameBoardManager {

	private static GameBoard aGameBoard;
	
	public GameBoardManager() {
		try {
			aGameBoard = new GameBoard();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
