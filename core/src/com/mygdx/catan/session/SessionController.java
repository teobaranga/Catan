package com.mygdx.catan.session;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.GameRules;

public class SessionController {
    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private SessionScreen aSessionScreen;
    private PlayerColor aPlayerColor;
    
    public PlayerColor getPlayerColor() {
        return aPlayerColor;
    }
    
    public void setPlayerColor(PlayerColor pc) {
        aPlayerColor = pc;
    }

    public SessionController(GameBoardManager gbm, SessionManager sm) {
        aGameBoardManager = gbm;
        aSessionManager = sm;
    }

    public void setSessionScreen(SessionScreen s) {
        aSessionScreen = s;
    }

    public ArrayList<Hex> getHexes() {
        return aGameBoardManager.getHexes();
    }

    public int getYellowDice() {
        return aSessionManager.getYellowDice();
    }

    public int getRedDice() {
        return aSessionManager.getRedDice();
    }

    public ArrayList<EdgeUnit> getRoadsAndShips() {
        return aGameBoardManager.getRoadsAndShips();
    }

    public ArrayList<CoordinatePair<Integer, Integer>> getIntersectionsAndEdges() {
        return aGameBoardManager.getIntersectionsAndEdges();
    }

    public Player[] getPlayers() {
        return aSessionManager.getPlayers();
    }
    
    public Hex getRobberPosition() {
        return aGameBoardManager.getRobberPosition();
    }

