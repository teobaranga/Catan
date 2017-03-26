package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.enums.VillageKind;

public class Village {

	private CoordinatePair position;
	private boolean cityWall;
	private VillageKind villageKind;
	private Player owner;
	
	/**
	 * @param pPosition intersection where this Village will be built
	 * */
	public Village(Player pOwner, CoordinatePair pPosition) {
		position = pPosition;
		owner = pOwner;
		cityWall = false;
		villageKind = VillageKind.SETTLEMENT;
	}
	
	/**
	 * @return the CoordinatePair representing the position of this Village
	 * */
	public CoordinatePair getPosition() {
		return position;
	}
	
	/**
	 * @return true if this Village has city walls
	 * */
	public boolean hasCityWalls() {
		return cityWall;
	}
	
	/**
	 * @param newCityWallStatus new city wall status
	 * */
	public void setCityWalls(boolean newCityWallStatus) {
		cityWall = newCityWallStatus;
	}
	
	public VillageKind getVillageKind() {
		return villageKind;
	}
	
	public void setVillageKind(VillageKind newVillageKind) {
		villageKind = newVillageKind;
	}
	
	public Player getOwner() {
		return owner;
	}
	
}
