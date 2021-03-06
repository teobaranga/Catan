package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Creates and mutates the state of the GameBoard.
 */
public class GameBoardManager {

    private static HashMap<GameBoard, GameBoardManager> gameboardManagerInstances;

    static {
        gameboardManagerInstances = new HashMap<>();
    }
    
    /**
     * The gameboard associated with this gameboard manager.
     * Must be private at all times.
     */
    private final GameBoard aGameBoard;

    private GameBoardManager(GameBoard aGameBoard) {
        /*
        try {
            this.aGameBoard = aGameBoard;
        } catch (NullPointerException o) {
            o.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        this.aGameBoard = aGameBoard;
    }

    public static GameBoardManager getInstance(GameBoard aGameBoard) {
        if (!gameboardManagerInstances.containsKey(aGameBoard))
            gameboardManagerInstances.put(aGameBoard, new GameBoardManager(aGameBoard));
        return gameboardManagerInstances.get(aGameBoard);
    }
    
    /**
     * creates the default game board
     * */
    public static GameBoard newDefaultGameboard() { 
        GameBoard gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);

        return gameboard;
    }

    public ArrayList<CoordinatePair> getIntersectionsAndEdges() {
        return aGameBoard.getIntersectionsAndEdges();
    }

    public ArrayList<EdgeUnit> getRoadsAndShips() {
        return aGameBoard.getRoadsAndShips();
    }
    
    public List<Village> getVillages() {
        return aGameBoard.getVillages();
    }

    public ArrayList<Hex> getHexes() {
        return aGameBoard.getHexes();
    }

    public Hex getRobberPosition() {
        return aGameBoard.getRobberPosition();
    }

    public Hex getPiratePosition() {
        return aGameBoard.getPiratePosition();
    }

    public GameBoard getGameBoard() {
        return aGameBoard;
    }

    /**
     * @return top of science progress card stack, null if stack is empty
     */
    public ProgressCardType drawScienceProgressCard() {
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

    public void setMerchantOwner(Player newOwner) {
        aGameBoard.setMerchantOwner(newOwner);
    }
    
    public Player getMerchantOwner() {
        return aGameBoard.getMerchantOwner();
    }
    
    public void setMerchantPosition(Hex pos) {
        aGameBoard.setMerchantPosition(pos);
    }
    
    public Hex getMerchantPosition() {
        return aGameBoard.getMerchantPosition();
    }
    
    public void setRobberPosition(Hex pos) {
        aGameBoard.setRobberPosition(pos);
    }

    public void setPiratePosition(Hex pos) {
        aGameBoard.setaPiratePosition(pos);
    }

    /**
     *
     * Checks if the given player suffers from the Old Boot malus.
     *
     * @param player the Player whose potential malus is requested
     * @return 1 if malus applies, 0 otherwise.
     */
    public int getBootMalus(Player player) {
        if(aGameBoard.getaBootOwner() == null){
            return 0;
        }
        if (player.getUsername().equals(aGameBoard.getaBootOwner().getUsername())) {
            return 1;
        }
        return 0;
    }

    public Player getBootOwner(){
        return aGameBoard.getaBootOwner();
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
        Village village = Village.newInstance(player, position);
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
        EdgeUnit edgeunit = EdgeUnit.newEdgeUnit(firstPosition, SecondPosition, kind, player);
        player.addEdgeUnit(edgeunit);
        aGameBoard.addRoadOrShip(edgeunit);
        if(kind == EdgeUnitKind.ROAD) { player.decrementAvailableRoads(); }
        if(kind == EdgeUnitKind.SHIP) { player.decrementAvailableShips(); }
    }

    public void buildCityWall(Player player, CoordinatePair myCityWall) {
        Village city = myCityWall.getOccupyingVillage();
        city.setCityWalls(true);
    }
    
    public void destroyCityWall(CoordinatePair wallPosition) {
        Village city = wallPosition.getOccupyingVillage();
        city.setCityWalls(false);
    }

    /**
     * Creates a new knight and adds it to the game board. By default, the knight created has
     * basic strength and is inactive.
     *
     * @param player           owner of the knight
     * @param myKnightPosition location of the knight
     * @return the knight that was build
     */
    public Knight buildKnight(Player player, CoordinatePair myKnightPosition) {
        Knight myKnight = Knight.newInstance(player, myKnightPosition, aGameBoard.nextKnightId());
        player.addKnight(myKnight);
        aGameBoard.addKnight(myKnight, myKnight.getId());
        return myKnight;
    }

    /**
     * removes given edge unit from the board, and back into players inventory 
     * @param unit to remove from board
     * */
    public void displaceRoad(EdgeUnit unit) {
        // removes edge from board
        aGameBoard.removeRoadOrShip(unit);
        
        // removed edge from players list of edges, and increments appropriate available game piece
        unit.getOwner().removeEdgeUnit(unit);
        if (unit.getKind() == EdgeUnitKind.ROAD) {
            unit.getOwner().incrementAvailableRoads();
        } else {
            unit.getOwner().incrementAvailableShips();
        }
    }
    

    /**TODO: you may move an active knight to another intersection. in order for the knight to move, you must have built roads
     * linking the intersection the knight is moving from to the intersection that he is moving to
     * @param id ID of the knight you'd like to move
     * @param position desired position
     */
    public void moveKnight(int id, CoordinatePair position) {
        Knight knight = aGameBoard.getKnight(id);
        knight.getPosition().putKnight(null);
        knight.setPosition(position);
        position.putKnight(knight);

        knight.setActive(false);
    }

    public void activateKnight(Knight k) {
        k.setActive(true);
    }

    public void activateKnight(int id) {
        aGameBoard.getKnight(id).activate();
    }

    public void promoteKnight(int id) {
        aGameBoard.getKnight(id).promote();
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
            if (pair.isOccupiedByVillage()) {
                adjacentVillages.add(pair.getOccupyingVillage());
            }
        }
        return adjacentVillages;
    }