    /**
     * @param a one interesection
     * @param b another intersection
     * @return true if a and b are adjacent
     * */
    private boolean isAdjacent (CoordinatePair<Integer, Integer> a, CoordinatePair<Integer, Integer> b) {
        return (Math.abs(a.getLeft() - b.getLeft()) + Math.abs(a.getRight() - b.getRight()) == 2);
    }

    
    /**
     * @param owner of request
     * @param kind of village requested to be built
     * @return true if owner has the resources to build the requested village kind
     * */
    public boolean requestBuildVillage(PlayerColor owner, VillageKind kind) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        Player currentP = null;
        ResourceMap cost = null;
        for(Player p: aSessionManager.getPlayers()){
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        if (kind.equals(VillageKind.SETTLEMENT)) {
            cost = GameRules.getGameRulesInstance().getSettlementCost();
        }
        else if (kind.equals(VillageKind.CITY)) {
            cost = GameRules.getGameRulesInstance().getCityCost();
        }
        //check to make sure player has sufficient resources
        if (currentP.hasEnoughResources(cost)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * @param owner of request
     * @param kind of edge unit requested to be built
     * @return true if owner has the resources to build the requested village kind
     * */
    public boolean requestBuildEdgeUnit(PlayerColor owner, EdgeUnitKind kind) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        Player currentP = null;
        ResourceMap cost = null;
        for(Player p: aSessionManager.getPlayers()){
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        if (kind.equals(EdgeUnitKind.SHIP)) {
            cost = GameRules.getGameRulesInstance().getShipCost();
        }
        else if (kind.equals(EdgeUnitKind.ROAD)) {
            cost = GameRules.getGameRulesInstance().getRoadCost();
        }
        //check to make sure player has sufficient resources
        if (currentP.hasEnoughResources(cost)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to road owned by player and (2) not adjacent to another village and (3) on land
     * */
    public ArrayList<CoordinatePair<Integer,Integer>> requestValidBuildIntersections(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        //get current player
        Player currentP = null;
        for(Player p: aSessionManager.getPlayers()){
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        //get list of currentp's villages
        ArrayList<Village> listOfVillages = currentP.getVillages();

        //get list of currentp's edge units
        ArrayList<EdgeUnit> listOfEdgeUnits = currentP.getRoadsAndShips();

        ArrayList<CoordinatePair<Integer, Integer>> validIntersections = new ArrayList<>();
        for (CoordinatePair<Integer, Integer> i: aGameBoardManager.getIntersectionsAndEdges()) {
            for (Village v: listOfVillages) {
                for (EdgeUnit e: listOfEdgeUnits) {
                    if ( !isAdjacent(i, v.getPosition()) && (!e.hasEndpoint(i)) && !i.isOccupied() && aGameBoardManager.isOnLand(i)) {
                        validIntersections.add(i);
                    }
                }
            }
            //need to iterate through all players villages and make sure i is not adjacent
            //need to iterate through all players edge units and make sure i is on an edge unit
            //need to check it its on land
        }
        return validIntersections;
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that have an owner's settlement on it
     * */
    public ArrayList<CoordinatePair<Integer,Integer>> requestValidCityUpgradeIntersections(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        
        return null;
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a road or village owned by owner
     * */
    public ArrayList<CoordinatePair<Integer,Integer>> requestValidRoadEndpoints(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        
        // this method will essentially return all the endpoints where you can build a road at any edge 
        // starting at that endpoint (if we disregard the edges that are occupied). The GUI will make sure 
        // none of the edges that are occupied or in water can be chosen.
        
        return null;
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a ship or harbour village owned by owner (2) something something pirate
     * */
    public ArrayList<CoordinatePair<Integer,Integer>> requestValidShipEndpoints(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        
        // same as above but with no edges that are in land can be chosen
        
        return null;
    }
    
    /**
     * Requests the GameBoardManager to build village on given coordinate. SessionScreen is notified of any boardgame changes.
     *
     * @param position of new settlement
     * @param kind of village to build
     * @param owner    of new settlement
     * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if building the village was successful, false otherwise
     */
    public boolean buildVillage(CoordinatePair<Integer, Integer> position, VillageKind kind, PlayerColor owner, boolean fromPeer) {
        Player currentP = null;
        for(Player p: aSessionManager.getPlayers()){
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        aGameBoardManager.buildSettlement(currentP, position);
        if (fromPeer) {
            aSessionScreen.updateIntersection(position, owner, kind);
        }
        else {
            aSessionScreen.updateIntersection(position, owner, kind);
            aSessionManager.updateResourceBar();
        }
        // changes state: of owner and gameboard. All validity checks have been done beforehand. 
        // if method call is from a peer, the gui only needs to be notified of the new gameboard change.
        // otherwise the gui will also need to be notified about resource changes
        //NOTE: if kind is for example city, then all you need to do is upgrade the settlement on that coordinate to a city
        return true;
    }
    
    

    /**
     * Requests the GameBoardManager to build edge unit on given coordinates. If fromPeer is false, the SessionController verifies that the position is valid, else it simply places the settlement. SessionScreen is notified of any boardgame changes.
     * Determines if new edge unit piece increases the players longest road, and takes appropriate action.
     *
     * @param player         owner of edgeUnit
     * @param firstPosition  first end point of road or ship
     * @param SecondPosition second end point of road or ship
     * @param kind           edge unit kind: ROAD or SHIP
     * @param fromPeer       indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if building the unit was successful, false otherwise
     */
    public boolean buildEdgeUnit(Player player, CoordinatePair<Integer, Integer> firstPosition, CoordinatePair<Integer, Integer> SecondPosition, EdgeUnitKind kind, boolean fromPeer) {
        
        //TODO: longest road (fun fact: longest disjoint path problem is NP-hard)

        return false;
    }

    /**
     * Requests the GameBoardManager to move the robber to given location. If fromPeer is false, the SessionController verifies that the position is valid.
     * If valid finds adjacent players to the new position and initiates prompts player who moved the robber to choose a victim.
     * Informs SessionScreen of new robber position
     */
    public boolean moveRobber(Hex newPosition, boolean fromPeer) {
        //TODO: as described above
        return false;
    }

    /**
     * Allows the user to place a city and an edge unit and then receive the resources near the city
     */
    public void placeCityAndRoads(CoordinatePair<Integer, Integer> cityPos, CoordinatePair<Integer, Integer> edgeUnitPos1, CoordinatePair<Integer, Integer> edgeUnitPos2, boolean isShip, boolean fromPeer) {
        Player cp = aSessionManager.getCurrentPlayer();

        EdgeUnit eu;
        if (isShip) {
            eu = new EdgeUnit(edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, cp);
            buildEdgeUnit(cp, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, fromPeer);
        } else {
            eu = new EdgeUnit(edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.ROAD, cp);
            buildEdgeUnit(cp, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, fromPeer);
        }

        Village v = new Village(cp, cityPos);
        buildVillage(cityPos, VillageKind.SETTLEMENT, cp.getColor(), fromPeer);

        List<Hex> neighbourHexes = new ArrayList<>();
        List<Hex> hexes = aGameBoardManager.getHexes();

        for (Hex h : hexes) {
            if (h.isAdjacent(cityPos)) {
                neighbourHexes.add(h);
            }
        }

        ResourceMap cost = new ResourceMap();
        Integer curr;
        for (Hex h : neighbourHexes) {
            switch (h.getKind()) {
                case FOREST:
                    curr = cost.get(ResourceKind.WOOD);
                    cost.put(ResourceKind.WOOD, (curr == null ? 0 : curr) + 1);
                    break;
                case MOUNTAINS:
                    curr = cost.get(ResourceKind.ORE);
                    cost.put(ResourceKind.ORE, (curr == null ? 0 : curr) + 1);
                    break;
                case HILLS:
                    curr = cost.get(ResourceKind.BRICK);
                    cost.put(ResourceKind.BRICK, (curr == null ? 0 : curr) + 1);
                    break;
                case FIELDS:
                    curr = cost.get(ResourceKind.GRAIN);
                    cost.put(ResourceKind.GRAIN, (curr == null ? 0 : curr) + 1);
                    break;
                case PASTURE:
                    curr = cost.get(ResourceKind.WOOL);
                    cost.put(ResourceKind.WOOL, (curr == null ? 0 : curr) + 1);
                    break;
                // GOLDFIELDS ?
            }
        }
        cp.addResources(cost);
    }

    /**
     * Adds resources to the bank.
     *
     * @param cost The resources to be added to the bank
     */
    public void add(ResourceMap cost) {
        aSessionManager.addToBank(cost);
    }

    /**
     * Remove resources from the bank.
     *
     * @param cost The resources to be removed from the bank
     */
    public ResourceMap remove(ResourceMap cost) {
        cost = aSessionManager.checkMaxCostForBank(cost);
        aSessionManager.removeFromBank(cost);
        return cost;
    }
}
