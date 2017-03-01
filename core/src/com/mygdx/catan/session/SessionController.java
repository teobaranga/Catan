package com.mygdx.catan.session;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;

public class SessionController {
    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private SessionScreen aSessionScreen;

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
     * Requests the GameBoardManager to build settlement on given coordinate. If fromPeer is false, the SessionController verifies that the position is valid, else it simply places the settlement. SessionScreen is notified of any boardgame changes.
     *
     * @param position of new settlement
     * @param owner    of new settlement
     * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if building the settlement was successful, false otherwise
     */
    public boolean buildSettlement(CoordinatePair<Integer, Integer> position, Player owner, boolean fromPeer) {
        // verifies that the intersection is not occupied, and that it is adjacent to a road or ship of player
        // TODO: it does not verify that the intersection is not in the sea, or that it is not adjacent to another Village
        // if (position.isOccupied() || !aGameBoard.isAdjacentToEdgeUnit(position, player)) {return false;}
        return false;
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
        //TODO: verify that edgeUnit is between two adjacent coordinates, that those coordinates are free
        // and that the adjacent hexes are compatible with the EdgeUnitKind (in SessionController)

        //TODO: longest road

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
        buildSettlement(cityPos, cp, fromPeer);

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
