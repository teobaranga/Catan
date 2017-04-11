package com.mygdx.catan.session;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CatanRandom;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.*;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

public class ProgressCardHandler {

    private final SessionManager aSessionManager;
    private final GameBoardManager aGameBoardManager;

    private SessionController aSessionController;
    //private SessionScreen aSessionScreen;

    public ProgressCardHandler(SessionController sessionController) {
        aSessionController = sessionController;
        final Game currentGame = GameManager.getInstance().getCurrentGame();
        aSessionManager = SessionManager.getInstance(currentGame.session);
        aGameBoardManager = GameBoardManager.getInstance(currentGame.gameboard);
        // aSessionScreen = aSessionController.getSessionScreen();
    }

    public void handle (ProgressCardType pType, PlayerColor currentPColor) {
        final Player currentP = aSessionController.getCurrentPlayer();
        //aSessionManager.incrementProgressCardMap(pType);
        //SessionManager.getInstance().incrementProgressCardMap(pType);
        aSessionManager.setCurrentlyExecutingProgressCard(pType);
        currentP.removeProgressCard(pType);
        //TODO: call finishCurrentlyExecutingProgressCard() every time interractionDone is called at the end of a multistepaction (note network stuff!), and at the end of  handling a card without any interactions
        ArrayList<Player> playersWithMoreVP = new ArrayList<>();

        switch(pType) {
            case ALCHEMIST:
                // choose the results of both production dice. then roll the event die as normal and resolve event
                
                CatanRandom random = CatanRandom.getInstance();
                EventKind eventDieResult = random.rollEventDie();
                 
                MultiStepMove chooseDieResults = new MultiStepMove();
                chooseDieResults.<DiceRollPair>addMove(diceResults -> {
                    aSessionController.getSessionScreen().addGameMessage("you chose: " + diceResults.getRed() + ", " + diceResults.getYellow());
                    
                    // handle roll
                    aSessionController.handleRoll(diceResults, eventDieResult);

                    aSessionController.getSessionScreen().addGameMessage(String.format("Rolled a %s", eventDieResult));

                    // Create the message that informs the other users of the dice roll
                    RollDice request = RollDice.newInstance(diceResults, eventDieResult, CatanGame.account.getUsername());
                    CatanGame.client.sendTCP(request);
                    
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                aSessionController.getSessionScreen().chooseDiceNumbers(chooseDieResults);
                
               
                
                break;
            case CRANE:
                // you can build a city improvement (abbey, town hall, etc.) for 1
                // commodity less than normal
                
                // Make a list of kinds the player may improve with new reduced cost
                ArrayList<ProgressCardKind> improvableCities = new ArrayList<>();
                if (aSessionController.requestTradeImprovement(currentP.getColor())) { improvableCities.add(ProgressCardKind.TRADE); }
                if (aSessionController.requestScienceImprovement(currentP.getColor())) { improvableCities.add(ProgressCardKind.SCIENCE); }
                if (aSessionController.requestPoliticsImprovement(currentP.getColor())) { improvableCities.add(ProgressCardKind.POLITICS); }
                
                // MulstiStepMove that will upgrade chosen improvement with reduced cost
                MultiStepMove buildImprovement = new MultiStepMove();
                
                buildImprovement.<ProgressCardKind>addMove((improvementType) -> {
                    switch(improvementType) {
                    case POLITICS:
                        aSessionController.politicsCityImprovement(currentP.getColor());
                        break;
                    case SCIENCE:
                        aSessionController.scienceCityImprovement(currentP.getColor());
                        break;
                    case TRADE:
                        aSessionController.tradeCityImprovement(currentP.getColor());
                        break;
                    default:
                        break;
                    }
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                if (!improvableCities.isEmpty()) {
                    aSessionController.getSessionScreen().chooseProgressCardKind(buildImprovement, improvableCities);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("Even with the cheaper cost, you do not have enough commodities to build any city improvement. That was a dumb move!");
                }
                
                break;
            case ENGINEER:
                // Adds a city wall to selected city for free
                ArrayList<CoordinatePair> validCityWallIntersections = new ArrayList<>();
                List<Village> listOfVillages = currentP.getVillages();
                for (Village v : listOfVillages) {
                    if (v.getVillageKind() == VillageKind.CITY && !v.hasCityWalls()) {
                        validCityWallIntersections.add(v.getPosition());
                    }
                }
                MultiStepMove playEngineer = new MultiStepMove();

                playEngineer.<CoordinatePair>addMove(myCityWallCoordinates -> {
                    aSessionController.buildCityWall(currentPColor, myCityWallCoordinates, true);
                    //revert back to choose action mode and enable buttons
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                if (!validCityWallIntersections.isEmpty()) {
                    aSessionController.getSessionScreen().initChooseIntersectionMove(validCityWallIntersections, playEngineer);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("You have no cities to choose from, that was a dumb move");
                }
                
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
                    
                    aSessionController.getSessionScreen().initChooseHexMove(validHexes, playInventor);

                });

                aSessionController.getSessionScreen().initChooseHexMove(validHexes, playInventor);

                break;
            case IRRIGATION:
                int numGrainCards = 0;
                for(Village v: currentP.getVillages()) {
                    if (aGameBoardManager.isAdjacentToCertainHex(TerrainKind.FIELDS, v.getPosition())) {
                        numGrainCards+=2;
                    }
                }
                ResourceMap newGrains = new ResourceMap();
                newGrains.add(ResourceKind.GRAIN, numGrainCards);
                currentP.addResources(newGrains);
                aSessionManager.finishCurrentlyExecutingProgressCard();
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
                playMedicine.<CoordinatePair>addMove(myCityCoordinates -> {
                    aSessionController.buildCityWall(currentPColor, myCityCoordinates, true);
                    //revert back to choose action mode and enable buttons
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                if (!validUpgradeIntersections.isEmpty()) {
                    aSessionController.getSessionScreen().initChooseIntersectionMove(validUpgradeIntersections, playMedicine);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("You have nothing to choose from, that was a dumb move");
                }
                

                break;
            case MINING:
                int numOreCards = 0;
                for(Village v: currentP.getVillages()) {
                    if (aGameBoardManager.isAdjacentToCertainHex(TerrainKind.MOUNTAINS, v.getPosition())) {
                        numOreCards+=2;
                    }
                }
                ResourceMap newOre = new ResourceMap();
                newOre.add(ResourceKind.GRAIN, numOreCards);
                currentP.addResources(newOre);
                aSessionManager.finishCurrentlyExecutingProgressCard();
                break;
            case PRINTER:
                aSessionManager.incrementTokenVP(currentP);
                aSessionManager.finishCurrentlyExecutingProgressCard();
                break;
            case ROADBUILDING:
                //TODO: COULD have a cancelling option for the second road. not necessary
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
                    updateValidEdges(validEdges);
                    
                    // prompts the player to choose the second edge with updated valid edges
                    if (!validEdges.isEmpty()) {
                        aSessionController.getSessionScreen().initChooseEdgeMove(validEdges, move);
                    } else {
                        aSessionController.getSessionScreen().addGameMessage("You do not have an available edge for the second piece");
                        aSessionController.getSessionScreen().interractionDone();
                    }
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
                
                if (!validEdges.isEmpty()) {
                    aSessionController.getSessionScreen().initChooseEdgeMove(validEdges, move);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("You have no available edges to build a road, that was a dumb move");
                }
                
                break;
            //you may promote 2 knights 1 level each for free
            //TODO: you may only promote a "strong" knight if you have the fortress city improvement
            case SMITH:
                ArrayList<CoordinatePair> validKnights = new ArrayList<>();
                List<Knight> listOfKnights = currentP.getKnights();
                for (Knight k : listOfKnights) {
                    if (k.getStrength() != 3) {
                        validKnights.add(k.getPosition());
                    }
                }
                MultiStepMove playSmith = new MultiStepMove();
                playSmith.<CoordinatePair>addMove(myKnightCoordinates -> {
                    for (Knight k: listOfKnights) {
                        if (k.getPosition().equals(myKnightCoordinates)) {
                            k.promote();
                        }
                    }
                    validKnights.clear();
                    updateValidKnights(validKnights, myKnightCoordinates);
                    
                    if (!validKnights.isEmpty()) {
                        aSessionController.getSessionScreen().initChooseIntersectionMove(validKnights, playSmith);
                    } else {
                        aSessionController.getSessionScreen().addGameMessage("You have no more knights to promote");
                        aSessionController.getSessionScreen().interractionDone();
                    }
                });
                playSmith.<CoordinatePair>addMove(mySecondKnightCoordinates -> {
                    for (Knight k: listOfKnights) {
                        if (k.getPosition().equals(mySecondKnightCoordinates)) {
                            k.promote();
                        }
                    }
                    aSessionController.getSessionScreen().interractionDone();
                });

                if (!validKnights.isEmpty()) {
                    aSessionController.getSessionScreen().initChooseIntersectionMove(validKnights, playSmith);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("You have no knights to promote. That was a dumb move");
                }
                
                //revert back to choose action mode and enable buttons
                break;
            case BISHOP:
                // Move the robber, following the normal rules. Draw 1 random resource/commodity card from each player who has a settlement or
                // city next to the robber's new hex
                
                if (!aSessionManager.getSession().firstBarbarianAttack) { 
                    aSessionManager.finishCurrentlyExecutingProgressCard();
                    break;
                }
                
                List<Hex> validRobberHexes = new ArrayList<>();
                List<Hex> boardHexes = aGameBoardManager.getHexes();
                for (Hex h : boardHexes) {
                    if (h.getKind() != TerrainKind.SEA && h.getKind() != TerrainKind.BIG_FISHERY && h.getKind() != TerrainKind.SMALL_FISHERY) {
                        validRobberHexes.add(h);
                    }
                }

                MultiStepMove moveRobber = new MultiStepMove();
                
                moveRobber.<Hex>addMove(hex -> {
                    // moves robber to chosen hex
                    aSessionController.moveRobber(hex, false);
                    
                    // initializes list of victims
                    ArrayList<Player> victims = new ArrayList<>();
                    List<Village> adjacentVillages = aGameBoardManager.getAdjacentVillages(hex);
                    for(Village v : adjacentVillages) {
                        if (!currentP.equals(v.getOwner()) && !victims.contains(v.getOwner())) {
                            victims.add(v.getOwner());
                        }
                    }
                    
                    // for each victim, pick a random card from their hand
                    for(Player victim : victims) {
                      //get hand
                        ResourceMap playerHand = victim.getResources();
                        int playerHandSize = victim.getResourceHandSize();

                        int randomCardIndex = new Random().nextInt(playerHandSize)+1;

                        //choose a random card to steal from victim's hand
                        ResourceKind cardToTake;
                        ResourceMap cardToSteal = new ResourceMap();
                        for(ResourceKind kind : ResourceKind.values()) {
                            int numCards = playerHand.get(kind);
                            
                            if (numCards > 0) {
                                randomCardIndex -= numCards;
                                
                                if (randomCardIndex <= 0) {
                                    cardToTake = kind;
                                    cardToSteal.put(cardToTake, 1);
                                    
                                    //update local player's resources
                                    currentP.addResources(cardToSteal);
                                    aSessionController.getSessionScreen().updateResourceBar(currentP.getResources());
                                    
                                    //update opponent hand
                                    TakeResources request = TakeResources.newInstance(cardToSteal, currentP.getUsername(), victim.getUsername());
                                    CatanGame.client.sendTCP(request);
                                   
                                    break;
                                }
                            }
                        }
                    }
                    
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                aSessionController.getSessionScreen().initChooseHexMove(validRobberHexes, moveRobber);
                
                
                break;
            case CONSTITUTION:
                aSessionManager.incrementTokenVP(currentP);
                aSessionManager.finishCurrentlyExecutingProgressCard();
                break;

            //TODO: need to test
            case DESERTER:
                List<CoordinatePair> ValidKnightIntersections = aSessionController.requestValidKnightIntersections(currentP.getColor());
                ArrayList<Player> myOpponents = new ArrayList<>();
                for (Player p : aSessionController.getPlayers()) {
                    if (!p.equals(currentP)) { myOpponents.add(p); }
                }

                //First choose player
                MultiStepMove playDeserter = new MultiStepMove();
                playDeserter.<Player>addMove((Player chosenPlayer) -> {
                    ArrayList opponentKnights = new ArrayList();
                    for (Knight k: chosenPlayer.getKnights()) {
                        opponentKnights.add(k);
                    }

                    playDeserter.<Knight>addMove((Knight knightToRemove) -> {
                        aGameBoardManager.removeKnight(knightToRemove);
                        ArrayList<CoordinatePair> myValidKnights = new ArrayList<>();
                        for(Knight k: aSessionController.requestValidKnights(currentP.getColor())){
                            if(k.getStrength() > knightToRemove.getStrength()) {
                                myValidKnights.add(k.getPosition());
                            }
                        }
                    });

                    aSessionController.getSessionScreen().initChooseIntersectionMove(opponentKnights, playDeserter);
                });

                aSessionController.getSessionScreen().chooseOtherPlayer(myOpponents, playDeserter);

                MultiStepMove placeNewKnight = new MultiStepMove();
                placeNewKnight.<CoordinatePair>addMove((CoordinatePair newKnightCoords) -> {
                    ArrayList myValidKnights = new ArrayList();
                    for (CoordinatePair intersection: aSessionController.requestValidKnightIntersections(currentP.getColor())){

                    }
                });

                break;
            case DIPLOMAT:
                // you may remove an "open" road (without another road or another piece at one end)
                // if you remove your own road, you may immediately place it somewhere else on the board for free
                // TODO : you may choose not to place the road immediately after in which case it is returned to your inventory
                
                // initialize list of valid edges to choose from
                ArrayList<Pair<CoordinatePair, CoordinatePair>> openRoads = new ArrayList<>();
                int firstEndAdj, secondEndAdj;
                for (EdgeUnit edge : aGameBoardManager.getRoadsAndShips()) {
                    firstEndAdj = 0; 
                    secondEndAdj = 0;
                    
                    // count the number of adjacent roads to the first and second endpoint
                    for (EdgeUnit other : aGameBoardManager.getRoadsAndShips()) {
                        if (!edge.equals(other) && other.hasEndpoint(edge.getAFirstCoordinate())) {
                            firstEndAdj++;
                        }
                        if (!edge.equals(other) && other.hasEndpoint(edge.getASecondCoordinate())) {
                            secondEndAdj ++;
                        }
                    }
                    
                    // count the number of adjacent villages to the first and second endpoint
                    for (Village village : aGameBoardManager.getVillages()) {
                        if (edge.getAFirstCoordinate().equals(village.getPosition())) {
                            firstEndAdj++;
                        }
                        if (edge.getASecondCoordinate().equals(village.getPosition())) {
                            secondEndAdj++;
                        }
                    }
                    
                    // if either one of the endpoints has an empty adjacent count, the road is open
                    if ((secondEndAdj == 0 || firstEndAdj == 0) && edge.getKind() == EdgeUnitKind.ROAD) {
                        openRoads.add(new ImmutablePair<>(edge.getAFirstCoordinate(), edge.getASecondCoordinate()));
                    }
                }
                
                // multistep move that will either let the player choose new position of owned road, or simply displace opponent road
                MultiStepMove displaceRoad = new MultiStepMove();
                
                displaceRoad.<Pair<CoordinatePair,CoordinatePair>>addMove(chosenEdgeCor -> {
                    // find the EdgeUnit piece that corresponds to the chosen edge
                    EdgeUnit chosenEdge = aGameBoardManager.getEdgeUnitFromCoordinatePairs(chosenEdgeCor.getLeft(), chosenEdgeCor.getRight());
                    
                    // if player owns chosenEdge, they will be given the possibility to immediately move it
                    if (chosenEdge.getOwner().equals(currentP)) {
                        // initialize valid move positions
                        ArrayList<Pair<CoordinatePair,CoordinatePair>> validMoveRoadPos = new ArrayList<>();
                        updateValidMoveRoad(validMoveRoadPos, chosenEdgeCor);
                        
                        // add move to displaceRoad that moves chosenEdge to chosenEdgePos and inform the network
                        displaceRoad.<Pair<CoordinatePair,CoordinatePair>>addMove(moveToPos -> {
                            
                            CoordinatePair originfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(chosenEdgeCor.getLeft().getLeft(), chosenEdgeCor.getLeft().getRight());
                            CoordinatePair originsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(chosenEdgeCor.getRight().getLeft(), chosenEdgeCor.getRight().getRight());
                            CoordinatePair newfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(moveToPos.getLeft().getLeft(), moveToPos.getLeft().getRight());
                            CoordinatePair newsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(moveToPos.getRight().getLeft(), moveToPos.getRight().getRight());
                            
                            aSessionController.moveEdge(originfirstPos, originsecondPos, newfirstPos, newsecondPos, currentP.getColor(), EdgeUnitKind.ROAD, false);
                            
                            aSessionController.getSessionScreen().interractionDone();
                        });
                        
                        aSessionController.getSessionScreen().removeEdgeUnit(chosenEdgeCor.getLeft().getLeft(), chosenEdgeCor.getLeft().getRight(), chosenEdgeCor.getRight().getLeft(), chosenEdgeCor.getRight().getRight());
                        aSessionController.getSessionScreen().initChooseEdgeMove(validMoveRoadPos, displaceRoad);
                        
                    } else { // if player does not own edge, it is simply displaced unto opponent inventory
                        ImmutablePair<Integer,Integer> firstCor = new ImmutablePair<>(chosenEdgeCor.getLeft().getLeft(), chosenEdgeCor.getLeft().getRight());
                        ImmutablePair<Integer,Integer> secondCor = new ImmutablePair<>(chosenEdgeCor.getRight().getLeft(), chosenEdgeCor.getRight().getRight());
                        DisplaceRoadRequest request = DisplaceRoadRequest.newInstance(firstCor, secondCor, currentP.getUsername());
                        CatanGame.client.sendTCP(request);
                        aSessionController.getSessionScreen().interractionDone();
                    }
                });
                
                if (!openRoads.isEmpty()) {
                    aSessionController.getSessionScreen().initChooseEdgeMove(openRoads, displaceRoad);
                } else {
                    aSessionController.getSessionScreen().addGameMessage("There are no open roads to choose from. Read the cards before you play them!");
                }
                
                break;
            //TODO: need to test
            case INTRIGUE:
                //generate valid intersections for displaced knights
                ArrayList<CoordinatePair> validKnightsForDisplace = new ArrayList<>();
                for (EdgeUnit roadOrShip : currentP.getRoadsAndShips()) {
                    //if the first coordinate of an edge usnit has a knight and that knight is not the current players knight add for list of displacement
                    if(roadOrShip.getAFirstCoordinate().hasKnight() && !roadOrShip.getAFirstCoordinate().getOccupyingKnight().getOwner().equals(currentP)) {
                        validKnightsForDisplace.add(roadOrShip.getAFirstCoordinate());
                    }
                    if(roadOrShip.getASecondCoordinate().hasKnight() && !roadOrShip.getASecondCoordinate().getOccupyingKnight().getOwner().equals(currentP)) {
                        validKnightsForDisplace.add(roadOrShip.getASecondCoordinate());
                    }
                }
                //generate valid intersections for the knight to go to
                ArrayList<CoordinatePair> validPositionsForDisplacedKnight = new ArrayList<>();
                for(CoordinatePair pair: aGameBoardManager.getEmptyCoordinates()) {
                    validPositionsForDisplacedKnight.add(pair);
                }

                //generate valid build intersections for current P to build a knight
                List<CoordinatePair> validPositionsForBasicKnight = aSessionController.requestValidKnightIntersections(currentPColor);


                MultiStepMove displaceKnight = new MultiStepMove();
                displaceKnight.<CoordinatePair>addMove(chosenKnightCoord -> {
                    Knight chosenKnight = chosenKnightCoord.getOccupyingKnight();


                    if(validPositionsForDisplacedKnight.size() == 0){
                        aSessionController.getKnightController().removeKnight(chosenKnightCoord);
                        //remove knight from owners knights
                        chosenKnight.getOwner().getKnights().remove(chosenKnight);
                        //update knight build intersections
                        updateValidKnights(validPositionsForBasicKnight, chosenKnightCoord);

                        //add move for build basic knight
                        displaceKnight.<CoordinatePair>addMove(buildKnightCoord -> {
                            aSessionController.getKnightController().buildKnight(buildKnightCoord, currentPColor);
                            KnightRequest knightRequest = KnightRequest.build(currentP.getUsername(), currentPColor, buildKnightCoord);
                            CatanGame.client.sendTCP(knightRequest);

                        });

                        //call a session controller init intersectinos for build basic knight
                        aSessionController.getSessionScreen().initChooseIntersectionMove(validPositionsForBasicKnight, displaceKnight);

                    }
                    else {
                        displaceKnight.<CoordinatePair>addMove(newPosForKnight -> {

                            CoordinatePair originalPos = chosenKnightCoord;
                            CoordinatePair newPosition = newPosForKnight;
                            aSessionController.getKnightController().displaceKnight(chosenKnightCoord, newPosForKnight);

                            //if there are no valid positions for the knight remove the knight

                            //otherwise allow player to pick place where they would like knight to go

                            DisplaceKnightRequest request = DisplaceKnightRequest.newInstance(newPosition, currentP.getUsername());
                            CatanGame.client.sendTCP(request);
                            //call a session controller init intersectinos for build basic knight
                        });

                        //add move for build basic knight
                        displaceKnight.<CoordinatePair>addMove(buildKnightCoord -> {
                            aSessionController.getKnightController().buildKnight(buildKnightCoord, currentPColor);
                            KnightRequest knightRequest = KnightRequest.build(currentP.getUsername(), currentPColor, buildKnightCoord);
                            CatanGame.client.sendTCP(knightRequest);

                        });

                        aSessionController.getSessionScreen().initChooseIntersectionMove(validPositionsForBasicKnight, displaceKnight);

                        aSessionController.getSessionScreen().initChooseIntersectionMove(validPositionsForDisplacedKnight, displaceKnight);
                    }
                });

                aSessionController.getSessionScreen().initChooseIntersectionMove(validKnightsForDisplace, displaceKnight);

                break;


            case SABOTEUR:
                // when you play this card, each player who has as many or more victory points than you must 
                // discard half (round down) of his cards to the bank (resource/commodity cards)
                
                // initialize list of players with as many or more victory points
                updatePlayersWithMoreOrEqualVP(playersWithMoreVP, currentP);
                
                for (Player p : playersWithMoreVP) {
                    DiscardHalfRequest request = DiscardHalfRequest.newInstance(currentP.getUsername(), p.getUsername());
                    CatanGame.client.sendTCP(request);
                }
                aSessionManager.finishCurrentlyExecutingProgressCard();
                
                break;
            case SPY:
                // look at another player's hand of progress cards. you may choose 1 card to take and add to your hand
                
                ArrayList<Player> opponents = new ArrayList<>();
                for (Player p : aSessionController.getPlayers()) {
                    if (!p.equals(currentP)) { opponents.add(p); }
                }
                
                // multistep move to prompt player to choose another player
                MultiStepMove choosePlayerForProgressCard = new MultiStepMove();
                
                choosePlayerForProgressCard.<Player>addMove(chosenPlayer -> {
                    // sends request to chosen player to look at they hand of progress cards
                    TargetedShowProgressCardsRequest request = TargetedShowProgressCardsRequest.newInstance(currentP.getUsername(), chosenPlayer.getUsername());
                    CatanGame.client.sendTCP(request);
                });
                
                aSessionController.getSessionScreen().chooseOtherPlayer(opponents, choosePlayerForProgressCard);
                aSessionManager.finishCurrentlyExecutingProgressCard();
                
                break;
            case WARLORD:
                for (Knight k: currentP.getKnights()) {
                    aSessionController.activateKnight(currentP.getColor(), k, false);
                    ActivateKnightRequest request = ActivateKnightRequest.newInstance(true, currentPColor, currentP.getUsername(), k.getPosition());
                    CatanGame.client.sendTCP(request);
                }
                aSessionManager.finishCurrentlyExecutingProgressCard();
                break;
            case WEDDING:
                
                // adds all the players with more VP points than current player
                updatePlayersWithMoreVP(playersWithMoreVP, currentP);
                
                // sends a targeted request to each player with more VP
                for (Player p : playersWithMoreVP) {
                    TargetedChooseResourceCardRequest request = TargetedChooseResourceCardRequest.newInstance(2, CatanGame.account.getUsername(), p.getUsername());
                    CatanGame.client.sendTCP(request);
                    aSessionController.pendingResponses++;
                }
                
                // disable all action until every target has reponded
                aSessionController.getSessionScreen().disableAllButtons();
                
                aSessionManager.finishCurrentlyExecutingProgressCard();

                break;
            case COMMERCIALHARBOUR:
                // you may force each of the other players to make a special trade. you may offer each opponent any 1 resource card
                // from your hand. He must exchange it for any 1 commodity card of his choice from his hand, if he has any. 
                
                // set the initial list of trading players to all players with at least one commodity card
                ArrayList<Player> tradingPlayers = new ArrayList<>();
                for (Player p : aSessionManager.getPlayers()) {
                    if (!p.equals(currentP) && 
                           (p.getResources().get(ResourceKind.CLOTH) > 0 || 
                            p.getResources().get(ResourceKind.COIN) > 0  || 
                            p.getResources().get(ResourceKind.PAPER) > 0  )) {
                        tradingPlayers.add(p);
                    }
                }
                
                // set the initial list of available Resources local player can choose from
                ArrayList<ResourceKind> availableResources = new ArrayList<>();
                for (Entry<ResourceKind, Integer> entry : currentP.getResources().entrySet()) {
                    if (entry.getValue() > 0 && entry.getKey() != ResourceKind.CLOTH && entry.getKey() != ResourceKind.COIN && entry.getKey() != ResourceKind.PAPER) {
                        availableResources.add(entry.getKey());
                    }
                }
                
                // init button that will be added temporarily to local sessionscreen
                TextButton initForceTrade = new TextButton("Force Trade", CatanGame.skin);
                if (tradingPlayers.isEmpty()) {
                    initForceTrade.setDisabled(true);
                    aSessionController.getSessionScreen().addGameMessage("No players are available for forcing a special trade");
                }
                if (availableResources.isEmpty()) {
                    initForceTrade.setDisabled(true);
                    aSessionController.getSessionScreen().addGameMessage("You do not have any resources to trade");
                }
                
                
                // add listener to button that will start the multistep move
                initForceTrade.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        
                        MultiStepMove forceTrade = new MultiStepMove();
                        forceTrade.<Player>addMove(chosenPlayer -> {
                            
                            // update list of players to choose from
                            tradingPlayers.remove(chosenPlayer);
                            if (tradingPlayers.isEmpty()) {
                                initForceTrade.setDisabled(true);
                                aSessionController.getSessionScreen().addGameMessage("No more players are available for forcing a special trade after this"); 
                            }
                            
                            forceTrade.<ResourceKind>addMove(kind -> {
                                // update list of resources currentP can choose from
                                if (currentP.getResources().get(kind) == 0) {
                                    availableResources.remove(kind);
                                }
                                if (availableResources.isEmpty()) {
                                    initForceTrade.setDisabled(true);
                                    aSessionController.getSessionScreen().addGameMessage("You do not have any more resources to trade after this");
                                }
                                
                                // remove chosen kind from local inventory
                                ResourceMap resourceGiven = new ResourceMap();
                                resourceGiven.put(kind, 1);
                                currentP.removeResources(resourceGiven);
                                aSessionController.getSessionScreen().updateResourceBar(currentP.getResources());
                                
                                // sends request to chosen player with chosen kind that will force them to send back a commodity
                                SpecialTradeRequest request = SpecialTradeRequest.newInstance(kind, currentP.getUsername(), chosenPlayer.getUsername());
                                CatanGame.client.sendTCP(request);
                                
                                // set pending responses to 1
                                // interaction will be unlocked once target has responded
                                aSessionController.pendingResponses = 1;
                            });
                            
                            aSessionController.getSessionScreen().chooseResource(availableResources, forceTrade);
                        });
                        
                        aSessionController.getSessionScreen().chooseOtherPlayer(tradingPlayers, forceTrade);
                    }
                });
                
                aSessionController.getSessionScreen().addTemporaryFunctionality(initForceTrade);
                
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
                // you may use one resource or commodity of your choice to make any number of 2:1 trades with the supply 
                // during the turn that you play this card.
                
                // multistep move to prompt player to choose a commodity or resource
                MultiStepMove chooseTradeSupply = new MultiStepMove();
                
                chooseTradeSupply.<ResourceKind>addMove(kind -> {
                    // set chosen temporary 2:1 trade of current player to kind
                    currentP.setTemporaryResourceKindTrade(kind);
                    
                    // ends the move
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                // begin the multistepmove 
                aSessionController.getSessionScreen().chooseResource(Arrays.asList(ResourceKind.values()), chooseTradeSupply);
                
                break;
            case MERCHANT:
                // place the merchant on a land hex next to your settlement or city
                
                ArrayList<Hex> validMerchantMovePositions = new ArrayList<>();
                for (Hex hex : aGameBoardManager.getHexes()) {
                    for (Village v : aGameBoardManager.getAdjacentVillages(hex)) {
                        if (v.getOwner().equals(currentP) && 
                                hex.getKind() != TerrainKind.BIG_FISHERY && 
                                hex.getKind() != TerrainKind.SEA && 
                                hex.getKind() != TerrainKind.SMALL_FISHERY &&
                                hex.getKind() != TerrainKind.GOLDFIELD){
                            validMerchantMovePositions.add(hex);
                            break;
                        }
                    }
                }
                
                MultiStepMove moveMerchant = new MultiStepMove();
                
                moveMerchant.<Hex>addMove(hex -> {
                    aSessionController.moveMerchant(hex, currentP.getColor(), false);
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                aSessionController.getSessionScreen().initChooseHexMove(validMerchantMovePositions, moveMerchant);
                
                break;
            case RESOURCEMONOPOLY:
                // name a resource. each player must give you 2 of that type of resource if they have them
                ArrayList<ResourceKind> resources = new ArrayList<>();
                resources.add(ResourceKind.BRICK);
                resources.add(ResourceKind.GRAIN);
                resources.add(ResourceKind.ORE);
                resources.add(ResourceKind.WOOD);
                resources.add(ResourceKind.WOOL);

                
                // multistep move to prompt player to choose a resource, and take 1 card of that commodity from each player
                MultiStepMove chooseResourceKind = new MultiStepMove();

                chooseResourceKind.<ResourceKind>addMove(resource -> {
                    // resources taken from each player with at least one chosen resource card
                    ResourceMap resourceTake = new ResourceMap();
                    resourceTake.put(resource, 2);

                    for (Player p : aSessionController.getPlayers()) {
                        // if the opponent player has at least one card of the chosen commodity, take one such card
                        if (!p.equals(currentP) && p.getResources().get(resource) > 0) {
                            // if player only has one card of chosen resources, only take one card
                            if (p.getResources().get(resource) == 1) { resourceTake.put(resource, 1); }
                            
                            // takes card from player
                            TakeResources request = TakeResources.newInstance(resourceTake, CatanGame.account.getUsername(), p.getUsername());
                            CatanGame.client.sendTCP(request);
                            
                            // gives card to current player
                            currentP.addResources(resourceTake);
                        }
                    }
                    // updates the sessionscreen resources of current player
                    aSessionController.getSessionScreen().updateResourceBar(currentP.getResources());
                    
                    // ends the move
                    aSessionController.getSessionScreen().interractionDone();
                });

                // begin the multistepmove
                aSessionController.getSessionScreen().chooseResource(resources, chooseResourceKind);
                
                break;
            case TRADEMONOPOLY: // name a commodity (coin paper cloth). each player must give you 1 commodity of that type if they have them
                ArrayList<ResourceKind> commodities = new ArrayList<>();
                commodities.add(ResourceKind.CLOTH);
                commodities.add(ResourceKind.COIN);
                commodities.add(ResourceKind.PAPER);

                // multistep move to prompt player to choose a commodity, and take 1 card of that commodity from each player
                MultiStepMove chooseCommodityType = new MultiStepMove();
                
                chooseCommodityType.<ResourceKind>addMove(commodity -> {
                    // resources taken from each player with at least one chosen commodity card
                    ResourceMap commodityTake = new ResourceMap();
                    commodityTake.put(commodity, 1);

                    for (Player p : aSessionController.getPlayers()) {
                        // if the opponent player has at least one card of the chosen commodity, take one such card
                        if (!p.equals(currentP) && p.getResources().get(commodity) > 0) {
                            // takes card from player
                            TakeResources request = TakeResources.newInstance(commodityTake, CatanGame.account.getUsername(), p.getUsername());
                            CatanGame.client.sendTCP(request);
                            
                            // gives card to current player
                            currentP.addResources(commodityTake);
                        }
                    }
                    // updates the sessionscreen resources of current player
                    aSessionController.getSessionScreen().updateResourceBar(currentP.getResources());
                    
                    // ends the move
                    aSessionController.getSessionScreen().interractionDone();
                });
                
                // begin the multistepmove
                aSessionController.getSessionScreen().chooseResource(commodities, chooseCommodityType);

                
                break;
        }
    }

    private void updateValidMoveRoad(List<Pair<CoordinatePair,CoordinatePair>> validRoadPos, Pair<CoordinatePair,CoordinatePair> roadToMove) {
        HashSet<CoordinatePair> validRoadEndpoints = aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor());

        // removes the end point at the intersection not connected to ships other than validShip
        for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
            if (i.equals(roadToMove.getLeft()) || i.equals(roadToMove.getRight())) {
                boolean hasOtherEndpoint = false;
                for (CoordinatePair j : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
                    if (aSessionController.isAdjacent(i, j) && !(j.equals(roadToMove.getLeft()) || j.equals(roadToMove.getRight()))) {
                        hasOtherEndpoint = true;
                        break;
                    }
                }
                if (!hasOtherEndpoint) {
                    validRoadEndpoints.remove(i);
                }
            }
        }


        for (CoordinatePair i : validRoadEndpoints) {
            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                if (aSessionController.isAdjacent(i, j) && aSessionController.isOnLand(i, j)) {

                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
                    validRoadPos.add(edge);

                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                            validRoadPos.remove(edge);
                        }
                    }
                }
            }
        }

        // add the valid ship position to validEdges
        validRoadPos.add(roadToMove);
    }
    
    void updateValidEdges(List<Pair<CoordinatePair,CoordinatePair>> validEdges) {
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
        for (Player p : aSessionManager.getPlayers()) { //TODO: uncomment after testing all progress cards is done
            if (!p.equals(currentPlayer) /*&& aSessionController.currentVP(p) > aSessionController.currentVP(currentPlayer)*/) {
                playersWithMoreVPlist.add(p);
            }
        }
    }
    
    private void updatePlayersWithMoreOrEqualVP(List<Player> playersWithMoreVPList, Player currentPlayer) {
        playersWithMoreVPList.clear();
        for (Player p : aSessionManager.getPlayers()) { //TODO: uncomment after testing all progress cards is done
            if (!p.equals(currentPlayer) /*&& aSessionController.currentVP(p) >= aSessionController.currentVP(currentPlayer)*/) {
                playersWithMoreVPList.add(p);
            }
        }
    }

    private void updateValidKnights(List<CoordinatePair> validKnights, CoordinatePair chosenKnight) {
        for(CoordinatePair i : validKnights) {
            if (i.equals(chosenKnight)) {
                validKnights.remove(i);
            }
        }
    }
}
