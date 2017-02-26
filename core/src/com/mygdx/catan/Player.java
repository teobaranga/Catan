package com.mygdx.catan;

import java.util.ArrayList;

import com.mygdx.catan.enums.EventDie;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.PlayerStatus;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;

public class Player {

	private PlayerColor color;
	private ArrayList<EdgeUnit> roadsAndShips = new ArrayList<EdgeUnit>();
	private ArrayList<Village> villages = new ArrayList<Village>();
	private int defenderOfCatanPoints;
	
	private int tokenVictoryPoints;
	private ResourceMap resourceMap = new ResourceMap();
	
	
	public Player(Account playerAccount, PlayerColor pColor) {
		color = pColor;
	}	
	
	public int getDefenderOfCatanPoints() {
		return defenderOfCatanPoints;
	}
	
	public void incrementDefenderOfCatanPoints(int defenderOfCatanPoints) {
		this.defenderOfCatanPoints = defenderOfCatanPoints;
	}
	
	public int getImprovementLevelByType(EventDie type) {
		throw new RuntimeException("getImprovementLevelByType not yet implemented");
	}
	public int getTokenVictoryPoints() {
		return tokenVictoryPoints;
	}
	
	public void setTokenVictoryPoints(int tokenVictoryPoints) {
		this.tokenVictoryPoints = tokenVictoryPoints;
	}
	
	public HarbourKind getHighestHarbourLevel(ResourceKind resKind) {
		throw new RuntimeException("getHighestHarbourLevel not yet implemented");
	}
	
	public void addResources(ResourceMap cost) {
		
	}
	
	public void removeResources(ResourceMap cost) {
		
	}
	
	public int getProgressCardCount() {
		throw new RuntimeException("getProgressCardCount not yet implemented");
	}
	
	public void addRoadOrShip(EdgeUnit edgeUnit){
		
	}
	
	/*public ProgressCardType chooseProgressCardType() {
	 //not yet needed for this demo
		
	}*/
	
	public Village chooseCity() {
		throw new RuntimeException("chooseCity not yet implemented");
	}
	
	public ProgressCardKind chooseProgressCard() {
		throw new RuntimeException("progressCardKind not yet implemented");
	}
	
	public Hex chooseHex() {
		throw new RuntimeException("chooseHex not yet implemented");
	}
	
	public Player choosePlayer(CollectionOfPlayer players) {
		//I made the skeleton for CollectionOfPlayer
		throw new RuntimeException("chooseplayer not yet implemented");
	}
	
	public int chooseResourceIndex(int maxIndex) {
		throw new RuntimeException("chooseResourceIndex not yet implemented");
	}
	
	public int getHandSize() {
		throw new RuntimeException("getHandSize not yet implemented");
	}
	
	public ResourceKind chooseResourceType() {
		throw new RuntimeException("chooseResource not yet implemented");
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
