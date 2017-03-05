package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CatanRandom;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.request.RollDice;
import com.mygdx.catan.request.RollTwoDice;
import com.mygdx.catan.response.PlaceCityAndRoad;
import com.mygdx.catan.response.ShowDice;
import com.mygdx.catan.response.UpdateResourceBar;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class SessionController {
    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private final SessionScreen aSessionScreen;
    private PlayerColor aPlayerColor;
    private final Listener aSessionListener;
    private final CatanRandom random;

    /** Flag indicating whether it's the turn of the player logged in */
    private boolean myTurn;

    SessionController(SessionScreen sessionScreen) {
        aSessionScreen = sessionScreen;
        aGameBoardManager = GameBoardManager.getInstance();
        aSessionManager = SessionManager.getInstance();
        random = CatanRandom.getInstance();
        
        // sets the color as the accounts associated Player object color
        for (Player p : aSessionManager.getPlayers()) {
        	if (p.getAccount().equals(CatanGame.account)) {
        		aPlayerColor = p.getColor();
        		break;
        	}
        }

        aSessionListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PlaceCityAndRoad) {
                    Gdx.app.postRunnable(() -> {
                        boolean isFirstInit = ((PlaceCityAndRoad) object).isFirstInit;
                        boolean isShip = false;
                        aSessionScreen.initialize(isFirstInit);

                        // FIXME: the following information will not be in the GUI at this time.
                        // CoordinatePair cityPos = aSessionScreen.getInitSettlementIntersection();
                        // CoordinatePair edgeUnitPos1 = aSessionScreen.getInitEdgePos1();
                        // CoordinatePair edgeUnitPos2 = aSessionScreen.getInitEdgePos2();

                        VillageKind villageKind = VillageKind.CITY;
                        if (((PlaceCityAndRoad) object).isFirstInit) {
                            villageKind = VillageKind.SETTLEMENT;
                        }
                        boolean fromPeer = ((PlaceCityAndRoad) object).fromPeer;
                        PlayerColor aPlayerColor = ((PlaceCityAndRoad) object).aPlayerColor;
                        //placeCityAndRoads(cityPos, edgeUnitPos1, edgeUnitPos2, isShip, fromPeer, aPlayerColor, villageKind);
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
                } else if (object instanceof RollTwoDice) {
                    Gdx.app.postRunnable(() -> {
                        Pair<Integer, Integer> diceRollResult = ((RollTwoDice) object).getRollResult();
                        int yellowDice = diceRollResult.getLeft();
                        int redDice = diceRollResult.getRight();
                        aSessionManager.setYellowDice(yellowDice);
                        aSessionManager.setRedDice(redDice);
                        aSessionScreen.showDice(yellowDice, redDice);
                    });
                } else if (object instanceof RollDice) {
                    Gdx.app.postRunnable(() -> {
                        Map<Player, ResourceMap> updatedPlayerResources = ((RollDice) object).getResourceUpdates();
                        aSessionScreen.updateResourceBar(getOwnresourcesUpdate(updatedPlayerResources));
                     });

                }
                // TODO update the myTurn variable here
            }
        };
        myTurn = aSessionManager.getCurrentPlayer().getAccount().equals(CatanGame.account);
    }

    /** Call this when the screen is shown */
    void onScreenShown() {
        CatanGame.client.addListener(aSessionListener);
    }

    /** Call this when the screen is hidden */
    void onScreenHidden() {
        CatanGame.client.removeListener(aSessionListener);
    }

    /** Process a turn */
    void turn() {
        if (!myTurn) {
            // TODO disable UI, the player can't really do anything at this moment
            return;
        }
        switch (aSessionManager.getCurrentPhase()) {
            case SETUP_PHASE_ONE:
                // TODO Allow the player to roll the dice
                break;
            case SETUP_PHASE_TWO_CLOCKWISE:
                // TODO Allow the player to place one settlement and one road
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                // TODO Allow the player to place on city and one road. The player also receives some resources here
                break;
            case TURNFIRSTPHASE:
                break;
            case TURNSECONDPHASE:
                break;
            case Completed:
                break;
        }
    }

    public PlayerColor getPlayerColor() {
        return aPlayerColor;
    }

    /*
    public void setPlayerColor(PlayerColor pc) {
        aPlayerColor = pc;
    }*/

    public int getYellowDice() {
        return aSessionManager.getYellowDice();
    }

    public int getRedDice() {
        return aSessionManager.getRedDice();
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
     */
    public boolean isAdjacent(CoordinatePair a, CoordinatePair b) {
        return (Math.abs(a.getLeft() - b.getLeft()) + Math.abs(a.getRight() - b.getRight()) == 2) && a.getRight() != b.getRight();
    }

    /**
     * @param intersection checks if this intersection is on land
     * @return true if on land
     */
    public boolean isOnLand(CoordinatePair intersection) {
        return aGameBoardManager.isOnLand(intersection);
    }

    /**
     * @param firstIntersection first  intersection
     * @param secondIntersection second intersection
     * @return true if the edge between the two intersections is on land (assumes the two intersections are adjacent)
     */
    public boolean isOnLand(CoordinatePair firstIntersection, CoordinatePair secondIntersection) {
        return aGameBoardManager.isOnLand(firstIntersection, secondIntersection);
    }


    /**
     * @param owner of request
     * @param kind  of village requested to be built
     * @return true if owner has the resources to build the requested village kind
     */
    public boolean requestBuildVillage(PlayerColor owner, VillageKind kind) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        Player currentP = null;
        ResourceMap cost = null;
        for (Player p : aSessionManager.getPlayers()) {
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        if (kind.equals(VillageKind.SETTLEMENT)) {
            cost = GameRules.getGameRulesInstance().getSettlementCost();
        } else if (kind.equals(VillageKind.CITY)) {
            cost = GameRules.getGameRulesInstance().getCityCost();
        }
        //check to make sure player has sufficient resources
        if (currentP.hasEnoughResources(cost)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param owner of request
     * @param kind  of edge unit requested to be built
     * @return true if owner has the resources to build the requested village kind
     */
    public boolean requestBuildEdgeUnit(PlayerColor owner, EdgeUnitKind kind) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        Player currentP = null;
        ResourceMap cost = null;
        for (Player p : aSessionManager.getPlayers()) {
            if (p.getColor().equals(owner)) {
                currentP = p;
            }
        }
        if (kind.equals(EdgeUnitKind.SHIP)) {
            cost = GameRules.getGameRulesInstance().getShipCost();
        } else if (kind.equals(EdgeUnitKind.ROAD)) {
            cost = GameRules.getGameRulesInstance().getRoadCost();
        }
        //check to make sure player has sufficient resources
        if (currentP.hasEnoughResources(cost)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to road owned by player and (2) not adjacent to another village and (3) on land (3) unoccupied
     */
    public ArrayList<CoordinatePair> requestValidBuildIntersections(PlayerColor owner) {
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        ArrayList<Village> buildingsInPlay = aGameBoardManager.getBuildingsInPlay();
        ArrayList<EdgeUnit> listOfEdgeUnits = currentP.getRoadsAndShips();

        ArrayList<CoordinatePair> validIntersections = new ArrayList<>();
        for (CoordinatePair i : aGameBoardManager.getIntersectionsAndEdges()) {
            for (Village v: buildingsInPlay) {
                for (EdgeUnit e : listOfEdgeUnits) {
                    if (!isAdjacent(i, v.getPosition()) && (e.hasEndpoint(i)) && !i.isOccupied() && aGameBoardManager.isOnLand(i)) {
                        validIntersections.add(i);
                    }
                }
            }
        }
        return validIntersections;
    }

    /**
     * Method to be called at initialization
     *
     * @return a list of all the intersections that are (1) unoccupied and (2) not adjacent to another occupied intersection (3) is on land
     */
    public ArrayList<CoordinatePair> requestValidInitializationBuildIntersections() {
        ArrayList<CoordinatePair> validIntersections = new ArrayList<>();

        for (CoordinatePair i : aGameBoardManager.getIntersectionsAndEdges()) {
            for (CoordinatePair j : aGameBoardManager.getIntersectionsAndEdges()) {
                if (!i.isOccupied() && (!j.isOccupied() || !isAdjacent(i, j)) && aGameBoardManager.isOnLand(i)) {
                    validIntersections.add(i);
                }
            }
        }

        return validIntersections;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that have an owner's settlement on it
     */
    public ArrayList<CoordinatePair> requestValidCityUpgradeIntersections(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        ArrayList<CoordinatePair> validUpgradeIntersections = new ArrayList<>();
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        ArrayList<Village> listOfVillages = currentP.getVillages();
        for (Village v: listOfVillages) {
            validUpgradeIntersections.add(v.getPosition());
        }
        return validUpgradeIntersections;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a road or village owned by owner
     */
    public HashSet<CoordinatePair> requestValidRoadEndpoints(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        HashSet<CoordinatePair> validRoadEndpoints = new HashSet<>();

        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);

        for (Village v: currentP.getVillages()) {
            if (!validRoadEndpoints.contains(v.getPosition())) {
                validRoadEndpoints.add(v.getPosition());
            }
        }
        for (EdgeUnit eu: currentP.getRoadsAndShips()) {
            if(!validRoadEndpoints.contains(eu.getAFirstCoordinate())) {
                validRoadEndpoints.add(eu.getAFirstCoordinate());
            }
            if(!validRoadEndpoints.contains(eu.getASecondCoordinate())) {
                validRoadEndpoints.add(eu.getASecondCoordinate());
            }
        }
        // this method will essentially return all the endpoints where you can build a road at any edge 
        // starting at that endpoint (if we disregard the edges that are occupied). The GUI will make sure 
        // none of the edges that are occupied or in water can be chosen.
        return validRoadEndpoints;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a ship or harbour village owned by owner (2) something something pirate
     */
    //TODO: implement pirate thing
    public HashSet<CoordinatePair> requestValidShipEndpoints(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        HashSet<CoordinatePair> validShipEndpoints = new HashSet<>();

        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);

        for (Village v: currentP.getVillages()) {
            if (!validShipEndpoints.contains(v.getPosition()) && aGameBoardManager.isOnSea(v.getPosition())) {
                validShipEndpoints.add(v.getPosition());
            }
        }
        for (EdgeUnit eu: currentP.getRoadsAndShips()) {
            if(!validShipEndpoints.contains(eu.getAFirstCoordinate()) && aGameBoardManager.isOnSea(eu.getAFirstCoordinate())) {
                validShipEndpoints.add(eu.getAFirstCoordinate());
            }
            if(!validShipEndpoints.contains(eu.getASecondCoordinate()) && aGameBoardManager.isOnSea(eu.getASecondCoordinate())) {
                validShipEndpoints.add(eu.getASecondCoordinate());
            }
        }

        return validShipEndpoints;
        // same as above but with no edges that are in land can be chosen
    }

    /**
     * Requests the GameBoardManager to build village on given coordinate. SessionScreen is notified of any boardgame changes.
     *
     * @param position of new settlement
     * @param kind     of village to build
     * @param owner    of new settlement
     * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if building the village was successful, false otherwise
     */
    public boolean buildVillage(CoordinatePair position, VillageKind kind, PlayerColor owner, boolean fromPeer) {
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);

        if (kind.equals(VillageKind.SETTLEMENT)) {
            aGameBoardManager.buildSettlement(currentP, position);
            aSessionManager.getTransactionManager().payPlayerToBank(currentP, GameRules.getGameRulesInstance().getSettlementCost());

            if (fromPeer) {
                aSessionScreen.updateIntersection(position, owner, kind);
            } else {
                aSessionScreen.updateIntersection(position, owner, kind);
                aSessionScreen.updateResourceBar(GameRules.getGameRulesInstance().getSettlementCost());
            }
        }
        else if (kind.equals(VillageKind.CITY)) {
            aGameBoardManager.upgradeSettlement(currentP, position);
            aSessionManager.getTransactionManager().payBankToPlayer(currentP, GameRules.getGameRulesInstance().getCityCost());

            if (fromPeer) {
                aSessionScreen.updateIntersection(position, owner, kind);
            } else {
                aSessionScreen.updateIntersection(position, owner, kind);
                aSessionScreen.updateResourceBar(GameRules.getGameRulesInstance().getCityCost());
            }
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
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        aGameBoardManager.buildEdgeUnit(currentP, firstPosition, SecondPosition, kind);
        aSessionScreen.updateEdge(firstPosition, SecondPosition, kind, owner);

        if (kind.equals(EdgeUnitKind.ROAD)) {
            aSessionManager.getTransactionManager().payBankToPlayer(currentP, GameRules.getGameRulesInstance().getRoadCost());
            if (!fromPeer) {
                aSessionScreen.updateResourceBar(GameRules.getGameRulesInstance().getRoadCost());
            }
        }

        if (kind.equals(EdgeUnitKind.SHIP)) {
            aSessionManager.getTransactionManager().payBankToPlayer(currentP, GameRules.getGameRulesInstance().getShipCost());
            if(!fromPeer) {
                aSessionScreen.updateResourceBar(GameRules.getGameRulesInstance().getShipCost());
            }
        }
        //TODO: longest road (fun fact: longest disjoint path problem is NP-hard)
        return true;
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
    public void placeCityAndRoads(CoordinatePair cityPos, CoordinatePair edgeUnitPos1, CoordinatePair edgeUnitPos2, boolean isShip, boolean fromPeer, PlayerColor aPlayerColor, VillageKind villageKind) {
        if (isShip) {
            buildEdgeUnit(aPlayerColor, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, fromPeer);
        } else {
            buildEdgeUnit(aPlayerColor, edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.ROAD, fromPeer);
        }

        buildVillage(cityPos, villageKind, aPlayerColor, fromPeer);

        if (fromPeer) {
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

    public Pair<Integer, Integer> rollTwoDice() {
        return random.rollTwoDice();
    }
    
    /*public Map<Player, ResourceMap> distributeResources(Player aPlayer) {
        
        switch(tKind) {
        
        }
    }*/

    public Map<Player, ResourceMap> getResourceUpdate(int diceRoll) {
        List<Hex> hexes = aGameBoardManager.getProducingHexes(diceRoll);
        Player currPlayer = aSessionManager.getCurrentPlayer();
        HashMap<Player, ResourceMap> playerResources = new HashMap<>();
        List<Hex> producingHexes = new ArrayList<>();
        List<Player> noResourcesReceivedPlayers = Arrays.asList(getPlayers());
        for (Hex h : hexes) {
            int hexNumber = h.getDiceNumber();
            Hex robberPosition = getRobberPosition();
            if (hexNumber == diceRoll && h != robberPosition) {
                producingHexes.add(h);
            }
        }

        for (Hex ph : producingHexes) {
            TerrainKind tKind = ph.getKind();
            ArrayList<Village> adjacentVillages = aGameBoardManager.getAdjacentVillages(ph);
            ArrayList<CoordinatePair> adjacentIntersections = aGameBoardManager
                    .getAdjacentIntersections(ph);
            for (CoordinatePair cp : adjacentIntersections) {
                boolean isOccupied = cp.isOccupied();
                if (isOccupied) {
                    Village v = cp.getOccupyingVillage();
                    adjacentVillages.add(v);
                }
            }
            for (Village v : adjacentVillages) {
                Player villageOwner = v.getOwner();
                VillageKind vKind = v.getVillageKind();
                ResourceMap ResAndComMap = new ResourceMap();

                switch (tKind) {
                case PASTURE:
                    ResAndComMap.put(ResourceKind.WOOL, 1);
                    if (vKind == VillageKind.CITY)
                        ResAndComMap.put(ResourceKind.CLOTH, 1);
                    break;
                case FOREST:
                    ResAndComMap.put(ResourceKind.WOOD, 1);
                    if (vKind == VillageKind.CITY)
                        ResAndComMap.put(ResourceKind.PAPER, 1);
                    break;
                case MOUNTAINS:
                    ResAndComMap.put(ResourceKind.ORE, 1);
                    if (vKind == VillageKind.CITY)
                        ResAndComMap.put(ResourceKind.COIN, 1);
                    break;
                case HILLS:
                    if (vKind == VillageKind.SETTLEMENT)
                        ResAndComMap.put(ResourceKind.BRICK, 1);
                    else
                        ResAndComMap.put(ResourceKind.BRICK, 2);
                    break;
                case FIELDS:
                    if (vKind == VillageKind.SETTLEMENT)
                        ResAndComMap.put(ResourceKind.GRAIN, 1);
                    else
                        ResAndComMap.put(ResourceKind.GRAIN, 2);
                    break;
                /*
                 * case DESERT: break; case GOLDFIELD: break; case SEA: break;
                 * default: break;
                 */
                }
                villageOwner.addResources(ResAndComMap);
                noResourcesReceivedPlayers.remove(villageOwner);
                playerResources.put(villageOwner, ResAndComMap);
            }
        }

        return playerResources;
    }
    // TODO: set GUI's mode to chooseActionMode for the player who's turn it is

    public ResourceMap getOwnresourcesUpdate(Map<Player, ResourceMap> updatedPlayerResources) {
        for (Map.Entry<Player, ResourceMap> entry : updatedPlayerResources.entrySet() ) {
            if(entry.getKey().getColor().equals(aPlayerColor)) {
                return entry.getValue();
            }
        }
        return new ResourceMap();
    }

    public void rollDices() {
        Pair<Integer, Integer> diceResults = rollTwoDice();
        //TODO: FIRE FOLLOWING MSG TO SERVER.
        RollTwoDice diceResultsToSent = RollTwoDice.newInstance(diceResults,"Dummy");
        Map<Player, ResourceMap> resourceUpdateMap = getResourceUpdate(diceResults.getLeft() + diceResults.getRight());
        //TODO: FIRE FOLLOWING MSG TO SERVER.
        RollDice diceResourcesToSent = RollDice.newInstance(resourceUpdateMap,"Dummy");
        aSessionScreen.showDice(diceResults.getLeft(), diceResults.getRight());
        aSessionScreen.updateResourceBar(getOwnresourcesUpdate(resourceUpdateMap));
    }
}
