package com.mygdx.catan;

import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.session.SessionManager;
import com.mygdx.catan.session.SessionScreen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by amandaivey on 3/14/17.
 */
public class ProgressCardHandler {

    private final SessionManager aSessionManager;

    private SessionController aSessionController;
    //private SessionScreen aSessionScreen;

    public ProgressCardHandler(SessionController sessionController) {
        aSessionController = sessionController;
        final Game currentGame = GameManager.getInstance().getCurrentGame();
        aSessionManager = SessionManager.getInstance(currentGame == null ? null : currentGame.session);
        // aSessionScreen = aSessionController.getSessionScreen();
        
    }

    public void handle (ProgressCardType pType, PlayerColor currentPColor) {
        final Player currentP = aSessionController.getCurrentPlayer();
        //aSessionManager.incrementProgressCardMap(pType);
        //SessionManager.getInstance().incrementProgressCardMap(pType);
        aSessionManager.setCurrentlyExecutingProgressCard(pType);
        
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
            case SMITH:
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
                break;
            case COMMERCIALHARBOUR:
                break;
            case MASTERMERCHANT:
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

}
