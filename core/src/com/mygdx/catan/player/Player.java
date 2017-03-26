package com.mygdx.catan.player;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.FishTokenMap;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.gameboard.Village;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Player {

    /** The account of this player */
    private Account account;

    private PlayerColor color;
    private List<EdgeUnit> roadsAndShips;
    private List<Village> villages;
    private List<Knight> knights; //TODO: keep track of knights for each player
    private int defenderOfCatanPoints;

    private int tokenVictoryPoints;
    private ResourceMap resourceMap;
    private CityImprovements cityImprovements;
    private EnumMap<ProgressCardType, Integer> hand;
    private FishTokenMap fishTokenHand;

    private int availableSettlements;
    private int availableCities;
    private int availableMetropolis; //need this for barbarian attack
    private int availableRoads;
    private int availableShips;

    public Player() {
        roadsAndShips = new ArrayList<>();
        villages = new ArrayList<>();
        knights = new ArrayList<>();
        resourceMap = new ResourceMap();
        cityImprovements = new CityImprovements();
        fishTokenHand = new FishTokenMap();
        hand = new EnumMap<>(ProgressCardType.class);


        // Set the default number of available pieces
        tokenVictoryPoints = 0;
        availableSettlements = 5;
        availableCities = 4;
        availableRoads = 15;
        availableShips = 15;
    }

    public static Player newInstance(Account account, PlayerColor color) {
        final Player player = new Player();
        player.account = account;
        player.color = color;
        return player;
    }

    public int getAvailableSettlements() {
        return availableSettlements;
    }

    public int getAvailableCities() {
        return availableCities;
    }

    public int getAvailableMetropolis() {
        return availableMetropolis;
    }

    public int getAvailableRoads() {
        return availableRoads;
    }

    public int getAvailableShips() {
        return availableShips;
    }

    public void decrementAvailableSettlements() {
        availableSettlements--;
    }

    public void incrementAvailableSettlements() {
        availableSettlements++;
    }

    public void decrementAvailableCities() {
        availableCities--;
    }

    public void incrementAvailableCities() {
        availableCities++;
    }

    public void decrementAvailableRoads() {
        availableRoads--;
    }

    public void incrementAvailableRoads() {
        availableRoads++;
    }

    public void decrementAvailableShips() {
        availableShips--;
    }

    public void incrementAvailableShips() {
        availableShips++;
    }

    /**
     * Adds a progress card with given type to the player's hand
     *
     * @param card type that gets added to the Player's hand
     */
    public void addProgressCard(ProgressCardType card) {
        hand.put(card, hand.get(card) + 1);
    }

    /**
     * Removes a progress card with given type from the player's hand
     *
     * @param card type that gets removed from the Player's hand
     */
    public void removeProgressCard(ProgressCardType card) {
        hand.put(card, hand.get(card) - 1);
    }

    public int getDefenderOfCatanPoints() {
        return defenderOfCatanPoints;
    }

    public List<Knight> getKnights() {
        return knights;
    }

    public List<Knight> getActiveKnights() {
        return knights.stream().filter(Knight::isActive).collect(Collectors.toList());
    }

    /** Add a new knight to this player */
    public void addKnight(Knight knight) {
        knights.add(knight);
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

    public void incrementTokenVictoryPoints() {
        tokenVictoryPoints++;
    }

    /**
     * Check if the player has enough resources.
     *
     * @param rm of rm is the resource map in question. need to see if player can pay for rm
     * @return true if player has enough resources for rm
     */
    public boolean hasEnoughResources(ResourceMap rm) {
        for (ResourceKind key : rm.keySet()) {
            if (resourceMap.get(key) < rm.get(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the player has enough of a specific resource/commodity
     *
     * @param resourceKind type of resource/commodity
     * @param count        count of that resource
     */
    public boolean hasEnoughOfResource(ResourceKind resourceKind, int count) {
        return resourceMap.get(resourceKind) >= count;
    }

    public int getHighestHarbourLevel(ResourceKind resKind) {
        CoordinatePair pos;
        HarbourKind hKind;
        int highest = 4;
        for (Village v : villages) {
            pos = v.getPosition();
            hKind = pos.getHarbourKind();
            if (hKind == HarbourKind.SPECIAL_WOOD) {
                if (resKind == ResourceKind.WOOD) {
                    return 2;
                }
            } else if (hKind == HarbourKind.SPECIAL_BRICK) {
                if (resKind == ResourceKind.BRICK) {
                    return 2;
                }
            } else if (hKind == HarbourKind.SPECIAL_ORE) {
                if (resKind == ResourceKind.ORE) {
                    return 2;
                }
            } else if (hKind == HarbourKind.SPECIAL_GRAIN) {
                if (resKind == ResourceKind.GRAIN) {
                    return 2;
                }
            } else if (hKind == HarbourKind.SPECIAL_WOOL) {
                if (resKind == ResourceKind.WOOL) {
                    return 2;
                }
            } else if (hKind == HarbourKind.GENERIC) {
                highest = 3;
            } else if (highest != 3) {
                highest = 4;
            }
        }
        return highest;
    }

    /**
     * Add resources to this player
     */
    public void addResources(ResourceMap cost) {
        resourceMap.add(cost);
    }

    /**
     * Remove resources from this player
     */
    public void removeResources(ResourceMap cost) {
        resourceMap.remove(cost);
    }

    public int getProgressCardCount() {
        throw new RuntimeException("getProgressCardCount not yet implemented");
    }

	/*public ProgressCardType chooseProgressCardType() {
     //not yet needed for this demo
		
	}*/

	public void addFishToken(FishTokenType type) {
        fishTokenHand.merge(type, 1, (a, b) -> a + b);
    }

    public FishTokenMap getFishTokenHand() {
        return fishTokenHand;
    }

    public void removeFishToken(EnumMap<FishTokenType, Integer> fishTokenToRemove) {
        for (EnumMap.Entry<FishTokenType, Integer> entry : fishTokenToRemove.entrySet()) {
            int newCount = fishTokenHand.get(entry.getKey()) - entry.getValue();
            if (newCount < 0) {
                throw new RuntimeException("FishToken count is below 0");
            }
            fishTokenHand.put(entry.getKey(), newCount);
        }
    }

    public Village chooseCity() {
        throw new RuntimeException("chooseCity not yet implemented");
    }

    public ProgressCardType chooseProgressCard() {
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

    /**
     * @return number of resource cards in the player's hand
     * */
    public int getResourceHandSize() {
        int handSize = 0;
        
        for (Map.Entry<ResourceKind, Integer> entry : resourceMap.entrySet()) {
            handSize += entry.getValue();
        }
        
        return handSize;
    }

    public ResourceKind chooseResourceType() {
        throw new RuntimeException("chooseResource not yet implemented");
    }

    public List<EdgeUnit> getRoadsAndShips() {
        return roadsAndShips;
    }

    public List<Village> getVillages() {
        return villages;
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

    public ResourceMap getResources() {
        return resourceMap;
    }

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
