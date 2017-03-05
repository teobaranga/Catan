package com.mygdx.catan;

import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player {

    /** The account of this player */
    private Account account;

    private PlayerColor color;
    private ArrayList<EdgeUnit> roadsAndShips = new ArrayList<>();
    private ArrayList<Village> villages = new ArrayList<>();
    private int defenderOfCatanPoints;

    private int tokenVictoryPoints;
    private ResourceMap resourceMap = new ResourceMap();

    private int availableSettlements;
    private int availableCities;
    private int availableRoads;
    private int availableShips;

    public static Player newInstance(Account account, PlayerColor color) {
        final Player player = new Player();
        player.account = account;
        player.color = color;
        player.availableSettlements = 5;
        player.availableCities = 4;
        player.availableRoads = 15;
        player.availableShips = 15;

        return player;
    }

    public int getAvailableSettlements() { return availableSettlements; }
    public int getAvailableCities() { return availableCities; }
    public int getAvailableRoads() { return availableRoads; }
    public int getAvailableShips() { return availableShips; }

    public void decrementAvailableSettlements() { availableSettlements--; }
    public void incrementAvailableSettlements() { availableSettlements++; }
    public void decrementAvailableCities() { availableCities--; }
    public void incrementAvailableCities() { availableCities++; }
    public void decrementAvailableRoads() { availableRoads--; }
    public void incrementAvailableRoads() { availableRoads++; }
    public void decrementAvailableShips() { availableShips--; }
    public void incrementAvailableShips() { availableShips++; }

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

    /**
     * @param rm of rm is the resource map in question. need to see if player can pay for rm
     * @return true if player has enough resources for rm
     * */
    public boolean hasEnoughResources(ResourceMap rm) {
        for (ResourceKind key: rm.keySet()){
            if(resourceMap.containsKey(key) && resourceMap.get(key) >= 1) {
                continue;
            }
            else{
                return false;
            }
        }
        return true;
    }
    public int getHighestHarbourLevel(ResourceKind resKind) {
        CoordinatePair pos;
        HarbourKind hKind;
        int highest = 4;
        for (Village v: villages){
            pos = v.getPosition();
            hKind = pos.getHarbourKind();
            if (hKind == HarbourKind.SPECIAL_WOOD) {
                if (resKind == ResourceKind.WOOD) {
                    return 2;
                }
            }
            else if (hKind == HarbourKind.SPECIAL_BRICK) {
                if (resKind == ResourceKind.BRICK) {
                    return 2;
                }
            }
            else if (hKind == HarbourKind.SPECIAL_ORE) {
                if (resKind == ResourceKind.ORE){
                    return 2;
                }
            }
            else if (hKind == HarbourKind.SPECIAL_GRAIN) {
                if (resKind == ResourceKind.GRAIN) {
                    return 2;
                }
            }
            else if (hKind == HarbourKind.SPECIAL_WOOL) {
                if (resKind == ResourceKind.WOOL) {
                    return 2;
                }
            }
            else if(hKind == HarbourKind.GENERIC) {
                highest = 3;
            }
            else if(highest != 3) {
                highest = 4;
            }
        }
        return highest;
    }

    public void addResources(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            resourceMap.put(entry.getKey(), resourceMap.get(entry.getKey()) + entry.getValue());
        }
    }

    public void removeResources(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            resourceMap.put(entry.getKey(), resourceMap.get(entry.getKey()) - entry.getValue());
        }
    }

    public int getProgressCardCount() {
        throw new RuntimeException("getProgressCardCount not yet implemented");
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

    public ArrayList<Village> getVillages() { return villages; }

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

    public ResourceMap getResourceMap() { return resourceMap;}

    /** Get this player's account */
    public Account getAccount() {
        return account;
    }

    /** Get the player's username */
    public String getUsername() {
        return account.getUsername();
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
