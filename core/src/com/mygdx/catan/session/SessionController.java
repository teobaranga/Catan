package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.*;
import com.mygdx.catan.TradeAndTransaction.TradeManager;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.request.BuildEdge;
import com.mygdx.catan.request.BuildIntersection;
import com.mygdx.catan.request.EndTurn;
import com.mygdx.catan.request.MoveShip;
import com.mygdx.catan.request.RollTwoDice;
import com.mygdx.catan.response.DiceRolled;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.mygdx.catan.enums.GamePhase.*;
import static com.mygdx.catan.enums.ResourceKind.*;
import static com.mygdx.catan.enums.VillageKind.CITY;

public class SessionController {

    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private final TransactionManager aTransactionManager;
    private final TradeManager tradeManager;

    private final SessionScreen aSessionScreen;

    private final Listener aSessionListener;

    /** The random number generator for dice rolls */
    private final CatanRandom random;

    /** The local player's color */
    private PlayerColor aPlayerColor;

    /** The local player */
    private Player localPlayer;

    /** Flag indicating whether it's the turn of the player logged in */
    private boolean myTurn;

    SessionController(SessionScreen sessionScreen) {
        final Game currentGame = GameManager.getInstance().getCurrentGame();

        aGameBoardManager = GameBoardManager.getInstance();
        aSessionManager = SessionManager.getInstance(currentGame == null ? null : currentGame.session);
        aTransactionManager = TransactionManager.getInstance(aSessionManager);
        tradeManager = TradeManager.getInstance(aTransactionManager);

        aSessionScreen = sessionScreen;

        random = CatanRandom.getInstance();

        // sets the color as the accounts associated Player object color
        for (Player p : aSessionManager.getPlayers()) {
            if (p.getAccount().equals(CatanGame.account)) {
                localPlayer = p;
                aPlayerColor = p.getColor();
                break;
            }
        }

        aSessionListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof DiceRolled) {
                    Gdx.app.postRunnable(() -> {
                        // Inform the player of the dice roll
                        final DiceRolled diceRolled = (DiceRolled) object;
                        aSessionScreen.addGameMessage(diceRolled.getUsername() + " rolled a " + diceRolled.getDiceRoll());
                        aSessionScreen.showDice(diceRolled.getDiceRoll().getLeft(), diceRolled.getDiceRoll().getRight());

                        switch (aSessionManager.getCurrentPhase()) {
                            case SETUP_PHASE_ONE:
                                if (diceRolled.isLastRoll()) {
                                    // Update the session with the one received from the server
                                    aSessionManager.updateSession(diceRolled.getSession());
                                    aSessionScreen.addGameMessage("The player with the highest roll is " + aSessionManager.getCurrentPlayer().getUsername());
                                    // Move on to the next phase
                                    endPhase(SETUP_PHASE_ONE);
                                } else {
                                    endTurn();
                                }
                                break;
                            case TURN_FIRST_PHASE:
                                resourceProduction(diceRolled.getDiceRoll().getLeft() + diceRolled.getDiceRoll().getRight());
                                endPhase(TURN_FIRST_PHASE);
                                break;
                            default:
                                break;
                        }
                    });
                } else if (object instanceof BuildIntersection) {
                    Gdx.app.postRunnable(() -> {
                        final BuildIntersection intersectionBuilt = (BuildIntersection) object;
                        aSessionScreen.addGameMessage(intersectionBuilt.username + " built a " + intersectionBuilt.getKind().toString().toLowerCase());
                        Pair<Integer, Integer> positionCoordinates = intersectionBuilt.getPosition();
                        CoordinatePair position = aGameBoardManager.getCoordinatePairFromCoordinates(positionCoordinates.getLeft(), positionCoordinates.getRight());

                        switch (aSessionManager.getCurrentPhase()) {
                            case SETUP_PHASE_TWO_CLOCKWISE:
                            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                                buildVillage(position, intersectionBuilt.getKind(), intersectionBuilt.getOwner(), true, true);
                                break;
                            case TURN_SECOND_PHASE:
                                buildVillage(position, intersectionBuilt.getKind(), intersectionBuilt.getOwner(), true, false);
                                break;
                            default:
                                break;
                        }
                    });
                } else if (object instanceof BuildEdge) {
                    Gdx.app.postRunnable(() -> {
                        final BuildEdge edgeBuilt = (BuildEdge) object;
                        aSessionScreen.addGameMessage(edgeBuilt.username + " built a " + edgeBuilt.getKind().toString().toLowerCase());
                        Pair<Integer, Integer> firstCor = edgeBuilt.getLeftPosition();
                        Pair<Integer, Integer> secondCor = edgeBuilt.getRightPosition();

                        CoordinatePair firstPos = aGameBoardManager.getCoordinatePairFromCoordinates(firstCor.getLeft(), firstCor.getRight());
                        CoordinatePair secondPos = aGameBoardManager.getCoordinatePairFromCoordinates(secondCor.getLeft(), secondCor.getRight());

                        switch (aSessionManager.getCurrentPhase()) {
                            case SETUP_PHASE_TWO_CLOCKWISE:
                            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                                buildEdgeUnit(edgeBuilt.getOwner(), firstPos, secondPos, edgeBuilt.getKind(), true, true);
                                // In the setup phases, placing an edge/building ends the turn
                                endTurn();
                                break;
                            case TURN_SECOND_PHASE:
                                buildEdgeUnit(edgeBuilt.getOwner(), firstPos, secondPos, edgeBuilt.getKind(), true, false);
                                break;
                            default:
                                break;

                        }
                    });
                } else if (object instanceof MoveShip) {
                    Gdx.app.postRunnable(() -> {
                        final MoveShip shipMoved = (MoveShip) object;
                        aSessionScreen.addGameMessage(shipMoved.username + " moved a ship");
                        Pair<Integer, Integer> originfirstCor = shipMoved.getOriginleftPos();
                        Pair<Integer, Integer> originsecondCor = shipMoved.getOriginrightPos();
                        Pair<Integer, Integer> newfirstCor = shipMoved.getnewleftPos();
                        Pair<Integer, Integer> newsecondCor = shipMoved.getnewrightPos();
                        
                        CoordinatePair originfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(originfirstCor.getLeft(), originfirstCor.getRight());
                        CoordinatePair originsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(originsecondCor.getLeft(), originsecondCor.getRight());
                        CoordinatePair newfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(newfirstCor.getLeft(), newfirstCor.getRight());
                        CoordinatePair newsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(newsecondCor.getLeft(), newsecondCor.getRight());
                        
                        moveShip(originfirstPos, originsecondPos, newfirstPos, newsecondPos, shipMoved.getOwner(), true);
                    });
                } else if (object instanceof EndTurn) {
                    Gdx.app.postRunnable(() -> {
                        System.out.println(((EndTurn) object).username + " ended their turn");
                        endTurn();
                    });
                }
            }
        };
    }

    /** Update the myTurn variable */
    void checkIfMyTurn() {
        myTurn = aSessionManager.getCurrentPlayer().getAccount().equals(CatanGame.account);
        turn();
    }

    /** Process a turn */
    private void turn() {
        final Player currentPlayer = aSessionManager.getCurrentPlayer();
        aSessionScreen.updateCurrentPlayer(currentPlayer);
        if (!myTurn) {
            aSessionScreen.endTurn();
            return;
        }
        aSessionScreen.enablePhase(aSessionManager.getCurrentPhase());
        switch (aSessionManager.getCurrentPhase()) {
            case SETUP_PHASE_ONE:
                aSessionScreen.addGameMessage("Please roll the dice");
                break;
            case SETUP_PHASE_TWO_CLOCKWISE:
                aSessionScreen.addGameMessage("Please choose a position for your settlement,\n then choose a neighbouring edge for the position of your road");
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                aSessionScreen.addGameMessage("Please choose a position for your city,\n then choose a neighbouring edge for the position of your road");
                break;
            case TURN_FIRST_PHASE:
                aSessionScreen.addGameMessage("You may now roll the dice to generate the production");
                break;
            case TURN_SECOND_PHASE:
                aSessionScreen.addGameMessage("You may now place new buildings and roads or engage in maritime trade");
                break;
            case Completed:
                break;
        }
    }

    /** Notify all the other players that the current player ended his/her turn. */
    void endTurnNotify() {
        endTurn();
        CatanGame.client.sendTCP(EndTurn.newInstance());
    }

    /**
     * Ends the turn of the current player (may not be local player)
     */
    void endTurn() {
        aSessionManager.nextPlayer();
        switch (aSessionManager.getCurrentPhase()) {
            case SETUP_PHASE_TWO_CLOCKWISE:
                // End the clockwise phase if everyone placed their settlements
                if (aSessionManager.isRoundCompleted()) {
                    endPhase(SETUP_PHASE_TWO_CLOCKWISE);
                    // Return in order to avoid checking the turn twice
                    return;
                }
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                // End the counter-clockwise phase if everyone placed their cities
                if (aSessionManager.isRoundCompleted()) {
                    endPhase(SETUP_PHASE_TWO_COUNTERCLOCKWISE);
                    // Return in order to avoid checking the turn twice
                    return;
                }
                break;
            case TURN_SECOND_PHASE:
                endPhase(TURN_SECOND_PHASE);
                // Return in order to avoid checking the turn twice
                return;
        }
        checkIfMyTurn();
    }

    /**
     * End the specified phase
     */
    private void endPhase(GamePhase phase) {
        System.out.println(phase + " ended");
        switch (phase) {
            case SETUP_PHASE_ONE:
                aSessionManager.setCurrentPhase(SETUP_PHASE_TWO_CLOCKWISE);
                break;
            case SETUP_PHASE_TWO_CLOCKWISE:
                aSessionManager.setCurrentPhase(SETUP_PHASE_TWO_COUNTERCLOCKWISE);
                aSessionManager.setCounterClockwise();
                // Come back to the same player
                aSessionManager.nextPlayer();
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                aSessionManager.setCurrentPhase(TURN_FIRST_PHASE);
                aSessionManager.setClockwise();
                // Come back to the same player
                aSessionManager.nextPlayer();
                break;
            case TURN_FIRST_PHASE:
                aSessionManager.setCurrentPhase(TURN_SECOND_PHASE);
                break;
            case TURN_SECOND_PHASE:
                aSessionManager.setCurrentPhase(TURN_FIRST_PHASE);
                break;
        }
        checkIfMyTurn();
    }

    public PlayerColor getPlayerColor() {
        return aPlayerColor;
    }

    /*
    public void setPlayerColor(PlayerColor pc) {
        aPlayerColor = pc;
    }*/

    public Player getCurrentPlayer() {
        return aSessionManager.getCurrentPlayer();
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
        return (Math.abs(a.getLeft() - b.getLeft()) + Math.abs(a.getRight() - b.getRight()) == 2 && a.getRight() != b.getRight());
    }

    /**
     * @param intersection checks if this intersection is on land
     * @return true if on land
     */
    public boolean isOnLand(CoordinatePair intersection) {
        return aGameBoardManager.isOnLand(intersection);
    }

    /**
     * @param firstIntersection  first  intersection
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
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        boolean canBuild = false;

        if (kind == VillageKind.SETTLEMENT) {
            ResourceMap cost = GameRules.getGameRulesInstance().getSettlementCost();
            boolean hasAvailSettlements = currentP.getAvailableSettlements() > 0;
            if (currentP.hasEnoughResources(cost) && hasAvailSettlements) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }

        if (kind == CITY) {
            ResourceMap cost = GameRules.getGameRulesInstance().getCityCost();
            boolean hasAvailCities = currentP.getAvailableCities() > 0;
            if (currentP.hasEnoughResources(cost) && hasAvailCities) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        return canBuild;
    }

    /**
     * @param owner of request
     * @param kind  of edge unit requested to be built
     * @return true if owner has the resources to build the requested village kind
     */
    public boolean requestBuildEdgeUnit(PlayerColor owner, EdgeUnitKind kind) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        boolean hasAvailableShips = currentP.getAvailableShips() > 0;
        boolean hasAvailableRoads = currentP.getAvailableRoads() > 0;
        boolean canBuild = false;
        ResourceMap cost = null;
        if (kind == EdgeUnitKind.SHIP) {
             cost = GameRules.getGameRulesInstance().getShipCost();
            if (currentP.hasEnoughResources(cost) && hasAvailableShips) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        if (kind == EdgeUnitKind.ROAD) {
             cost = GameRules.getGameRulesInstance().getRoadCost();
            if (currentP.hasEnoughResources(cost) && hasAvailableRoads) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        return canBuild;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to road owned by player and (2) not adjacent to another village and (3) on land (3) unoccupied
     */
    public ArrayList<CoordinatePair> requestValidBuildIntersections(PlayerColor owner) {
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        List<EdgeUnit> listOfEdgeUnits = currentP.getRoadsAndShips();

        ArrayList<CoordinatePair> validIntersections = new ArrayList<>();
        for (CoordinatePair i : aGameBoardManager.getIntersectionsAndEdges()) {
            boolean isAdjacentToSomeBuilding = false;
            for (Village v : aGameBoardManager.getBuildingsInPlay()) {
                if (isAdjacent(i, v.getPosition())) {
                    isAdjacentToSomeBuilding = true;
                }
            }
            for (EdgeUnit e : listOfEdgeUnits) {
                if (!isAdjacentToSomeBuilding && (e.hasEndpoint(i)) && !i.isOccupied() && aGameBoardManager.isOnLand(i)) {
                    validIntersections.add(i);
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
            boolean isAdjacentToSomeBuilding = false;
            for (Village v : aGameBoardManager.getBuildingsInPlay()) {
                if (isAdjacent(i, v.getPosition())) {
                    isAdjacentToSomeBuilding = true;
                }
            }
            if (!i.isOccupied() && (!isAdjacentToSomeBuilding) && aGameBoardManager.isOnLand(i)) {
                validIntersections.add(i);
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
        List<Village> listOfVillages = currentP.getVillages();
        for (Village v : listOfVillages) {
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

        for (Village v : currentP.getVillages()) {
            if (!validRoadEndpoints.contains(v.getPosition())) {
                validRoadEndpoints.add(v.getPosition());
            }
        }
        for (EdgeUnit eu : currentP.getRoadsAndShips()) {
            if (!validRoadEndpoints.contains(eu.getAFirstCoordinate())) {
                validRoadEndpoints.add(eu.getAFirstCoordinate());
            }
            if (!validRoadEndpoints.contains(eu.getASecondCoordinate())) {
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

        for (Village v : currentP.getVillages()) {
            if (!validShipEndpoints.contains(v.getPosition()) && aGameBoardManager.isOnSea(v.getPosition())) {
                validShipEndpoints.add(v.getPosition());
            }
        }
        for (EdgeUnit eu : currentP.getRoadsAndShips()) {
            if (!validShipEndpoints.contains(eu.getAFirstCoordinate()) && aGameBoardManager.isOnSea(eu.getAFirstCoordinate())) {
                validShipEndpoints.add(eu.getAFirstCoordinate());
            }
            if (!validShipEndpoints.contains(eu.getASecondCoordinate()) && aGameBoardManager.isOnSea(eu.getASecondCoordinate())) {
                validShipEndpoints.add(eu.getASecondCoordinate());
            }
        }

        return validShipEndpoints;
        // same as above but with no edges that are in land can be chosen
    }
    
    /**
     * @param owner of requested valid intersections
     * @return a list of all ships that the owner may move
     * */
    public ArrayList<EdgeUnit> requestValidShips(PlayerColor owner) {
    	
    	Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
    	ArrayList<EdgeUnit> validShips = new ArrayList<EdgeUnit>();
    	
    	
    	for (EdgeUnit eu : currentP.getRoadsAndShips()) {
    		boolean isMoveable = (eu.getKind() == EdgeUnitKind.SHIP);
    		
    		// verifies that eu is not in between two other edge units
    		if (isMoveable) {
    			for (EdgeUnit firstAdjacentEu : currentP.getRoadsAndShips()) {
        			for (EdgeUnit secondAdjacentEu : currentP.getRoadsAndShips()) {
            			if (!firstAdjacentEu.equals(secondAdjacentEu) && (eu.isAdjacent(firstAdjacentEu) && eu.isAdjacent(secondAdjacentEu))) {
            				isMoveable = false;
            				/*
            				System.out.println("<"+eu.getAFirstCoordinate().getLeft()+","+eu.getAFirstCoordinate().getRight() +"> "+ "<"+eu.getASecondCoordinate().getLeft()+","+eu.getASecondCoordinate().getRight()+"> "+"is not moveable");
            				System.out.println("adjacent to <"+firstAdjacentEu.getAFirstCoordinate().getLeft()+","+firstAdjacentEu.getAFirstCoordinate().getRight() +"> "+ "<"+firstAdjacentEu.getASecondCoordinate().getLeft()+","+firstAdjacentEu.getASecondCoordinate().getRight()+"> ");
            				System.out.println("adjacent to <"+secondAdjacentEu.getAFirstCoordinate().getLeft()+","+secondAdjacentEu.getAFirstCoordinate().getRight() +"> "+ "<"+secondAdjacentEu.getASecondCoordinate().getLeft()+","+secondAdjacentEu.getASecondCoordinate().getRight()+"> ");
            			    */
            			}
            		}
        		}
    		}
    		
    		if (isMoveable) { validShips.add(eu); }
    	}
    	
    	return validShips;
    }

    /**
     * Requests the GameBoardManager to build village on given coordinate. SessionScreen is notified of any boardgame changes.
     *
     * @param position of new settlement
     * @param kind     of village to build
     * @param owner    of new settlement
     * @param fromPeer indicates whether the method was called from the owner of new settlement, or from a peer
     * @param init     indicated whether the method was called during initialization. If it was, player resource are not updated
     * @return true if building the village was successful, false otherwise
     */
    public boolean buildVillage(CoordinatePair position, VillageKind kind, PlayerColor owner, boolean fromPeer, boolean init) {
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);

        if (kind == VillageKind.SETTLEMENT) {
            aGameBoardManager.buildSettlement(currentP, position);

            if (fromPeer) {
                aSessionScreen.updateIntersection(position, owner, kind);
            } else {
                aSessionScreen.updateIntersection(position, owner, kind);
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getSettlementCost());
                    aSessionScreen.updateResourceBar(currentP.getResources());
                }

                // notify peers about board game change
                BuildIntersection request = BuildIntersection.newInstance(new ImmutablePair<>(position.getLeft(), position.getRight()), kind, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
            }
        } else if (kind == CITY) {
            aGameBoardManager.upgradeSettlement(currentP, position);

            if (fromPeer) {
                aSessionScreen.updateIntersection(position, owner, kind);
            } else {
                aSessionScreen.updateIntersection(position, owner, kind);
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getCityCost());
                    aSessionScreen.updateResourceBar(currentP.getResources());
                }

                // notify peers about board game change
                BuildIntersection request = BuildIntersection.newInstance(new ImmutablePair<>(position.getLeft(), position.getRight()), kind, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
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
     * @param secondPosition second end point of road or ship
     * @param kind           edge unit kind: ROAD or SHIP
     * @param fromPeer       indicates whether the method was called from the owner of new settlement, or from a peer
     * @param init           indicated whether the method was called during initialization. If it was, player resource are not updated
     * @return true if building the unit was successful, false otherwise
     */
    public boolean buildEdgeUnit(PlayerColor owner, CoordinatePair firstPosition, CoordinatePair secondPosition, EdgeUnitKind kind, boolean fromPeer, boolean init) {
        Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
        aGameBoardManager.buildEdgeUnit(currentP, firstPosition, secondPosition, kind);
        aSessionScreen.updateEdge(firstPosition, secondPosition, kind, owner);

        if (kind == EdgeUnitKind.ROAD) {
            if (!fromPeer) {
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getRoadCost());
                    aSessionScreen.updateResourceBar(currentP.getResources());
                }

                // notify peers about board game change
                BuildEdge request = BuildEdge.newInstance(new ImmutablePair<>(firstPosition.getLeft(), firstPosition.getRight()), new ImmutablePair<>(secondPosition.getLeft(), secondPosition.getRight()), kind, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
            }
        }

        if (kind == EdgeUnitKind.SHIP) {
            if (!fromPeer) {
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getShipCost());
                    aSessionScreen.updateResourceBar(currentP.getResources());
                }

                // notify peers about board game change
                BuildEdge request = BuildEdge.newInstance(new ImmutablePair<>(firstPosition.getLeft(), firstPosition.getRight()), new ImmutablePair<>(secondPosition.getLeft(), secondPosition.getRight()), kind, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
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
     * Finds the owner's ship at <firstOriginPos,secondOriginPos> and updates its coordinates to <newFirstPos,newSecondPos>, assets that EdgeUnit at origin positions is a ship
     * @param firstOriginPos  first coordinate of ship to move
     * @param secondOriginPos second coordinate of ship to move
     * @param newFirstPos     first coordinate of new ship position
     * @param newSecondPos    second coordinate of new ship position
     * @param owner           of ship
     * @fromPeer              indicates whether the method was called from the owner of new settlement, or from a peer
     * @return true if successful
     * */
    public boolean moveShip(CoordinatePair firstOriginPos, CoordinatePair secondOriginPos, CoordinatePair newFirstPos, CoordinatePair newSecondPos, PlayerColor owner, boolean fromPeer) {
    	Player currentP = aSessionManager.getCurrentPlayerFromColor(owner);
    	EdgeUnit shipToMove = null;
    	
    	for (EdgeUnit eu : currentP.getRoadsAndShips()) {
    		if (eu.hasEndpoint(firstOriginPos) && eu.hasEndpoint(secondOriginPos)) {
    			shipToMove = eu;
    		}
    	}
    	
    	if (shipToMove == null) {
    		return false;
    	} else {
    		// tell other peers if fromPeer is false
    		if (!fromPeer) {
    		    Pair<Integer,Integer> originleftPos = new ImmutablePair<>(firstOriginPos.getLeft(), firstOriginPos.getRight());
    		    Pair<Integer,Integer> originrightPos = new ImmutablePair<>(secondOriginPos.getLeft(), secondOriginPos.getRight());
    		    Pair<Integer,Integer> newleftPos = new ImmutablePair<>(newFirstPos.getLeft(), newFirstPos.getRight());
    		    Pair<Integer,Integer> newrightPos = new ImmutablePair<>(newSecondPos.getLeft(), newSecondPos.getRight());
    		    MoveShip request = MoveShip.newInstance(originleftPos, originrightPos, newleftPos, newrightPos, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
    		} else {
    		    // remove old position of ship from client GUI
                aSessionScreen.removeEdgeUnit(firstOriginPos.getLeft(), firstOriginPos.getRight(), secondOriginPos.getLeft(), secondOriginPos.getRight());
    		}
    		
    		shipToMove.moveShip(newFirstPos, newSecondPos);
            aSessionScreen.updateEdge(newFirstPos, newSecondPos, EdgeUnitKind.SHIP, owner);
    		
    		return true;
    	}
    }

    
    public void buildInitialVillageAndRoad(CoordinatePair villagePos, CoordinatePair firstEdgePos, CoordinatePair secondEdgePos, VillageKind kind, EdgeUnitKind edgeKind) {
    	 if (kind == VillageKind.CITY) {
             buildVillage(villagePos, VillageKind.SETTLEMENT, getPlayerColor(), false, true);
             buildVillage(villagePos, VillageKind.CITY, getPlayerColor(), false, true);
             distributeInitialResources(villagePos);
         } else {
             buildVillage(villagePos, kind, getPlayerColor(), false, true);
         }

         buildEdgeUnit(getPlayerColor(), firstEdgePos, secondEdgePos, edgeKind, false, true);
    }
    
    void distributeInitialResources(CoordinatePair cityPos) {
        List<Hex> neighbouringHexes = aGameBoardManager.getNeighbouringHexes(cityPos);
        Player clientPlayer = aSessionManager.getCurrentPlayerFromColor(aPlayerColor);
        ResourceMap playerResourceMap = new ResourceMap();

        int pastureCounter = 0;
        int forestCounter = 0;
        int mountainCounter = 0;
        int hillCounter = 0;
        int fieldCounter = 0;

        for (Hex h : neighbouringHexes) {
            TerrainKind tKind = h.getKind();
            switch (tKind) {
                case PASTURE:
                    pastureCounter++;
                    break;
                case FOREST:
                    forestCounter++;
                    break;
                case MOUNTAINS:
                    mountainCounter++;
                    break;
                case HILLS:
                    hillCounter++;
                    break;
                case FIELDS:
                    fieldCounter++;
                    break;
                case DESERT:
                    break;
                case GOLDFIELD:
                    break;
                case SEA:
                    break;
                default:
                    break;
            }
        }

        playerResourceMap.put(WOOL, pastureCounter);
        playerResourceMap.put(WOOD, forestCounter);
        playerResourceMap.put(ORE, mountainCounter);
        playerResourceMap.put(BRICK, hillCounter);
        playerResourceMap.put(GRAIN, fieldCounter);

        clientPlayer.addResources(playerResourceMap);
        aSessionScreen.updateResourceBar(clientPlayer.getResources());
    }

    /*
    /**
     * Allows the user to place a city and an edge unit and then receive the resources near the city
     *
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
    */

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

    /**
     * Get a pair of integers representing the roll of the red and yellow dice.
     *
     * @return a pair of integers, first one being the red die, second one being the yellow die
     */
    private Pair<Integer, Integer> rollTwoDice() {
        return random.rollTwoDice();
    }

    /**
     * Determine the resources generated for the current player from the dice roll.
     *
     * @param diceRoll sum of the red and yellow dice
     */
    private void resourceProduction(int diceRoll) {
        // Get the hexes having a dice number equal to the dice roll
        List<Hex> producingHexes = aGameBoardManager.getProducingHexes(diceRoll);

        // Create the map containing the resources and commodities that the player
        // will receive as a result of this dice roll
        ResourceMap resAndComMap = new ResourceMap();

        // For each producing hex...
        for (Hex ph : producingHexes) {
            // Get the building surrounding it
            List<Village> adjacentVillages = aGameBoardManager.getAdjacentVillages(ph);

            // For each building...
            for (Village v : adjacentVillages) {
                // Skip the building if it doesn't belong to the local player
                if (!v.getOwner().equals(localPlayer))
                    continue;

                VillageKind vKind = v.getVillageKind();

                switch (ph.getKind()) {
                    case PASTURE:
                        resAndComMap.add(WOOL, 1);
                        if (vKind == CITY)
                            resAndComMap.add(CLOTH, 1);
                        break;
                    case FOREST:
                        resAndComMap.add(WOOD, 1);
                        if (vKind == CITY)
                            resAndComMap.add(PAPER, 1);
                        break;
                    case MOUNTAINS:
                        resAndComMap.add(ORE, 1);
                        if (vKind == CITY)
                            resAndComMap.add(COIN, 1);
                        break;
                    case HILLS:
                        resAndComMap.add(BRICK, 1);
                        if (vKind == CITY)
                            resAndComMap.add(BRICK, 1);
                        break;
                    case FIELDS:
                        resAndComMap.add(GRAIN, 1);
                        if (vKind == CITY)
                            resAndComMap.add(GRAIN, 1);
                        break;
                    case GOLDFIELD:
                        break;
                    default:
                        break;
                }
            }
        }

        localPlayer.addResources(resAndComMap);

        aSessionScreen.updateResourceBar(localPlayer.getResources());
    }

    /*public ResourceMap getOwnresourcesUpdate(Map<Player,ResourceMap> updatedPlayerResources) {
        for (Map.Entry<Player, ResourceMap> entry : updatedPlayerResources.entrySet() ) {
            if(entry.getKey().getColor().equals(aPlayerColor)) {
                return entry.getValue();
            }
        }
        return new ResourceMap();
    }*/

    /**
     * Roll the dice according to the phase of the game/session.
     */
    void rollDice() {
        switch (aSessionManager.getCurrentPhase()) {
            case TURN_FIRST_PHASE:
                // We're at the phase where we the player rolls the dice and everyone gets appropriate resources
            case SETUP_PHASE_ONE:
                // We're at the phase where we have to determine who rolled the highest number
                // Roll the dice
                Pair<Integer, Integer> diceResults = rollTwoDice();
                // Inform the server / other users of the roll
                RollTwoDice request = RollTwoDice.newInstance(diceResults, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
                break;
            default:
                // FIXME This is not good, pretend it doesn't exist (-Teo)
                //Pair<Integer, Integer> roll = rollTwoDice();
//                Map<Player, ResourceMap> resourceUpdateMap = getResourceUpdate(roll.getLeft() + roll.getRight());
//                RollDice diceResourcesToSent = RollDice.newInstance(resourceUpdateMap, "Dummy");
                //aSessionScreen.showDice(roll.getLeft(), roll.getRight());
//                aSessionScreen.updateResourceBar(getOwnresourcesUpdate(resourceUpdateMap));
                break;
        }
    }

    /**
     * Perform maritime trade
     *
     * @param offer      type of resource that the player is offering
     * @param request    type of resource that the player is requesting
     * @param tradeRatio the number of units of the offered resource necessary to receive one requested resource
     */
    void maritimeTrade(ResourceKind offer, ResourceKind request, int tradeRatio) {
        tradeManager.maritimeTrade(offer, request, tradeRatio, localPlayer);
        aSessionScreen.updateResourceBar(localPlayer.getResources());
    }

    /**
     * Get the trade ratios used when doing maritime trade.
     * The values of the map represent how many units of that resource are necessary
     * in order to get any other resource in exchange. If the value is zero, the
     * player does not have enough resources to perform trade for that resource.
     */
    ResourceMap getTradeRatios() {
        final ResourceMap tradeRatios = new ResourceMap();
        for (ResourceKind resourceKind : ResourceKind.values()) {
            // Get the min number of resources of this type that the player
            // needs to give in order to receive any other resource
            int ratio = localPlayer.getHighestHarbourLevel(resourceKind);
            if (localPlayer.hasEnoughOfResource(resourceKind, ratio)) {
                tradeRatios.put(resourceKind, ratio);
            } else {
                tradeRatios.put(resourceKind, 0);
            }
        }
        return tradeRatios;
    }

    ResourceMap getMaxOffer() {
        return localPlayer.getResources();
    }

    /** Call this when the screen is shown */
    void onScreenShown() {
        CatanGame.client.addListener(aSessionListener);
    }

    /** Call this when the screen is hidden */
    void onScreenHidden() {
        CatanGame.client.removeListener(aSessionListener);
    }
}
