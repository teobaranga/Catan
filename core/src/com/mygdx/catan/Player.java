package com.mygdx.catan;

import java.util.ArrayList;

import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.PlayerStatus;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Village;

public class Player {

	PlayerStatus status;
	private PlayerColor color;
	private ArrayList<EdgeUnit> roadsAndShips = new ArrayList<EdgeUnit>();
	private ArrayList<Village> villages = new ArrayList<Village>();
	private int defenderOfCatanVPs;
	private int gold;
	
	public Player(Account playerAccount, PlayerColor pColor) {
		color = pColor;
	}
	
	public PlayerStatus getPlayerStatus() {
		return status;
	}
	
	public void setPlayerStatus(PlayerStatus status) {
		this.status = status;
	}
		
	
	public int getDefenderOfCatanVPs() {
		return defenderOfCatanVPs;
	}
	
	public void setDefenderOfCatanVPs(int defenderOfCatanVPs) {
		this.defenderOfCatanVPs = defenderOfCatanVPs;
	}
	
	public int getGold() {
		return gold;
	}
	
	public void setGold(int gold) {
		this.gold = gold;
	}
	
	public ArrayList<EdgeUnit> getRoadsAndShips() {
		return roadsAndShips;
	}
	
	/**
	 * adds given EdgeUnit to roadsAndShips. Its position and and type is assumed to be legal. 
	 * @param edge unit. 
	 * */
	public void addEdgeUnit(EdgeUnit unit) {
		roadsAndShips.add(unit);
	}
	
	/**
	 * adds given village to villages. Its position and and type is assumed to be legal. 
	 * @param village. 
	 * */
	public void addVillage(Village v) {
		villages.add(v);
	}
	
	public PlayerColor getColor() {
		return color;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Player)) {return false;}
	    Player otherPlayer = (Player)other;
	    return this.color.equals(otherPlayer.getColor());
	} 	
}
