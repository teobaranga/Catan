package com.mygdx.catan;

import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.BuildIntersection;
import com.mygdx.catan.request.GiveResources;
import com.mygdx.catan.request.SwitchHexDiceNumbers;
import com.mygdx.catan.request.TakeResources;
import com.mygdx.catan.request.TargetedChooseResourceCardRequest;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.session.SessionManager;

import org.apache.commons.lang3.tuple.ImmutablePair;
import com.mygdx.catan.session.SessionScreen;
import com.mygdx.catan.gameboard.Knight;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amandaivey on 3/14/17.
 */
public class ProgressCardHandler {

    private final SessionManager aSessionManager;
    private final GameBoardManager aGameBoardManager;

    private SessionController aSessionController;
    //private SessionScreen aSessionScreen;

    public ProgressCardHandler(SessionController sessionController) {
        aSessionController = sessionController;
        final Game currentGame = GameManager.getInstance().getCurrentGame();
        aSessionManager = SessionManager.getInstance(currentGame == null ? null : currentGame.session);
        aGameBoardManager = GameBoardManager.getInstance();
        // aSessionScreen = aSessionController.getSessionScreen();
        
    }

    public void handle (ProgressCardType pType, PlayerColor currentPColor) {
        final Player currentP = aSessionController.getCurrentPlayer();
        //aSessionManager.incrementProgressCardMap(pType);
        //SessionManager.getInstance().incrementProgressCardMap(pType);
        aSessionManager.setCurrentlyExecutingProgressCard(pType);
        ArrayList<Player> playersWithMoreVP = new ArrayList<>();
        
        switch(pType) {
            case ALCHEMIST:
            case CRANE:

                /** adds a city wall to selected city for free*/
            case ENGINEER:
                ArrayList<CoordinatePair> validCityWallIntersections = new ArrayList<>();
                List<Village> listOfVillages = currentP.getVillages();
                for (Village v : listOfVillages) {
                    if (v.getVillageKind() == VillageKind.CITY && !v.hasCityWalls()) {
                        validCityWallIntersections.add(v.getPosition());
                    }
                }
                MultiStepMove playEngineer = new MultiStepMove();
                aSessionController.getSessionScreen().initChooseIntersectionMove(validCityWallIntersections, playEngineer);
                playEngineer.<CoordinatePair>addMove(myCityWallCoordinates -> {
                    aSessionController.buildCityWall(currentPColor, myCityWallCoordinates, true);
                    //revert back to choose action mode and enable buttons
                    aSessionController.getSessionScreen().interractionDone();
                });
                break;
            case INVENTOR:
                ArrayList<Hex> validHexes = new ArrayList<>();
                // adds all hexes without 2, 6, 8 or 12 to validHexes
                for (Hex h : aGameBoardManager.getHexes()) {
                    if (h.getDiceNumber() != 2 && h.getDiceNumber() != 6 && h.getDiceNumber() != 8 && h.getDiceNumber() != 12 && h.getDiceNumber() != 0) {
                        validHexes.add(h);
                    }
                }

                MultiStepMove playInventor = new MultiStepMove();

                playInventor.<Hex>addMove(chosenFirstHex -> {
                    final Hex firstHex = chosenFirstHex;
                    validHexes.remove(chosenFirstHex);
                    aSessionController.getSessionScreen().initChooseHexMove(validHexes, playInventor);

                    playInventor.<Hex>addMove(chosenSecondHex -> {
                        int firstDiceNumber = firstHex.getDiceNumber();
                        firstHex.setDiceNumber(chosenSecondHex.getDiceNumber());
                        chosenSecondHex.setDiceNumber(firstDiceNumber);
                        aSessionController.getSessionScreen().interractionDone();

                        // sends message to peers about hex number token change
                        Pair<Integer,Integer> firstHexPos = new ImmutablePair<>(firstHex.getLeftCoordinate(), firstHex.getRightCoordinate());
                        Pair<Integer,Integer> secondHexPos = new ImmutablePair<>(chosenSecondHex.getLeftCoordinate(), chosenSecondHex.getRightCoordinate());

                        SwitchHexDiceNumbers request = SwitchHexDiceNumbers.newInstance(firstHexPos, secondHexPos, CatanGame.account.getUsername());
                        CatanGame.client.sendTCP(request);
                    });

                });

                aSessionController.getSessionScreen().initChooseHexMove(validHexes, playInventor);

                break;
            case IRRIGATION:
                int numGrainCards = 0;
                for(Village v: currentP.getVillages()) {
                    if (GameBoardManager.getInstance().isAdjacentToCertainHex(TerrainKind.FIELDS, v.getPosition())) {
                        numGrainCards+=2;
                    }
                }
                ResourceMap newGrains = new ResourceMap();
                newGrains.add(ResourceKind.GRAIN, numGrainCards);
                currentP.addResources(newGrains);
                break;
            //allows player to upgrade a settlement to a city for 2 ore and 1 grain
            case MEDICINE:
                ArrayList<CoordinatePair> validUpgradeIntersections = new ArrayList<>();
                final List<Village> listOfSettlements = currentP.getVillages();
                for (Village v : listOfSettlements) {
                    if(v.getVillageKind() == VillageKind.SETTLEMENT) {
                        validUpgradeIntersections.add(v.getPosition());
                    }
                }
                MultiStepMove playMedicine = new MultiStepMove();
                aSessionController.getSessionScreen().initChooseIntersectionMove(validUpgradeIntersections, playMedicine);
                playMedicine.<CoordinatePair>addMove(myCityCoordinates -> {
                    aSessionController.buildCityWall(currentPColor, myCityCoordinates, true);
                    //revert back to choose action mode and enable buttons
                    aSessionController.getSessionScreen().interractionDone();
                });
                break;
            case MINING:
                int numOreCards = 0;
                for(Village v: currentP.getVillages()) {
                    if (GameBoardManager.getInstance().isAdjacentToCertainHex(TerrainKind.MOUNTAINS, v.getPosition())) {
                        numOreCards+=2;
                    }
                }
                ResourceMap newOre = new ResourceMap();
                newOre.add(ResourceKind.GRAIN, numOreCards);
                currentP.addResources(newOre);
                break;
            case PRINTER:
                aSessionManager.incrementTokenVP(currentP);
                break;
            case ROADBUILDING:
                ArrayList<Pair<CoordinatePair, CoordinatePair>> validEdges = new ArrayList<>();
                
            	// loops through valid road end points and adds valid edges (both ships and roads)
                for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
                    for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) { 
                        if (aSessionController.isAdjacent(i, j)) {

                            Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
                            validEdges.add(edge);

                            for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                                if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                                    validEdges.remove(edge);
                                }
                            }
                        }
                    } 
                }
                
                // create multistepmove that will handle building both edges
                MultiStepMove move = new MultiStepMove();
                
                // add the moves
                move.<Pair<CoordinatePair,CoordinatePair>>addMove(chosenEdge -> {
                    EdgeUnitKind kind = EdgeUnitKind.ROAD;
                    if (!aSessionController.isOnLand(chosenEdge.getLeft(), chosenEdge.getRight())) {
                        kind = EdgeUnitKind.SHIP;
                    }
                    aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), chosenEdge.getLeft(), chosenEdge.getRight(), kind, false, false);
                    
                    // re initializes validEdges 
                    validEdges.clear();
                    updateValidEdges(validEdges, chosenEdge);
                    
                    // prompts the player to choose the second edge with updated valid edges
                    aSessionController.getSessionScreen().initChooseEdgeMove(validEdges, move);
                });
                
                move.<Pair<CoordinatePair,CoordinatePair>>addMove(chosenEdge -> {
                    EdgeUnitKind kind = EdgeUnitKind.ROAD;
                    if (!aSessionController.isOnLand(chosenEdge.getLeft(), chosenEdge.getRight())) {
                        kind = EdgeUnitKind.SHIP;
                    }
                    aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), chosenEdge.getLeft(), chosenEdge.getRight(), kind, false, false);
                    
                    // ends the move
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                aSessionController.getSessionScreen().initChooseEdgeMove(validEdges, move);
                
                break;
            //you may promote 2 knights 1 level each for free
            //todo: you may only promote a "strong" knight if you have the fortress city improvement
            case SMITH:
                ArrayList<CoordinatePair> validKnights = new ArrayList<>();
                List<Knight> listOfKnights = currentP.getKnights();
                for (Knight k : listOfKnights) {
                    if (k.getStrength() != 3) {
                        validKnights.add(k.getPosition());
                    }
                }
                MultiStepMove playSmith = new MultiStepMove();
                aSessionController.getSessionScreen().initChooseIntersectionMove(validKnights, playSmith);
                playSmith.<CoordinatePair>addMove(myKnightCoordinates -> {
                    for (Knight k: listOfKnights) {
                        if (k.getPosition().equals(myKnightCoordinates)) {
                            k.upgrade();
                        }
                    }
                    validKnights.clear();
                    updateValidKnights(validKnights, myKnightCoordinates);

                });
                playSmith.<CoordinatePair>addMove(mySecondKnightCoordinates -> {
                    for (Knight k: listOfKnights) {
                        if (k.getPosition().equals(mySecondKnightCoordinates)) {
                            k.upgrade();
                        }
                    }
                });

                //revert back to choose action mode and enable buttons
                aSessionController.getSessionScreen().interractionDone();
                break;
            case BISHOP:
                break;
            case CONSTITUTION:
                aSessionManager.incrementTokenVP(currentP);
                break;
            case DESERTER:
                break;
            case DIPLOMAT:
                break;
            case INTRIGUE:
                break;
            case SABOTEUR:
                break;
            case SPY:
                break;
            case WARLORD:
                break;
            case WEDDING:
                
                // adds all the players with more VP points than current player
                updatePlayersWithMoreVP(playersWithMoreVP, currentP);
                
                // sends a targeted request to each player with more VP
                for (Player p : playersWithMoreVP) {
                    TargetedChooseResourceCardRequest request = TargetedChooseResourceCardRequest.newInstance(2, CatanGame.account.getUsername(), p.getUsername());
                    CatanGame.client.sendTCP(request);
                }
                
                break;
            case COMMERCIALHARBOUR:
                break;
            case MASTERMERCHANT:
                // adds all the players with more VP points than current player
                updatePlayersWithMoreVP(playersWithMoreVP, currentP);
                
                // creates multistepmove that prompts client to choose a player whose hand they will be able to look through
                MultiStepMove takeResources = new MultiStepMove();
                                
                takeResources.<Player>addMove(player -> {
                    final Player chosenPlayer = player;
                    
                    int numberOfResources = 2;
                    if (player.getResourceHandSize() < numberOfResources) { numberOfResources = player.getResourceHandSize(); }
                    
                    takeResources.<ResourceMap>addMove(map -> {
                        currentP.addResources(map);
                        aSessionController.getSessionScreen().updateResourceBar(currentP.getResources());
                        TakeResources request = TakeResources.newInstance(map, currentP.getUsername(), chosenPlayer.getUsername());
                        CatanGame.client.sendTCP(request);
                        
                        aSessionController.getSessionScreen().interractionDone();
                    });
                    
                    aSessionController.getSessionScreen().chooseMultipleResource(player.getResources(), numberOfResources, takeResources);
                });
                
                aSessionController.getSessionScreen().chooseOtherPlayer(playersWithMoreVP, takeResources);
                
                break;
            case MERCHANTFLEET:
                break;
            case MERCHANT:
                break;
            case RESOURCEMONOPOLY:
                break;
            case TRADEMONOPOLY:
                break;
        }
    }

    
    private void updateValidEdges(List<Pair<CoordinatePair,CoordinatePair>> validEdges, Pair<CoordinatePair,CoordinatePair> chosenEdge) {
     // loops through valid road end points and adds valid edges (both ships and roads)
        for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) { 
                if (aSessionController.isAdjacent(i, j)) {

                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
                    validEdges.add(edge);

                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                            validEdges.remove(edge);
                        }
                    }
                }
            } 
        }
    }
    
    private void updatePlayersWithMoreVP(List<Player> playersWithMoreVPlist, Player currentPlayer) {
        playersWithMoreVPlist.clear();
        for (Player p : aSessionManager.getPlayers()) {
            if (!p.equals(currentPlayer) /*&& aSessionController.currentVP(p) > aSessionController.currentVP(currentPlayer)*/) {
                playersWithMoreVPlist.add(p);
            }
        }
    }

    private void updateValidKnights(ArrayList<CoordinatePair> validKnights, CoordinatePair chosenKnight) {
        for(CoordinatePair i : validKnights) {
            if (i.equals(chosenKnight)) {
                validKnights.remove(i);
            }
        }

    }

}
