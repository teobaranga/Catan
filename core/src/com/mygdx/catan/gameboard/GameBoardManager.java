package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.session.SessionManager;

import java.util.ArrayList;
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
     * decreases the barbarian position by 1. Asserts that it is strictly positive
     *
     * @return true if barbarian position becomes 0, false otherwise.
     */
    public boolean decreaseBarbarianPosition() {
        assert aGameBoard.getBarbarianPosition() > 0;

        int bPos = aGameBoard.getBarbarianPosition() - 1;
        aGameBoard.setBarbarianPosition(bPos);

        return bPos == 0;
    }

    /**
     * sets the barbarian position to 7
     */
    public void resetBarbarianPosition() {
        aGameBoard.setBarbarianPosition(7);
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
        return true;
    }

    /**
     * builds a settlement for player at given position. Adds the settlement to the player's collection of settlements, and associates it to the given position
     *
     * @param player   owner village
     * @param position of settlement
     * @return true if upgrading the village was successful, false otherwise
     */
    //TODO
    public boolean upgradeSettlement(Player player, CoordinatePair position) {
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

    public ArrayList<Village> getAdjacentVillages(Hex hex) {
        ArrayList<Village> adjacentVillages = new ArrayList<>();
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
    public ArrayList<Hex> getProducingHexes(int diceNumber) {
        ArrayList<Hex> producingHexes = new ArrayList<>();
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
        /*for (Hex h: aGameBoard.getHexes()) {
            for (Hex i : aGameBoard.getHexes()) {
                for (Hex j:aGameBoard.getHexes()) {
                    if (((h.getLeftCoordinate() - 1) == (intersection.getLeft())) &&
                            ((h.getRightCoordinate() - 1) == (intersection.getRight())) &&
                            h.getKind().equals(TerrainKind.SEA) &&

                            (i.getLeftCoordinate() + 1) == (intersection.getLeft()) &&
                            ((i.getRightCoordinate() - 1) == (intersection.getRight())) &&
                            i.getKind().equals(TerrainKind.SEA) &&

                            ((j.getLeftCoordinate()) == (intersection.getLeft())) &&
                            ((j.getRightCoordinate() + 2) == (intersection.getRight())) &&
                            j.getKind().equals(TerrainKind.SEA)) {
                        return false;
                    }
                }
            }
        }
        return true;*/
        int seaHexes = 0;
        ArrayList<Hex> neighbouringHexes = getNeighbouringHexes(intersection);
        for (Hex h : neighbouringHexes) {
            if (h.getKind() == TerrainKind.SEA) {
                seaHexes++;
            }
        }
        return seaHexes < neighbouringHexes.size();
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
        Integer xCoord = h.getLeftCoordinate();
        Integer yCoord = h.getRightCoordinate();

        //find all coordinates that make the hex based on the relative coordinates
        for (CoordinatePair i: aGameBoard.getIntersectionsAndEdges()) {
            if ((i.getLeft() == xCoord && i.getRight() == 3*yCoord-2) ||
                    (i.getLeft() == xCoord + 1 && i.getRight() == 3 * yCoord - 1) ||
                    (i.getLeft() == xCoord + 1 && i.getRight() == 3 * yCoord + 1) ||
                    (i.getLeft() == xCoord && i.getRight() == 3 * yCoord + 2) ||
                    (i.getLeft() == xCoord - 1 && i.getRight() == 3 * yCoord + 1) ||
                    (i.getLeft() == xCoord - 1 && i.getRight() == 3 * yCoord - 1)) {
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
        ArrayList<CoordinatePair> attachedIntersections = new ArrayList<>();

        //for each hex in the neighboring hexes we will iterate through the attached coordinates to check if
        //the sum of the absolute value of the difference between the pair in question p and aPair is 2
        for (Hex h: neighboringHexes) {
            for (CoordinatePair aPair : getAttachedIntersections(h)) {
                if (abs(aPair.getLeft() - p.getLeft()) + (aPair.getRight() - p.getRight()) == 2) {
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
    public ArrayList<Village> getBuildingsInPlay() {
        Player[] players = SessionManager.getInstance().getPlayers();
        ArrayList<Village> buildingsInPlay = new ArrayList<>();

        for (Player p: players) {
            for (Village v: p.getVillages()) {
                buildingsInPlay.add(v);
            }
        }
        return buildingsInPlay;
    }
}
