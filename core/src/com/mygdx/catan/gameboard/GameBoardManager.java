package com.mygdx.catan.gameboard;

import java.util.Iterator;

import com.mygdx.catan.gameboard.GameBoard;

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
	
	
}
