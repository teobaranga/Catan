package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.response.PlaceCityAndRoad;
import com.mygdx.catan.response.ShowDice;
import com.mygdx.catan.response.UpdateResourceBar;

import java.util.ArrayList;
import java.util.List;

public class SessionController {
    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private SessionScreen aSessionScreen;
    private PlayerColor aPlayerColor;
    private final Listener aSessionListener;

    public PlayerColor getPlayerColor() {
        return aPlayerColor;
    }
    
    public void setPlayerColor(PlayerColor pc) {
        aPlayerColor = pc;
    }

    public SessionController(GameBoardManager gbm, SessionManager sm) {
        aGameBoardManager = gbm;
        aSessionManager = sm;
        aSessionListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PlaceCityAndRoad) {
                    Gdx.app.postRunnable(() -> {
                        // ENTER CHOOSEINTERSECTIONMODE AND RETURN cityPos;
                        CoordinatePair cityPos = CoordinatePair.of(0, 0, HarbourKind.NONE);
                        // ENTER CHOOSEEDGEMODE AND RETURN edgeUnitPos1 edgeUnitPos2;
                        CoordinatePair edgeUnitPos1 = CoordinatePair.of(0, 0, HarbourKind.NONE);
                        CoordinatePair edgeUnitPos2 = CoordinatePair.of(0, 0, HarbourKind.NONE);
                        // LET USER CHOOSE BTW SHIP AND ROAD (or force him to build a road ?)
                        boolean isShip = false;
                        boolean fromPeer = ((PlaceCityAndRoad) object).fromPeer;
                        PlayerColor aPlayerColor = ((PlaceCityAndRoad) object).aPlayerColor;
                        placeCityAndRoads(cityPos, edgeUnitPos1, edgeUnitPos2, isShip, fromPeer, aPlayerColor);
                    });
                } else if (object instanceof ShowDice) {
                    Gdx.app.postRunnable(() -> {
                        aSessionScreen.showDice(getYellowDice(), getRedDice());
                    });
                } else if (object instanceof UpdateResourceBar) {
                    Gdx.app.postRunnable(() -> {
                        ResourceMap cost = ((UpdateResourceBar) object).cost;
                        aSessionScreen.updateResourceBar(cost);
                    });
                }
            }
        };
        CatanGame.client.addListener(aSessionListener);
    }

    public int getYellowDice() {
        return aSessionManager.getYellowDice();
    }

    public int getRedDice() {
        return aSessionManager.getRedDice();
    }

    public void setSessionScreen(SessionScreen s) {
        aSessionScreen = s;
    }

    public ArrayList<Hex> getHexes() {
        return aGameBoardManager.getHexes();
    }

    public ArrayList<EdgeUnit> getRoadsAndShips() {
        return aGameBoardManager.getRoadsAndShips();
    }

    public ArrayList<CoordinatePair> getIntersectionsAndEdges() {
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
    public boolean isAdjacent (CoordinatePair a, CoordinatePair b) {
        return (Math.abs(a.getLeft() - b.getLeft()) + Math.abs(a.getRight() - b.getRight()) == 2) && a.getRight() != b.getRight();
    }
    
    /**
     * @param intersection checks if this intersection is on land
     * @return true if on land
     * */
    public boolean isOnLand(CoordinatePair intersection) {
        return aGameBoardManager.isOnLand(intersection);
    }
    
    /**
     * @param first intersection
     * @param second intersection
     * @return true if the edge between the two intersections is on land (assumes the two intersections are adjacent)
     * */
    public boolean isOnLand(CoordinatePair firstIntersection, CoordinatePair secondIntersection) {
        return aGameBoardManager.isOnLand(firstIntersection, secondIntersection);
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
     * @return a list of all the intersections that are (1) connected to road owned by player and (2) not adjacent to another village and (3) on land (3) unoccupied
     * */
    public ArrayList<CoordinatePair> requestValidBuildIntersections(PlayerColor owner) {
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

        ArrayList<CoordinatePair> validIntersections = new ArrayList<>();
        for (CoordinatePair i: aGameBoardManager.getIntersectionsAndEdges()) {
            //FIXME: only verifies that i is not adjacent to a village owned by current player! we need it to verify this for all the villages
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
     * Method to be called at initialization
     * 
     * @return a list of all the intersections that are (1) unoccupied and (2) not adjacent to another occupied intersection
     * */
    public ArrayList<CoordinatePair> requestValidInitializationBuildIntersections() {
        ArrayList<CoordinatePair> validIntersections = new ArrayList<CoordinatePair>();
        
        for (CoordinatePair i : aGameBoardManager.getIntersectionsAndEdges()) {
            for (CoordinatePair j : aGameBoardManager.getIntersectionsAndEdges()) {
                if (!i.isOccupied() && (!j.isOccupied() || !isAdjacent(i,j))) {
                    validIntersections.add(i);
                }
            }
        }
        
        return validIntersections;
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that have an owner's settlement on it
     * */
    public ArrayList<CoordinatePair> requestValidCityUpgradeIntersections(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        
        return null;
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a road or village owned by owner
     * */
    public ArrayList<CoordinatePair> requestValidRoadEndpoints(PlayerColor owner) {
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
    public ArrayList<CoordinatePair> requestValidShipEndpoints(PlayerColor owner) {
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
    public boolean buildVillage(CoordinatePair position, VillageKind kind, PlayerColor owner, boolean fromPeer) {
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
            aSessionScreen.updateResourceBar(GameRules.getGameRulesInstance().getSettlementCost());
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
     * @param owner          owner of edgeUnit
     * @param firstPosition  first end point of road or ship
     * @param SecondPosition second end point of road or ship
     * @param kind           edge unit kind: ROAD or SHIP
     * @param fromPeer       indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if building the unit was successful, false otherwise
     */
    public boolean buildEdgeUnit(PlayerColor owner, CoordinatePair firstPosition, CoordinatePair SecondPosition, EdgeUnitKind kind, boolean fromPeer) {
        
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
    public void placeCityAndRoads(CoordinatePair cityPos, CoordinatePair edgeUnitPos1, CoordinatePair edgeUnitPos2, boolean isShip, boolean fromPeer, PlayerColor aPlayerColor) {
        if (isShip) {
            buildEdgeUnit(aPlayerColor, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, fromPeer);
        } else {
            buildEdgeUnit(aPlayerColor, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.ROAD, fromPeer);
        }

        buildVillage(cityPos, VillageKind.SETTLEMENT, aPlayerColor, fromPeer);

        if(fromPeer){
            return;
        }

        List<Hex> neighbourHexes = aGameBoardManager.getNeighbouringHexes(cityPos);

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
            default:
                break;
            }
        }
       // cp.addResources(cost); getPLayerByColor(aPlayerColor).addResources(cost);
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
