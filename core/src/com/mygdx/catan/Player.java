package com.mygdx.catan;

import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.CoordinatePair;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private PlayerColor color;
    private ArrayList<EdgeUnit> roadsAndShips = new ArrayList<>();
    private ArrayList<Village> villages = new ArrayList<>();
    private int defenderOfCatanPoints;
    private List<Player> collectionOfPlayers = new ArrayList<>();

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

    public int getHighestHarbourLevel(ResourceKind resKind) {
        CoordinatePair pos;
        HarbourKind hKind;
        int highest = 4;
        for (v: villages){
            pos = v.getPosition();
            hKind = pos.getHarbourKind();
            if (hKind.equals(HarbourKind.SPECIAL)) {
                //need to check if resKind corresponds with this special harbor
                return 2;
            }
            else if(hKind.equals(HarbourKind.GENERIC)){
                highest = 3;
            }
            else if(highest != 3){
                highest = 4;
            }
            return highest;
        }
    }

    public void addResources(ResourceMap cost) {

    }

    public void removeResources(ResourceMap cost) {

    }

    public int getProgressCardCount() {
        throw new RuntimeException("getProgressCardCount not yet implemented");
    }

    public void addRoadOrShip(EdgeUnit edgeUnit) {

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

    public Player choosePlayer(List<Player> players) {
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
     *
     * @param unit
     */
    public void addEdgeUnit(EdgeUnit unit) {
        roadsAndShips.add(unit);
    }

    /**
     * adds given village to villages. Its position and and type is assumed to be legal.
     *
     * @param v
     */
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
        if (!(other instanceof Player)) {
            return false;
        }
        Player otherPlayer = (Player) other;
        return this.color.equals(otherPlayer.getColor());
    }
}