    public ArrayList<EdgeUnit> getAdjacentShips(Hex hex) {
        ArrayList<EdgeUnit> adjacentShips = new ArrayList<>();
        for (CoordinatePair intersection1 : getAdjacentIntersections(hex)) {
            for (CoordinatePair intersection2 : getAdjacentIntersections(hex)) {
                EdgeUnit edgeUnit = getEdgeUnitFromCoordinatePairs(intersection1,intersection2);
                if ( edgeUnit != null && edgeUnit.getKind() ==  EdgeUnitKind.SHIP) {
                    adjacentShips.add(edgeUnit);
                }
            }
        }
        return adjacentShips;
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
            if (h.getKind() == TerrainKind.SEA || h.getKind() == TerrainKind.SMALL_FISHERY || h.getKind() == TerrainKind.BIG_FISHERY) {
                seaHexes++;
            }
        }
        return seaHexes < neighbouringHexes.size();
    }

    public boolean isOnSea(CoordinatePair intersection) {
        for (Hex h : getNeighbouringHexes(intersection)) {
            if (h.getKind() == TerrainKind.SEA || h.getKind() == TerrainKind.SMALL_FISHERY) {
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

    public static CoordinatePair getCoordinatePairFromCoordinates(int leftCoordinate, int rightCoordinate, GameBoard gameBoard) {
        for (CoordinatePair intersection : gameBoard.getIntersectionsAndEdges()) {
            if (intersection.getLeft().equals(leftCoordinate) && intersection.getRight().equals(rightCoordinate)) {
                return intersection;
            }
        }
        return null;
    }

    public CoordinatePair getCoordinatePair(int x, int y) {
        return aGameBoard.getIntersections().get(x, y);
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
    
    /**
     * @param leftCoordinate x coordinate of hex
     * @param rightCoordinate y coordinate of hex
     * @return the hex with coordinates <x,y>, null if such an intersection does not exist
     * */
    public static Hex getHexFromCoordinates(int leftCoordinate, int rightCoordinate, GameBoard gameboard) {
        for (Hex hex : gameboard.getHexes()) {
            if (hex.getLeftCoordinate() == leftCoordinate && hex.getRightCoordinate() == rightCoordinate) {
                return hex;
            }
        }
        return null;
    }
    
    /**
     * @precondition it is assumed firstCoordinatePair and secondCoordinatePair are distinct
     * @param firstCoordinatePair first CoordinatePair of Road
     * @param secondCoordinatePair second CoordinatePair of Road
     * @return the EdgeUnit on board with given endpoints, null if such an edge does not exist 
     * */
    public EdgeUnit getEdgeUnitFromCoordinatePairs(CoordinatePair firstCoordinatePair, CoordinatePair secondCoordinatePair) {
        for (EdgeUnit edge : aGameBoard.getRoadsAndShips()) {
            if (edge.hasEndpoint(firstCoordinatePair) && edge.hasEndpoint(secondCoordinatePair)) {
                return edge;
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
        for (Hex h : aGameBoard.getHexes()) {
            if (h.isAdjacent(pair) && h.getKind() == tKind) {
                return true;
            }
        }
        return false;
    }

    //TODO:
    public void removeKnight(Object myKnightCoordinates) {

    }

    public ArrayList<CoordinatePair> getEmptyCoordinates(){
        ArrayList<CoordinatePair> emptyCoordinates = new ArrayList<>();
        for(CoordinatePair pair: aGameBoard.getIntersectionsAndEdges()) {
            if(!pair.hasKnight() && !pair.isOccupied()) {
                emptyCoordinates.add(pair);
            }
        }
        return emptyCoordinates;
    }

}
