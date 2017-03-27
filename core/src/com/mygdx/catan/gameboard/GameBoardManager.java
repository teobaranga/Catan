package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.enums.*;

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

    public GameBoard getGameBoard() {return this.aGameBoard;}

    /**
     * @return top of science progress card stack, null if stack is empty
     */
    public ProgressCardType drawProgressCard() {
        return aGameBoard.popScienceProgressCardStack();
    }

    /**
     * @return top of trade progress card stack, null if stack is empty
     */
    public ProgressCardType drawTradeProgressCard() {
        return aGameBoard.popTradeProgressCardStack();
    }

    /**
     * @return top of politics progress card stack, null if stack is empty
     */
    public ProgressCardType drawPoliticsProgressCard() {
        return aGameBoard.popPoliticsProgressCardStack();
    }

    public FishTokenType drawFishToken() {
        return  aGameBoard.popFishTokenStack();
    }

    /**
     * @param player whose merchant point is requested
     * @return 0 if player does not own merchant, 1 otherwise
     */
    public int getMerchantPoint(Player player) {
        if (player.equals(aGameBoard.getMerchantOwner())) {
            return 1;
        }
        return 0;

    }

    /**
     *
     * Checks if the given player suffers from the Old Boot malus.
     *
     * @param player the Player whose potential malus is requested
     * @return 1 if malus applies, 0 otherwise.
     */
    public int getBootMalus(Player player) {
        if (player.equals(aGameBoard.getaBootOwner())) {
            return 1;
        }
        return 0;
    }

    public void setaBootOwner(Player newOwner) {
        aGameBoard.setaBootOwner(newOwner);
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
     * upgrades a settlement to a city for player at given position.
     *
     * @param player   owner village
     * @param position of settlement to upgrade
     * @return true if upgrading the village was successful, false otherwise
     */
    public boolean upgradeSettlement(Player player, CoordinatePair position) {
        Village settlement = position.getOccupyingVillage();
        //TODO: upgrade to metropolis if already a city?

        //note: settlement is already associated to player and position, and list of villages in aGameBoard, and therefore only needs to be upgraded to city
        settlement.setVillageKind(VillageKind.CITY);

        player.decrementAvailableCities();
        player.incrementAvailableSettlements();
        return true;
    }

    public void removeProgressCard(ProgressCardType cardType, ProgressCardKind cardKind) {
        aGameBoard.removeProgressCard(cardType, cardKind);
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

    public void buildCityWall(Player player, CoordinatePair myCityWall) {

    }

    public void buildBasicKnight(Player player, CoordinatePair myKnightPosition) {
        Knight myKnight = new Knight(player, myKnightPosition);
        player.addKnight(myKnight);
        aGameBoard.addKnight(myKnight);
    }




    /**TODO: you may move an active knight to another intersection. in order for the knight to move, you must have built roads
     * linking the intersection the knight is moving from to the intersection that he is moving to
     * @param player current player
     * @param knight the knight you'd like to move
     * @param myKnightPosition desired position
     * @return total number of cities on the gameboard
     */
    public void moveKnight(Player player, Knight knight, CoordinatePair myKnightPosition) {

    }

    public void activateKnight(Knight k) {
        k.setActive(true);
    }

    //TODO: you may move one of your active knights to an intersection occupried by an opponent's knight.
    //you can only displace a knight if he is weaker than the knight you are moving
    //the owner of the displaced knight must move his knight to any empty intersection that is connected
    //by roads to the place from which he was dispalced
    //if there is no valid intersection the knight is removed from the board
    public void displaceKnight(Player player, Knight myKnight, Knight displacedKnight) {

    }

    /**
     * @return total number of cities on the gameboard
     */
    public int getCityCount() {
        int cityCount = 0;
        for (Village village : aGameBoard.getVillages()) {
            if (village.getVillageKind() == VillageKind.CITY) {
                cityCount++;
            }
        }
        return cityCount;
    }

    /**
     * @return total number of metropoles on the gameboard
     */
    public int getMetropolisCount() {
        int metropolisCount = 0;
        for (Village village : aGameBoard.getVillages()) {
            final VillageKind villageKind = village.getVillageKind();
            if (villageKind == VillageKind.POLITICS_METROPOLIS ||
                    villageKind == VillageKind.SCIENCE_METROPOLIS ||
                    villageKind == VillageKind.TRADE_METROPOLIS) {
                metropolisCount++;
            }
        }
        return metropolisCount;
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
    
    /**
     * @param leftCoordinate x coordinate of hex
     * @param rightCoordinate y coordinate of hex
     * @return the hex with coordinates <x,y>, null if such an intersection does not exist
     * */
    public Hex getHexFromCoordinates(int leftCoordinate, int rightCoordinate) {
        for (Hex hex : aGameBoard.getHexes()) {
            if (hex.getLeftCoordinate() == leftCoordinate && hex.getRightCoordinate() == rightCoordinate) {
                return hex;
            }
        }
        return null;
    }


    public int getVillagePoints(Player player) {
        int villagePoints = 0;
        for (Village v : aGameBoard.getVillages()) {
            VillageKind vk = v.getVillageKind();
            if (v.getOwner().equals(player)){
                switch (vk) {
                    case CITY:
                        villagePoints += 2;
                        break;
                    case SETTLEMENT:
                        villagePoints += 1;
                        break;
                    case POLITICS_METROPOLIS:
                    case SCIENCE_METROPOLIS:
                    case TRADE_METROPOLIS:
                        villagePoints += 4;
                    default:
                        break;
                }
            }
        }
        return villagePoints;
    }

    //Returns whether or not a coordinate pair is adjacent to a field hex
    public boolean isAdjacentToCertainHex(TerrainKind tKind, CoordinatePair pair) {
        for (Hex h: aGameBoard.getHexes()) {
            if (h.isAdjacent(pair) && h.getKind() == tKind) {
                return true;
            }
        }
        return false;
    }

}
