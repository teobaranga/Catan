package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Creates and mutates the state of the GameBoard.
 */
public class GameBoardManager {

    private static GameBoardManager instance;
    private static GameBoard aGameBoard;

    private GameBoardManager() {
        try {
            aGameBoard = new GameBoard();
        } catch (NullPointerException o) {
            o.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GameBoardManager getInstance() {
        if (instance == null)
            instance = new GameBoardManager();
        return instance;
    }

    public ArrayList<CoordinatePair> getIntersectionsAndEdges() {
        return aGameBoard.getIntersectionsAndEdges();
    }

    public ArrayList<EdgeUnit> getRoadsAndShips() {
        return aGameBoard.getRoadsAndShips();
    }

    public ArrayList<Hex> getHexes() {
        return aGameBoard.getHexes();
    }

    public Hex getRobberPosition() {
        return aGameBoard.getRobberPosition();
    }

    /**
     * @return top of progress card stack, null if stack is empty
     */
    public ProgressCardKind drawProgressCard() {
        return aGameBoard.popProgressCardStack();
    }

    /**
     * @param player whose merchant point is requested
     * @return 0 if player does not own merchant, 1 otherwise
     */
    public int getMerchantPoint(Player player) {
        if (aGameBoard.getMerchantOwner().equals(player)) {
            return 1;
        } else {
            return 0;
        }
    }

    //TODO
    public int LongestRoadPoint(Player player) {
        return 0;
    }

    /**
     * builds a settlement for player at given position. Adds the settlement to the player's collection of settlements, and associates it to the given position
     *
     * @param player   of village
     * @param position of village
     * @return true if building the village was successful, false otherwise
     */
    public boolean buildSettlement(Player player, CoordinatePair position) {
        Village village = new Village(player, position);
        position.putVillage(village);
        player.addVillage(village);
        aGameBoard.addVillage(village);
        player.decrementAvailableSettlements();
        return true;
    }

    /**
     * builds a settlement for player at given position. Adds the settlement to the player's collection of settlements, and associates it to the given position
     *
     * @param player   owner village
     * @param position of settlement
     * @return true if upgrading the village was successful, false otherwise
     */
    public boolean upgradeSettlement(Player player, CoordinatePair position) {
        Village city = position.getOccupyingVillage();
        city.setVillageKind(VillageKind.CITY);
        position.putVillage(city);
        player.addVillage(city);
        player.decrementAvailableCities();
        player.incrementAvailableSettlements();
        return true;
    }

    /**
     * Builds the edge unit according to corresponding input. Adds the edge unit to the player's collection of edge units, as well as the gameboard's collection of edge units
     *
     * @param player         of edgeUnit
     * @param firstPosition  end point of road or ship
     * @param SecondPosition end point of road or ship
     * @param kind           unit kind: ROAD or SHIP
     */
    public void buildEdgeUnit(Player player, CoordinatePair firstPosition, CoordinatePair SecondPosition, EdgeUnitKind kind) {
        EdgeUnit edgeunit = new EdgeUnit(firstPosition, SecondPosition, kind, player);
        player.addEdgeUnit(edgeunit);
        aGameBoard.addRoadOrShip(edgeunit);
        if(kind == EdgeUnitKind.ROAD) { player.decrementAvailableRoads(); }
        if(kind == EdgeUnitKind.SHIP) { player.decrementAvailableShips(); }
    }

    /**
     * @return total number of cities on the gameboard
     */
    public int getCityCount() {
        int cityCount = 0;

        ArrayList<CoordinatePair> edgesAndIntersections = aGameBoard.getIntersectionsAndEdges();

        for (CoordinatePair coordinate : edgesAndIntersections) {
            if (coordinate.isOccupied()) {
                Village v = coordinate.getOccupyingVillage();
                if (v.getVillageKind().equals(VillageKind.CITY)) {
                    cityCount++;
                }
            }
        }

        return cityCount;
    }

    /**
     * Places the robber on newPosition
     *
     * @param newPosition Hex to position robber on
     */
    public void moveRobber(Hex newPosition) {
        aGameBoard.setRobberPosition(newPosition);
    }

    /**
     * @param hex to find adjacent intersections of
     * @return a list of adjacent intersections
     */
    public ArrayList<CoordinatePair> getAdjacentIntersections(Hex hex) {
        ArrayList<CoordinatePair> adjacentIntersections = new ArrayList<>();
        for (CoordinatePair intersection : aGameBoard.getIntersectionsAndEdges()) {
            if (hex.isAdjacent(intersection)) {
                adjacentIntersections.add(intersection);
            }
        }
        return adjacentIntersections;
    }

    public List<Village> getAdjacentVillages(Hex hex) {
        List<Village> adjacentVillages = new ArrayList<>();
        for (CoordinatePair pair : getAdjacentIntersections(hex)) {
            if (pair.isOccupied()) {
                adjacentVillages.add(pair.getOccupyingVillage());
            }
        }
        return adjacentVillages;
    }

    /**
     * @param diceNumber of dice roll
     * @return a list of hexes whose dice number equals diceNumber
     */
    public List<Hex> getProducingHexes(int diceNumber) {
        List<Hex> producingHexes = new ArrayList<>();
        for (Hex hex : aGameBoard.getHexes()) {
            if (hex.getDiceNumber() == diceNumber) {
                producingHexes.add(hex);
            }
        }
        return producingHexes;
    }

    /**
     * @param intersection checks if this intersection is on land
     * @return true if on land
     */
    public boolean isOnLand(CoordinatePair intersection) {
        int seaHexes = 0;
        ArrayList<Hex> neighbouringHexes = getNeighbouringHexes(intersection);
        for (Hex h : neighbouringHexes) {
            if (h.getKind() == TerrainKind.SEA) {
                seaHexes++;
            }
        }
        return seaHexes < neighbouringHexes.size();
    }

    public boolean isOnSea(CoordinatePair intersection) {
        for (Hex h : getNeighbouringHexes(intersection)) {
            if (h.getKind() == TerrainKind.SEA) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param firstIntersection
     * @param secondIntersection
     * @return true if the edge between the two intersections is on land (assumes the two intersections are adjacent)
     */
    public boolean isOnLand(CoordinatePair firstIntersection, CoordinatePair secondIntersection) {
        int landHexes = 0;
        ArrayList<Hex> neighbouringHexes = getNeighbouringHexes(firstIntersection, secondIntersection);
        for (Hex h : neighbouringHexes) {
            if (h.getKind() == TerrainKind.SEA) {
                landHexes++;
            }
        }

        return landHexes < neighbouringHexes.size();
    }

    /**
     * @param i intersection
     * @return all hexes adjacent to intersection i
     */
    public ArrayList<Hex> getNeighbouringHexes(CoordinatePair i) {
        ArrayList<Hex> nhexes = new ArrayList<Hex>();

        for (Hex h : aGameBoard.getHexes()) {
            if (h.isAdjacent(i)) {
                nhexes.add(h);
            }
        }

        return nhexes;
    }

    /**
     * @param firstIntersection
     * @param secondIntersection
     * @return all hexes adjacent to both the first and second intersections
     */
    public ArrayList<Hex> getNeighbouringHexes(CoordinatePair firstIntersection, CoordinatePair secondIntersection) {
        ArrayList<Hex> nhexes = new ArrayList<Hex>();

        for (Hex h : aGameBoard.getHexes()) {
            if (h.isAdjacent(firstIntersection) && h.isAdjacent(secondIntersection)) {
                nhexes.add(h);
            }
        }

        return nhexes;
    }

    public ArrayList<CoordinatePair> getAttachedIntersections(Hex h) {
        ArrayList<CoordinatePair> attachedIntersections = new ArrayList<>();

        //find all coordinates that make the hex based on the relative coordinates
        for (CoordinatePair i: aGameBoard.getIntersectionsAndEdges()) {
            if (h.isAdjacent(i)) {
                attachedIntersections.add(i);
            }
        }
        return attachedIntersections;
    }

    /**
     * @param p  intersection
     * @return all neighboring intersections that are accessible by one edge units
     */
    public ArrayList<CoordinatePair> getNeighboringIntersections(CoordinatePair p) {

        ArrayList<CoordinatePair> neighboringIntersections = new ArrayList<>();
        ArrayList<Hex> neighboringHexes = getNeighbouringHexes(p);

        //for each hex in the neighboring hexes we will iterate through the attached coordinates to check if
        //the sum of the absolute value of the difference between the pair in question p and aPair is 2
        for (Hex h: neighboringHexes) {
            for (CoordinatePair aPair : getAttachedIntersections(h)) {
                if (abs(aPair.getLeft() - p.getLeft()) + abs((aPair.getRight() - p.getRight())) == 2 && aPair.getRight() != p.getRight()) {
                    neighboringIntersections.add(aPair);
                }
            }
        }
        return neighboringIntersections;
    }

    /**
     *
     * @return all buildings: villages, cities and metropolis currently in play
     */
    public List<Village> getBuildingsInPlay() {
        return aGameBoard.getVillages();
    }
    
    /**
     * @param leftCoordinate x coordinate of intersection 
     * @param rightCoordinate y coordinate of intersection
     * @return the intersection with coordinates <x,y>, null if such an intersection does not exist
     * */
    public CoordinatePair getCoordinatePairFromCoordinates(int leftCoordinate, int rightCoordinate) {
    	for (CoordinatePair intersection : aGameBoard.getIntersectionsAndEdges()) {
    		if (intersection.getLeft().equals(leftCoordinate) && intersection.getRight().equals(rightCoordinate)) {
    			return intersection;
    		}
    	}
    	return null;
    }
}
