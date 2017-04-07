package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.*;
import com.mygdx.catan.TradeAndTransaction.TradeManager;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.*;
import com.mygdx.catan.injection.component.SessionComponent;
import com.mygdx.catan.injection.module.SessionModule;
import com.mygdx.catan.moves.Move;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.*;
import com.mygdx.catan.response.DiceRolled;
import com.mygdx.catan.ui.KnightActor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.mygdx.catan.enums.GamePhase.*;
import static com.mygdx.catan.enums.ResourceKind.*;
import static com.mygdx.catan.enums.VillageKind.CITY;

public class SessionController {

    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;
    private final TransactionManager aTransactionManager;
    private final TradeManager tradeManager;

    private final SessionScreen aSessionScreen;

    /** Listener for server messages */
    private final Listener aSessionListener;

    private final ProgressCardHandler aProgressCardHandler;

    private final GameRules aGameRules;

    private final GamePieces gamePieces;

    /** The random number generator for dice rolls */
    private final CatanRandom random;

    /** int that represents the number of players who have chosen a village to pillage during a barbarian attack */
    private int villagesPillaged;

    /** The local player */
    private Player localPlayer;

    /** Flag indicating whether it's the turn of the player logged in */
    private boolean myTurn;

    private final KnightController knightController;

    final SessionComponent sessionComponent;

    SessionController(SessionScreen sessionScreen) {
        Game currentGame = GameManager.getInstance().getCurrentGame();
        if (currentGame == null) {
            currentGame = GameManager.newPlaceholderGame();
            GameManager.getInstance().setCurrentGame(currentGame);
        }

        sessionComponent = CatanGame.appComponent.plus(new SessionModule(currentGame.session));

        aGameBoardManager = GameBoardManager.getInstance();
        aSessionManager = SessionManager.getInstance(currentGame.session);
        aTransactionManager = TransactionManager.getInstance(aSessionManager);
        tradeManager = TradeManager.getInstance(aTransactionManager);
        aProgressCardHandler = new ProgressCardHandler(this);
        aGameRules = GameRules.getGameRulesInstance();
        gamePieces = GamePieces.getInstance();
        aSessionScreen = sessionScreen;

        random = CatanRandom.getInstance();

        // sets the color as the accounts associated Player object color
        for (Player p : aSessionManager.getPlayers()) {
            if (p.getAccount().equals(CatanGame.account)) {
                localPlayer = p;
                break;
            }
        }

        // Create the sub-controllers once the local player was found
        knightController = new KnightController(this);

        aSessionListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof DiceRolled) {
                    Gdx.app.postRunnable(() -> {
                        // Inform the player of the dice roll
                        final DiceRolled diceRolled = (DiceRolled) object;
                        aSessionScreen.addGameMessage(diceRolled.getUsername() + " rolled a " + diceRolled.getDiceRoll());
                        aSessionScreen.showDice(diceRolled.getDiceRoll().getRed(), diceRolled.getDiceRoll().getYellow());

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
                                endPhase(TURN_FIRST_PHASE);
                                break;
                            default:
                                break;
                        }
                    });
                } else if (object instanceof DistributeResources) {
                    Gdx.app.postRunnable(() -> {
                        final DistributeResources distributeRequest = (DistributeResources) object;

                        resourceProduction(distributeRequest.getDiceResults().getSum());
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
                } else if (object instanceof MoveEdge) {
                    Gdx.app.postRunnable(() -> {
                        final MoveEdge edgeMoved = (MoveEdge) object;
                        aSessionScreen.addGameMessage(edgeMoved.username + " moved a ship");
                        Pair<Integer, Integer> originfirstCor = edgeMoved.getOriginleftPos();
                        Pair<Integer, Integer> originsecondCor = edgeMoved.getOriginrightPos();
                        Pair<Integer, Integer> newfirstCor = edgeMoved.getnewleftPos();
                        Pair<Integer, Integer> newsecondCor = edgeMoved.getnewrightPos();

                        CoordinatePair originfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(originfirstCor.getLeft(), originfirstCor.getRight());
                        CoordinatePair originsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(originsecondCor.getLeft(), originsecondCor.getRight());
                        CoordinatePair newfirstPos = aGameBoardManager.getCoordinatePairFromCoordinates(newfirstCor.getLeft(), newfirstCor.getRight());
                        CoordinatePair newsecondPos = aGameBoardManager.getCoordinatePairFromCoordinates(newsecondCor.getLeft(), newsecondCor.getRight());

                        moveEdge(originfirstPos, originsecondPos, newfirstPos, newsecondPos, edgeMoved.getOwner(), edgeMoved.getKind(), true);
                    });
                } else if (object instanceof DisplaceRoadRequest) {
                    Gdx.app.postRunnable(() -> {
                        final DisplaceRoadRequest displaceRoad = (DisplaceRoadRequest) object;
                        aSessionScreen.addGameMessage(displaceRoad.username+ " has displaced a road");

                        CoordinatePair firstCoordinate = aGameBoardManager.getCoordinatePairFromCoordinates(displaceRoad.getFirstCoordinate().getLeft(), displaceRoad.getFirstCoordinate().getRight());
                        CoordinatePair secondCoordinate = aGameBoardManager.getCoordinatePairFromCoordinates(displaceRoad.getSecondCoordinate().getLeft(), displaceRoad.getSecondCoordinate().getRight());

                        displaceEdgeUnit(firstCoordinate, secondCoordinate);

                    });
                } else if (object instanceof SwitchHexDiceNumbers) {
                  Gdx.app.postRunnable(() -> {
                      final SwitchHexDiceNumbers diceNumbersSwitched = (SwitchHexDiceNumbers) object;
                      aSessionScreen.addGameMessage(diceNumbersSwitched.username + " switched the number tokens of two hexes");
                      Pair<Integer,Integer> firstHexPos = diceNumbersSwitched.getFirstHex();
                      Pair<Integer,Integer> secondHexPos = diceNumbersSwitched.getSecondHex();

                      Hex firstHex = aGameBoardManager.getHexFromCoordinates(firstHexPos.getLeft(), firstHexPos.getRight());
                      Hex secondHex = aGameBoardManager.getHexFromCoordinates(secondHexPos.getLeft(), secondHexPos.getRight());

                      int firstHexNumberToken = firstHex.getDiceNumber();
                      firstHex.setDiceNumber(secondHex.getDiceNumber());
                      secondHex.setDiceNumber(firstHexNumberToken);
                  });  } else if (object instanceof ChooseResourceCardRequest) {
                  Gdx.app.postRunnable(() -> {
                      final ChooseResourceCardRequest chooseResources = (ChooseResourceCardRequest) object;


                      MultiStepMove chooseResourceCards = new MultiStepMove();
                      chooseResourceCards.<ResourceMap>addMove(map -> {
                          localPlayer.removeResources(map);
                          aSessionScreen.updateResourceBar(localPlayer.getResources());

                          // send targeted message to username player
                          GiveResources request = GiveResources.newInstance(map, CatanGame.account.getUsername(), chooseResources.username);
                          CatanGame.client.sendTCP(request);

                          aSessionScreen.interractionDone();
                      });

                      int resourcesToChoose = chooseResources.getNumberOfCards();
                      // if client player has less than requested number of resources, it is set to hand size
                      if (localPlayer.getResourceHandSize() < resourcesToChoose) { resourcesToChoose = localPlayer.getResourceHandSize(); }

                      if (resourcesToChoose > 0) {
                          aSessionScreen.addGameMessage(chooseResources.username + " has requested that you choose " + resourcesToChoose + " resources from your hand");
                          aSessionScreen.chooseMultipleResource(localPlayer.getResources(), resourcesToChoose, chooseResourceCards);
                      }

                  });
                } else if (object instanceof MoveMerchantRequest) {
                    Gdx.app.postRunnable(() -> {
                        final MoveMerchantRequest merchantMoved = (MoveMerchantRequest) object;
                        aSessionScreen.addGameMessage(merchantMoved.username + " moved and now owns the merchant");

                        Hex newPos = aGameBoardManager.getHexFromCoordinates(merchantMoved.getNewPos().getLeft(), merchantMoved.getNewPos().getRight());

                        moveMerchant(newPos, merchantMoved.getOwner(), true);
                    });
                } else if (object instanceof MoveRobberRequest) {
                    Gdx.app.postRunnable(() -> {
                        final MoveRobberRequest robberMoved = (MoveRobberRequest) object;
                        aSessionScreen.addGameMessage(robberMoved.username + " moved the robber");

                        Hex newPos = aGameBoardManager.getHexFromCoordinates(robberMoved.getNewPos().getLeft(), robberMoved.getNewPos().getRight());

                        moveRobber(newPos, true);
                    });
                } else if (object instanceof SpecialTradeRequest) {
                    Gdx.app.postRunnable(() -> {
                        final SpecialTradeRequest specialTrade = (SpecialTradeRequest) object;
                        aSessionScreen.addGameMessage(specialTrade.sender + " has forced you to make a special trade, and has given you a " + specialTrade.getKind().toString().toLowerCase());

                        // creates a list of commodities to choose from 
                        ArrayList<ResourceKind> commodities = new ArrayList<>();
                        for (Map.Entry<ResourceKind, Integer> entry : localPlayer.getResources().entrySet()) {
                            if (entry.getValue() > 0 && (entry.getKey() == ResourceKind.CLOTH || entry.getKey() == ResourceKind.COIN || entry.getKey() == ResourceKind.PAPER)) {
                                commodities.add(entry.getKey());
                            }
                        }

                        // add given resource to local inventory
                        ResourceMap resourceGiven = new ResourceMap();
                        resourceGiven.put(specialTrade.getKind(), 1);
                        localPlayer.addResources(resourceGiven);
                        aSessionScreen.updateResourceBar(localPlayer.getResources());

                        MultiStepMove forceTrade = new MultiStepMove();
                        forceTrade.<ResourceKind>addMove(kind -> {
                            ResourceMap commodityGiven = new ResourceMap();
                            commodityGiven.put(kind, 1);

                            localPlayer.removeResources(commodityGiven);
                            aSessionScreen.updateResourceBar(localPlayer.getResources());

                            // send targeted message back to sender to give them chosen commodity
                            GiveResources request = GiveResources.newInstance(commodityGiven, localPlayer.getUsername(), specialTrade.sender);
                            CatanGame.client.sendTCP(request);

                            aSessionScreen.interractionDone();
                        });

                        aSessionScreen.chooseResource(commodities, forceTrade);
                    });
                } else if (object instanceof GiveResources) {
                    Gdx.app.postRunnable(() -> {
                        final GiveResources resourcesGiven = (GiveResources) object;
                        aSessionScreen.addGameMessage(resourcesGiven.sender + " gave you resources");

                        localPlayer.addResources(resourcesGiven.getResources());
                        aSessionScreen.updateResourceBar(localPlayer.getResources());

                    });
                } else if (object instanceof TakeResources) {
                    Gdx.app.postRunnable(() -> {
                        final TakeResources resourcesTaken = (TakeResources) object;
                        aSessionScreen.addGameMessage(resourcesTaken.sender + " took your resources");

                        localPlayer.removeResources(resourcesTaken.getResources());
                        aSessionScreen.updateResourceBar(localPlayer.getResources());

                    });
                } else if (object instanceof UpdateResources) {
                    Gdx.app.postRunnable(() -> {
                        final UpdateResources resourcesUpdated = (UpdateResources) object;

                        Player peer = aSessionManager.getPlayerFromColor(resourcesUpdated.getResourceOwner());
                        peer.setResources(resourcesUpdated.getUpdatedResources());

                        aSessionScreen.addGameMessage(resourcesUpdated.username + " now has " + peer.getResourceHandSize() + " resource and commodities cards");
                    });
                } else if (object instanceof TargetedShowProgressCardsRequest) {
                    Gdx.app.postRunnable(() -> {
                        final TargetedShowProgressCardsRequest showProgressCards = (TargetedShowProgressCardsRequest) object;
                        aSessionScreen.addGameMessage("you show your progress card hand to " + showProgressCards.sender);

                        // send progress card hand back to sender
                        ChooseOpponentProgressCard request = ChooseOpponentProgressCard.newInstance(localPlayer.getProgressCardHand(), localPlayer.getUsername(), showProgressCards.sender);
                        CatanGame.client.sendTCP(request);
                    });
                } else if (object instanceof ChooseOpponentProgressCard) {
                    Gdx.app.postRunnable(() -> {
                        final ChooseOpponentProgressCard chooseCardtoSteal = (ChooseOpponentProgressCard) object;
                        aSessionScreen.addGameMessage("choose progress card to steal from " + chooseCardtoSteal.sender);

                        // multistep move that will steal chosen progress card
                        MultiStepMove stealCard = new MultiStepMove();

                        stealCard.<ProgressCardType>addMove(type -> {
                            // take progress card from sender
                            TakeProgressCard request = TakeProgressCard.newInstance(type, localPlayer.getUsername(), chooseCardtoSteal.sender);
                            CatanGame.client.sendTCP(request);

                            // add card to local player hand
                            localPlayer.addProgressCard(type);

                            aSessionScreen.interractionDone();
                        });

                        // prompt local player to choose progress card from given hand
                        if (!chooseCardtoSteal.getHand().isEmpty()) {
                            aSessionScreen.chooseProgressCard(chooseCardtoSteal.getHand(), stealCard);
                        } else {
                            aSessionScreen.addGameMessage("chosen opponent does not have any progress cards in hand");
                            aSessionScreen.interractionDone();
                        }

                    });
                } else if (object instanceof TakeProgressCard) {
                    Gdx.app.postRunnable(() -> {
                        final TakeProgressCard progressCardsTaken = (TakeProgressCard) object;
                        aSessionScreen.addGameMessage(progressCardsTaken.sender + " stole your progress card(s)");

                        localPlayer.removeProgressCard(progressCardsTaken.getProgressCard());
                    });
                } else if (object instanceof DiscardHalfRequest) {
                    Gdx.app.postRunnable(() -> {
                        final DiscardHalfRequest discardHalf = (DiscardHalfRequest) object;
                        aSessionScreen.addGameMessage(discardHalf.sender + " has forced you to discard half of your hand");

                        int cardsToDiscard = localPlayer.getResourceHandSize() / 2;

                        // multistep move that will prompt player to discard half their hand
                        MultiStepMove discard = new MultiStepMove();
                        discard.<ResourceMap>addMove(cards -> {
                            aTransactionManager.payPlayerToBank(localPlayer, cards);
                            aSessionScreen.updateResourceBar(localPlayer.getResources());
                            aSessionScreen.interractionDone();
                        });

                        if (cardsToDiscard > 0) {
                            aSessionScreen.addGameMessage("You must discard " + cardsToDiscard + " cards");
                            aSessionScreen.chooseMultipleResource(localPlayer.getResources(), cardsToDiscard, discard);
                        } else {
                            aSessionScreen.addGameMessage("Lucky for you, you have no cards to discard");
                        }

                    });
                } else if (object instanceof EndTurn) {
                    Gdx.app.postRunnable(() -> {
                        aSessionScreen.addGameMessage(((EndTurn) object).username + " ended its turn");
                        endTurn();
                    });
                } else if (object instanceof TradeProposal) {
                    Gdx.app.postRunnable(() -> {
                        // Get the trade proposal info
                        final TradeProposal tradeProposal = (TradeProposal) object;
                        final ResourceMap offer = tradeProposal.getOffer();
                        final ResourceMap request = tradeProposal.getRequest();
                        if (request != null) {
                            // Start a new trade
                            sessionScreen.onIncomingTrade(tradeProposal.username, offer, request);
                        } else {
                            // Add an offer to an existing trade
                            sessionScreen.onTradeOfferReceived(tradeProposal.username, offer);
                        }
                    });
                } else if (object instanceof TradeOfferAccept) {
                    Gdx.app.postRunnable(() -> {
                        final TradeOfferAccept tradeOfferAccept = (TradeOfferAccept) object;
                        // Perform the trade on the player whose offer was accepted
                        if (tradeOfferAccept.getChosenUsername().equals(localPlayer.getUsername())) {
                            // The local offer is from the perspective of the trade initiator
                            final ResourceMap localOffer = tradeOfferAccept.getLocalOffer();
                            final ResourceMap remoteOffer = tradeOfferAccept.getRemoteOffer();
                            tradeManager.trade(localPlayer, localOffer, remoteOffer);
                            sessionScreen.updateResourceBar(localPlayer.getResources());
                        }
                        sessionScreen.onTradeCompleted();
                    });
                } else if (object instanceof TradeCancel) {
                    Gdx.app.postRunnable(aSessionScreen::onTradeCompleted);
                } else if (object instanceof TradeOfferCancel) {
                    Gdx.app.postRunnable(() -> aSessionScreen.onTradeOfferCancelled(((TradeOfferCancel) object).username));
                } else if (object instanceof  UpdateOldBoot){
                    Gdx.app.postRunnable(() -> {
                        UpdateOldBoot updateOldBoot = (UpdateOldBoot) object;
                        aSessionScreen.updateBootOwner(updateOldBoot.username);
                        for (Player p : getPlayers()) {
                            if (p.getUsername().equals(updateOldBoot.username)) {
                                setBootOwner(p);
                            }
                        }
                    });
                } else if (object instanceof UpdateVP){
                    Gdx.app.postRunnable(() -> {
                        aSessionScreen.updateVpTables();
                        if(isWinner(localPlayer)) {
                            aSessionScreen.showWinner(localPlayer);
                        }
                    });
                } else if (object instanceof OpponentDrawnProgressCard) {
                    Gdx.app.postRunnable(() -> {
                        OpponentDrawnProgressCard opponentDrawnProgressCard = (OpponentDrawnProgressCard) object;
                        aSessionScreen.addGameMessage(opponentDrawnProgressCard.toString().toLowerCase() + " drew a progress card");
                        ProgressCardType card = opponentDrawnProgressCard.getCard();
                        ProgressCardKind cardKind = aGameRules.getProgressCardKind(card);
                        aGameBoardManager.removeProgressCard(card, cardKind);
                    });
                } else if (object instanceof DrawProgressCard) {
                    Gdx.app.postRunnable(() -> {
                        DrawProgressCard handleEventDie = (DrawProgressCard) object;
                        aSessionScreen.addGameMessage("You may be able to draw a progress card");
                        eventDieProgressCardHandle(handleEventDie.getEventKind(), handleEventDie.getRedDie());
                    });
                } else if (object instanceof BestPlayersWin) {
                    Gdx.app.postRunnable(() -> {
                        aSessionScreen.addGameMessage("You were one of the best players against the Barbarian attack, and get to choose a progress card to draw");

                        MultiStepMove drawProgressCard = new MultiStepMove();
                        drawProgressCard.<ProgressCardKind>addMove((kind) -> {
                            drawProgressCard(kind);
                            aSessionScreen.interractionDone();
                        });

                        aSessionScreen.chooseProgressCardKind(drawProgressCard, Arrays.asList(ProgressCardKind.values()));
                    });
                } else if (object instanceof DefenderOfCatan) {
                    Gdx.app.postRunnable(() -> {
                        DefenderOfCatan defenderOfCatan = (DefenderOfCatan) object;

                        Player winner = aSessionManager.getPlayerFromColor(defenderOfCatan.getWinner());
                        aSessionScreen.addGameMessage(winner.getUsername() + " is the Defender of Catan!");

                        winner.incrementDefenderOfCatanPoints();
                        CatanGame.client.sendTCP(UpdateVP.newInstance(localPlayer.getUsername()));

                    });
                } else if (object instanceof PoliticsImprovementRequest) {
                    Gdx.app.postRunnable(() -> {
                        PoliticsImprovementRequest politicsImproved = (PoliticsImprovementRequest) object;

                        Player owner = aSessionManager.getPlayerFromColor(politicsImproved.getOwner());
                        owner.getCityImprovements().upgradePoliticsLevel();
                    });
                } else if (object instanceof TradeImprovementRequest) {
                    Gdx.app.postRunnable(() -> {
                        TradeImprovementRequest tradeImproved = (TradeImprovementRequest) object;

                        Player owner = aSessionManager.getPlayerFromColor(tradeImproved.getOwner());
                        owner.getCityImprovements().upgradeTradeLevel();
                    });
                } else if (object instanceof ScienceImprovementRequest) {
                    Gdx.app.postRunnable(() -> {
                        ScienceImprovementRequest scienceImproved = (ScienceImprovementRequest) object;

                        Player owner = aSessionManager.getPlayerFromColor(scienceImproved.getOwner());
                        owner.getCityImprovements().upgradeScienceLevel();
                    });
                } else if (object instanceof UpdateVillage) {
                    Gdx.app.postRunnable(() -> {
                        UpdateVillage villageUpdated = (UpdateVillage) object;

                        Pair<Integer,Integer> coord = villageUpdated.getVillageCoord();
                        CoordinatePair villageCoord = aGameBoardManager.getCoordinatePairFromCoordinates(coord.getLeft(), coord.getRight());
                        Village villageToUpdate = villageCoord.getOccupyingVillage();

                        if (villageToUpdate.hasCityWalls()) {
                            villageToUpdate.setCityWalls(false);
                            //TODO remove city walls on session screen
                         } else {
                             villageToUpdate.setVillageKind(VillageKind.SETTLEMENT);
                             villageToUpdate.getOwner().incrementAvailableCities();
                             villageToUpdate.getOwner().decrementAvailableSettlements();
                             aSessionScreen.updateIntersection(villageToUpdate.getPosition(), villageToUpdate.getOwner().getColor(), VillageKind.SETTLEMENT);
                         }
                    });
                } else if(object instanceof PillageVillageRequest) {
                    Gdx.app.postRunnable(() -> {
                        PillageVillageRequest pillage = (PillageVillageRequest) object;

                        ArrayList<CoordinatePair> validCityIntersections = new ArrayList<>();
                        List<Village> listOfVillages = localPlayer.getVillages();
                        for (Village v : listOfVillages) {
                            if (v.getVillageKind() == VillageKind.CITY) {
                                validCityIntersections.add(v.getPosition());
                            }
                        }

                        MultiStepMove pillageVillageMove = new MultiStepMove();
                        pillageVillageMove.addMove((Move<CoordinatePair>) cityPosition -> {
                            //pillage a village if it is a city, make to settlement
                            Village city = cityPosition.getOccupyingVillage();
                            //if the city has city walls then these are removed
                            if (city.hasCityWalls()) {
                               city.setCityWalls(false);
                               //TODO remove city walls on session screen
                            } else {
                                city.setVillageKind(VillageKind.SETTLEMENT);
                                localPlayer.incrementAvailableCities();
                                localPlayer.decrementAvailableSettlements();
                                aSessionScreen.updateIntersection(city.getPosition(), city.getOwner().getColor(), VillageKind.SETTLEMENT);
                                aSessionScreen.updateAvailableGamePieces(localPlayer.getAvailableSettlements(), localPlayer.getAvailableCities(), localPlayer.getAvailableRoads(), localPlayer.getAvailableShips());
                            }

                            CatanGame.client.sendTCP(UpdateVillage.newInstance(localPlayer.getUsername(), new ImmutablePair<Integer,Integer>(city.getPosition().getLeft(), city.getPosition().getRight())));
                            aSessionScreen.interractionDone();

                            // send message back to sender to tell them one of their villages was pillaged
                            VillagePillaged request = VillagePillaged.newInstance(localPlayer.getUsername(), pillage.sender, pillage.getDiceResults(), pillage.getTotalWorstPlayers());
                            CatanGame.client.sendTCP(request);
                        });

                        if (!validCityIntersections.isEmpty()) {
                            aSessionScreen.initChooseIntersectionMove(validCityIntersections, pillageVillageMove);
                        } else {
                            // send message back to sender to tell them one of their villages was "pillaged" (since there was no city to choose)
                            VillagePillaged request = VillagePillaged.newInstance(localPlayer.getUsername(), pillage.sender, pillage.getDiceResults(), pillage.getTotalWorstPlayers());
                            CatanGame.client.sendTCP(request);
                        }

                    });
                } else if (object instanceof VillagePillaged) {
                    Gdx.app.postRunnable(() -> {
                        VillagePillaged pillaged = (VillagePillaged) object;

                        villagesPillaged++;

                        // if all the worst players have had their villages pillaged, we can finally handle the dice roll
                        if (villagesPillaged == pillaged.getTotalWorstPlayers()) {
                            diceResultHandle(pillaged.getDiceResults());
                            villagesPillaged = 0;
                        }

                    });
                }
            }
        };
    }

    /** get Session screen */
    public SessionScreen getSessionScreen() {
        return aSessionScreen;
    }

    public SessionManager getSessionManager() {
        return aSessionManager;
    }
    public ProgressCardHandler getProgressCardHandler() {
        return aProgressCardHandler;
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
        CatanGame.client.sendTCP(UpdateVP.newInstance(localPlayer.getUsername()));
        CatanGame.client.sendTCP(EndTurn.newInstance());
    }

    /**
     * Ends the turn of the current player (may not be local player)
     */
    void endTurn() {
        aSessionManager.nextPlayer();
        // resets all the turn temporary status effects of local player 
        localPlayer.endTurn();
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
        //System.out.println(phase + " ended");
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
        return localPlayer.getColor();
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public GamePhase getCurrentGamePhase() {
        return aSessionManager.getCurrentPhase();
    }

    /**
     * @return true iff it is the client's turn
     * */
    public boolean isMyTurn() {
    	return myTurn;
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

    public List<ProgressCardType> getProgressCardHand() {
        return localPlayer.getProgressCardHand();
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
        Player currentP = aSessionManager.getPlayerFromColor(owner);
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

//        if (kind == CITY && aSessionManager.getSession().getProgressCardMap().get(ProgressCardType.MEDICINE) > 0) {
//            ResourceMap cost = GameRules.getGameRulesInstance().getCityCostWithMedicine();
//            boolean hasAvailCities = currentP.getAvailableCities() > 0;
//            if (currentP.hasEnoughResources(cost) && hasAvailCities) {
//                canBuild = true;
//            } else {
//                canBuild = false;
//            }
//        }
        if (kind == CITY) {
            ResourceMap cost = GameRules.getGameRulesInstance().getCityCost(aSessionManager.getCurrentlyExecutingProgressCard());
            boolean hasAvailCities = currentP.getAvailableCities() > 0;
            if (currentP.hasEnoughResources(cost) && hasAvailCities) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        return canBuild;
    }

    public boolean requestBuildCityWall (PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        Boolean canBuild = false;
//        if (aSessionManager.getSession().getProgressCardMap().get(ProgressCardType.ENGINEER) > 0) {
//            ResourceMap cost = GameRules.getGameRulesInstance().getCityWallWithEngineer();
//            if (currentP.hasEnoughResources(cost)) {
//                canBuild = true;
//            }
//        }
        ResourceMap cost = GameRules.getGameRulesInstance().getCityWallCost(aSessionManager.getCurrentlyExecutingProgressCard());
        if ((currentP.hasEnoughResources(cost))) {
            canBuild = true;
        }
        else {
            canBuild = false;
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
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        boolean hasAvailableShips = currentP.getAvailableShips() > 0;
        boolean hasAvailableRoads = currentP.getAvailableRoads() > 0;
        boolean canBuild = false;
        ResourceMap cost = null;
        if (kind == EdgeUnitKind.SHIP) {
             cost = GameRules.getGameRulesInstance().getShipCost(aSessionManager.getCurrentlyExecutingProgressCard());
            if (currentP.hasEnoughResources(cost) && hasAvailableShips) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        if (kind == EdgeUnitKind.ROAD) {
             cost = GameRules.getGameRulesInstance().getRoadCost(aSessionManager.getCurrentlyExecutingProgressCard());
            if (currentP.hasEnoughResources(cost) && hasAvailableRoads) {
                canBuild = true;
            } else {
                canBuild = false;
            }
        }
        return canBuild;
    }

    public boolean requestBuildKnight() {
        return knightController.requestBuildKnight();
    }

    public boolean requestTradeImprovement(PlayerColor owner) {
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        int currentTradeLevel = getCurrentPlayer().getCityImprovements().getTradeLevel();
        boolean canImprove = currentP.hasEnoughResources(GameRules.getGameRulesInstance().getTradeCityImprovementCost(currentTradeLevel + 1, aSessionManager.getCurrentlyExecutingProgressCard()));
        return canImprove;

    }

    public boolean requestScienceImprovement(PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        int currentScienceLevel = getCurrentPlayer().getCityImprovements().getScienceLevel();
        return currentP.hasEnoughResources(GameRules.getGameRulesInstance().getScienceCityImprovementCost(currentScienceLevel + 1, aSessionManager.getCurrentlyExecutingProgressCard()));
    }

    public boolean requestPoliticsImprovement(PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        int currentPoliticsLevel = getCurrentPlayer().getCityImprovements().getPoliticsLevel();
        return currentP.hasEnoughResources(GameRules.getGameRulesInstance().getPoliticsCityImprovementCost(currentPoliticsLevel + 1, aSessionManager.getCurrentlyExecutingProgressCard()));
    }

    //returns true if progress card play is valid based on game rules
    public boolean controlPlayProgressCard(PlayerColor owner) {
        return true;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to road owned by player and (2) not adjacent to another village and (3) on land (3) unoccupied
     */
    public ArrayList<CoordinatePair> requestValidBuildIntersections(PlayerColor owner) {
        Player currentP = aSessionManager.getPlayerFromColor(owner);
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
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        List<Village> listOfVillages = currentP.getVillages();
        for (Village v : listOfVillages) {
            validUpgradeIntersections.add(v.getPosition());
        }
        return validUpgradeIntersections;
    }

    //TODO: generate valid metropolis intersections

    public ArrayList<CoordinatePair> requestValidMetropolisIntersections(PlayerColor owner) {
        Player currentP = aSessionManager.getCurrentPlayer();
        ArrayList<CoordinatePair> validMetropolisIntersections = new ArrayList<>();
        for (Village v: aSessionManager.getCurrentPlayer().getVillages()) {
            if(v.getVillageKind() == CITY) {
                validMetropolisIntersections.add(v.getPosition());
            }
        }
        return validMetropolisIntersections;
    }

    /**
     * @param owner of requested valid intersections
     * @return a list of all the intersections that are (1) connected to a road or village owned by owner
     */
    public HashSet<CoordinatePair> requestValidRoadEndpoints(PlayerColor owner) {
        // does not change any state, gui does not need to be notified, method call cannot come from peer
        HashSet<CoordinatePair> validRoadEndpoints = new HashSet<>();

        Player currentP = aSessionManager.getPlayerFromColor(owner);

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

        Player currentP = aSessionManager.getPlayerFromColor(owner);

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

    	Player currentP = aSessionManager.getPlayerFromColor(owner);
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

    //todo: return valid knigths for moving
    public ArrayList<Knight> requestValidKnightsForMove(PlayerColor owner) {
        ArrayList <Knight> validKnights = new ArrayList<>();
        return validKnights;
    }

    //todo: return valid move intersections
    public ArrayList<CoordinatePair> requestValidMovingKnightIntersections(PlayerColor owner) {
        ArrayList<CoordinatePair> validMovingKnightIntersections = new ArrayList<>();
        return validMovingKnightIntersections;
    }

    //todo: return valid knigths for displacing
    public ArrayList<Knight> requestValidKnightsForDisplace(PlayerColor owner) {
        ArrayList <Knight> validKnights = new ArrayList<>();
        return validKnights;
    }

    //todo: return valid displace intersections
    public ArrayList<CoordinatePair> requestValidDisplacingKnightIntersections(PlayerColor owner) {
        ArrayList<CoordinatePair> validDisplacingKnightIntersections = new ArrayList<>();
        return validDisplacingKnightIntersections;
    }



    /** produces a list of cities that are eligible for a city wall i.e. must be a city without a city wall */
    public ArrayList<CoordinatePair> requestValidCityWallIntersections(PlayerColor owner) {
        ArrayList<CoordinatePair> validCityWallIntersections = new ArrayList<>();
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        List<Village> listOfVillages = currentP.getVillages();
        for (Village v : listOfVillages) {
            if (v.getVillageKind() == VillageKind.CITY && !v.hasCityWalls()) {
                validCityWallIntersections.add(v.getPosition());
            }
        }
        return validCityWallIntersections;
    }

    public ArrayList<CoordinatePair> requestValidActivateKnightIntersections(PlayerColor owner) {
        ArrayList<CoordinatePair> validInactiveKnights = new ArrayList<>();
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        for (Knight k : aGameBoardManager.getGameBoard().getKnights()) {
            if (currentP.equals(k.getOwner()) && !k.isActive()) {
                validInactiveKnights.add(k.getPosition());
            }
        }
        return validInactiveKnights;
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
        Player currentP = aSessionManager.getPlayerFromColor(owner);

        if (kind == VillageKind.SETTLEMENT) {
            aGameBoardManager.buildSettlement(currentP, position);

            if (fromPeer) {
                aSessionScreen.updateIntersection(position, owner, kind);
            } else {
                aSessionScreen.updateIntersection(position, owner, kind);
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                // note that resources only get updated locally, that is only the player who is connected to the client will have their resources updated. 
                // if this is to change, we would also need to update the currentlyExecutingProgressCard to be the same across the network
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getSettlementCost());
                    aSessionScreen.updateResourceBar(localPlayer.getResources());
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
                // note that resources only get updated locally, that is only the player who is connected to the client will have their resources updated. 
                // if this is to change, we would also need to update the currentlyExecutingProgressCard to be the same across the network
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getCityCost(aSessionManager.getCurrentlyExecutingProgressCard()));
                    aSessionScreen.updateResourceBar(localPlayer.getResources());
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
        CatanGame.client.sendTCP(UpdateVP.newInstance(localPlayer.getUsername()));
        return true;
    }


    /** Build a new basic knight for the local player. */
    KnightActor buildKnight(CoordinatePair position) {
        return knightController.buildKnight(position);
    }

    /** Build a new basic knight for a given player */
    KnightActor buildKnight(CoordinatePair position, PlayerColor color) {
        return knightController.buildKnight(position, color);
    }

    Image buildCityWall(CoordinatePair position, boolean fromPeer) {
        // TODO: Add city wall to gameboard state

        // Display the city wall
        Image cityWall = gamePieces.createCityWall(localPlayer.getColor());
        cityWall.setPosition(position.getLeft() - cityWall.getOriginX(), position.getRight() - cityWall.getOriginY());

        // TODO inform other players

        return cityWall;
    }

    public boolean activateKnight(PlayerColor owner, Knight myKnight, boolean fromPeer) {
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getActivateKnightCost(aSessionManager.getCurrentlyExecutingProgressCard()));
        aGameBoardManager.activateKnight(myKnight);
        CoordinatePair position = myKnight.getPosition();

        //change knight status
        ActivateKnightRequest request = ActivateKnightRequest.newInstance(true, owner, CatanGame.account.getUsername(), new ImmutablePair<>(position.getLeft(), position.getRight()));
        CatanGame.client.sendTCP(request);
        /* TODO: update display, inform other players, check sufficient resources */
        return true;
    }

    //todo: update gameboard, inform players of move
    public boolean moveKnight(PlayerColor owner, Knight k, CoordinatePair newPosition) {
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        aGameBoardManager.moveKnight(currentP, k, newPosition);
        k.setActive(false);
        //update session screen

        MoveKnightRequest request = MoveKnightRequest.newInstance(new ImmutablePair<>(k.getPosition().getLeft(), k.getPosition().getRight()), new ImmutablePair<>(newPosition.getLeft(), newPosition.getRight()), owner, CatanGame.account.getUsername());
        CatanGame.client.sendTCP(request);
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
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        aGameBoardManager.buildEdgeUnit(currentP, firstPosition, secondPosition, kind);
        aSessionScreen.updateEdge(firstPosition, secondPosition, kind, owner);

        if (kind == EdgeUnitKind.ROAD) {
            if (!fromPeer) {
                aSessionScreen.updateAvailableGamePieces(currentP.getAvailableSettlements(), currentP.getAvailableCities(), currentP.getAvailableRoads(), currentP.getAvailableShips());
                // if piece was build during regular turn, appropriate resources are removed from the player
                // note that resources only get updated locally, that is only the player who is connected to the client will have their resources updated. 
                // if this is to change, we would also need to update the currentlyExecutingProgressCard to be the same across the network
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getRoadCost(aSessionManager.getCurrentlyExecutingProgressCard()));
                    aSessionScreen.updateResourceBar(localPlayer.getResources());
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
                // note that resources only get updated locally, that is only the player who is connected to the client will have their resources updated. 
                // if this is to change, we would also need to update the currentlyExecutingProgressCard to be the same across the network
                if (!init) {
                    aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getShipCost(aSessionManager.getCurrentlyExecutingProgressCard()));
                    aSessionScreen.updateResourceBar(localPlayer.getResources());
                }

                // notify peers about board game change
                BuildEdge request = BuildEdge.newInstance(new ImmutablePair<>(firstPosition.getLeft(), firstPosition.getRight()), new ImmutablePair<>(secondPosition.getLeft(), secondPosition.getRight()), kind, owner, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
            }
        }
        //TODO: longest road (fun fact: longest disjoint path problem is NP-hard)
        CatanGame.client.sendTCP(UpdateVP.newInstance(localPlayer.getUsername()));
        return true;
    }

    //TODO: build city wall this is where messages will happen, will tell GUI to show city wall
    public boolean buildCityWall(PlayerColor owner, CoordinatePair myWall, boolean fromPeer){
        Player currentP = aSessionManager.getPlayerFromColor(owner);
        aGameBoardManager.buildCityWall(currentP, myWall);
        //need to change city has wall to true
        //aSessionScreen.updateCity();
        return true;
    }

    /**
     * Requests the GameBoardManager to move the robber to given location. If fromPeer is false, the SessionController sends message to network notifying
     * other peers about board change
     */
    public boolean moveRobber(Hex newPosition, boolean fromPeer) {
        aGameBoardManager.setRobberPosition(newPosition);
        aSessionScreen.placeRobber(newPosition.getLeftCoordinate(), newPosition.getRightCoordinate());

        if (!fromPeer) {
            MoveRobberRequest request = MoveRobberRequest.newInstance(new ImmutablePair<Integer,Integer>(newPosition.getLeftCoordinate(), newPosition.getRightCoordinate()), localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
        }

        return true;
    }

    /**
     * Plays the given progress card type
     * */
    public void playProgressCard(ProgressCardType type) {
        aProgressCardHandler.handle(type, localPlayer.getColor());
        aSessionScreen.removeCardFromHand(type);
    }

    /**
     * Requests the GameBoardManager to move the merchant to given position. If fromPeer is false, SessionController sends a message to the network notifying
     * other peers of board change.
     * @param newPosition       new merchant position
     * @param newOwner          player who now owns the merchant
     * @param fromPeer          indicates whether method was called from localPlayer or peer
     * */
    public boolean moveMerchant(Hex newPosition, PlayerColor newOwner, boolean fromPeer) {
        Player owner = aSessionManager.getPlayerFromColor(newOwner);

        aGameBoardManager.setMerchantOwner(owner);
        aGameBoardManager.setMerchantPosition(newPosition);
        aSessionScreen.placeMerchant(newPosition.getLeftCoordinate(), newPosition.getRightCoordinate());

        if (!fromPeer) {
            MoveMerchantRequest request = MoveMerchantRequest.newInstance(new ImmutablePair<Integer,Integer>(newPosition.getLeftCoordinate(), newPosition.getRightCoordinate()), newOwner, localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
        }

        CatanGame.client.sendTCP(UpdateVP.newInstance(localPlayer.getUsername()));

        return true;
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
    public boolean moveEdge(CoordinatePair firstOriginPos, CoordinatePair secondOriginPos, CoordinatePair newFirstPos, CoordinatePair newSecondPos, PlayerColor owner, EdgeUnitKind kind, boolean fromPeer) {
    	Player currentP = aSessionManager.getPlayerFromColor(owner);
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
    		    MoveEdge request = MoveEdge.newInstance(originleftPos, originrightPos, newleftPos, newrightPos, owner, kind, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
    		} else {
    		    // remove old position of ship from client GUI
                aSessionScreen.removeEdgeUnit(firstOriginPos.getLeft(), firstOriginPos.getRight(), secondOriginPos.getLeft(), secondOriginPos.getRight());
    		}

    		shipToMove.moveEdge(newFirstPos, newSecondPos);
            aSessionScreen.updateEdge(newFirstPos, newSecondPos, kind, owner);

    		return true;
    	}
    }

    /**
     * Finds the ship with given CoordinatePairs as its endpoints, and puts it back in that unit's owner's inventory
     * @param firstCor first CoordinatePair of edge to displace
     * @param secondCor second CoordinatePair of edge to displace
     * @return true if successful
     * */
    public void displaceEdgeUnit(CoordinatePair firstCor, CoordinatePair secondCor) {
        EdgeUnit roadToDisplace = aGameBoardManager.getEdgeUnitFromCoordinatePairs(firstCor, secondCor);

        // removes the road from the gameboard and from the players list of edgeunits
        aGameBoardManager.displaceRoad(roadToDisplace);

        // notify session screen of changes
        aSessionScreen.removeEdgeUnit(firstCor.getLeft(), firstCor.getRight(), secondCor.getLeft(), secondCor.getRight());

        // if owner is local player, update available game pieces
        Player owner = roadToDisplace.getOwner();
        if (owner.equals(localPlayer)) {
            aSessionScreen.addGameMessage("Your edge unit was displaced, it has been returned to your inventory");
            aSessionScreen.updateAvailableGamePieces(owner.getAvailableSettlements(), owner.getAvailableCities(), owner.getAvailableRoads(), owner.getAvailableShips());
        } else {
            aSessionScreen.addGameMessage(owner.getUsername() + "'s edge unit was displaced");
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

        localPlayer.addResources(playerResourceMap);
        aSessionScreen.updateResourceBar(localPlayer.getResources());
    }

    public int currentVP(Player player) {
        int currentVP = 0;
        Player longestRoadOwner =  aSessionManager.getlongestRoadOwner();
        if (player.equals(longestRoadOwner)) {
            currentVP++;
        }
        currentVP += aGameBoardManager.getMerchantPoint(player);
        currentVP += player.getTokenVictoryPoints();
        currentVP += aGameBoardManager.getVillagePoints(player);
        return currentVP;
    }

    private boolean isWinner(Player p){
        return currentVP(p) > aGameRules.getVpToWin() + getBootMalus(p);
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

    /**
     * Local player draws a progress card from given progress card kind deck. The network is notified of stack change
     * */
    private void drawProgressCard(ProgressCardKind kind) {
        ProgressCardType drawnCard;
        OpponentDrawnProgressCard request;
        switch(kind) {
        case POLITICS:
            drawnCard = aGameBoardManager.drawPoliticsProgressCard();
            localPlayer.addProgressCard(drawnCard);

            // updates the stacks of peers
            request = OpponentDrawnProgressCard.newInstance(drawnCard, localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
            break;
        case SCIENCE:
            drawnCard = aGameBoardManager.drawScienceProgressCard();
            localPlayer.addProgressCard(drawnCard);

            // updates the stacks of peers
            request = OpponentDrawnProgressCard.newInstance(drawnCard, localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
            break;
        case TRADE:
            drawnCard = aGameBoardManager.drawPoliticsProgressCard();
            localPlayer.addProgressCard(drawnCard);

            // updates the stacks of peers
            request = OpponentDrawnProgressCard.newInstance(drawnCard, localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
            break;
        default:
            break;
        }
    }

    //The event die yields a trade,science, or politics.
    public void eventDieProgressCardHandle(EventKind eventDieResult, int redDie) {
            int level = localPlayer.getImprovementLevelByType(eventDieResult);
            //player is eligible
            if (redDie <= level) {
                if (localPlayer.getProgressCardCount() != 4) {

                    MultiStepMove chooseProgressCard = new MultiStepMove();
                    chooseProgressCard.<Boolean>addMove((choice) -> {

                            if (choice) {
                                switch(eventDieResult) {
                                case POLITICS:
                                    drawProgressCard(ProgressCardKind.POLITICS);
                                    break;
                                case SCIENCE:
                                    drawProgressCard(ProgressCardKind.SCIENCE);
                                    break;
                                case TRADE:
                                    drawProgressCard(ProgressCardKind.TRADE);
                                    break;
                                default:
                                    break;
                                }
                            }
                            aSessionScreen.interractionDone();
                    });

                    aSessionScreen.chooseDraw(chooseProgressCard);
                }
            }
    }

    /**
     * handles the roll of the two dices and the event die. To be called only from current player.
     * */
    void handleRoll(DiceRollPair diceResults, EventKind eventDieResult) {
        if (eventDieResult == EventKind.BARBARIAN) {
            aSessionManager.decreaseBarbarianPosition();
            if (aSessionManager.getSession().barbarianPosition == 0) {
                //barbarians attack!
                if (!aSessionManager.getSession().firstBarbarianAttack) {
                    aSessionManager.getSession().firstBarbarianAttack = true;
                }
                barbarianHandleAttack(diceResults);
            } else {
                diceResultHandle(diceResults);
            }
        } else {
            diceResultHandle(diceResults);
            DrawProgressCard drawRequest = DrawProgressCard.newInstance(eventDieResult, diceResults.getRed(), localPlayer.getUsername());
            CatanGame.client.sendTCP(drawRequest);
        }


    }

    /**
     * handles result of the red and yellow dice. If their sum is seven, robber is handled, otherwise resources are distributed
     * */
    private void diceResultHandle(DiceRollPair diceResults) {
        int sum = diceResults.getSum();
        if (sum == 7) {
            if (aSessionManager.getSession().firstBarbarianAttack) {
                robberHandleAfterBarbarian();
            } else {
                robberHandleBeforeBarbarian();
            }
        } else {
            // sends message to all peers to distribute resources
            DistributeResources request = DistributeResources.newInstance(diceResults, localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
            // resourceProduction(sum); // this works bitchiz
        }
    }

    /**
     * handles robber before the first barbarian attack. All players with at least 7 cards must discard half their hand.
     * */
    private void robberHandleBeforeBarbarian() {
        //called when a 7 is rolled until the first barbarian attack
        //check players with hand greater than or equal to 7
        for (Player p : aSessionManager.getPlayers()) {
            if(p.getResourceHandSize() >= 7) {
                DiscardHalfRequest request = DiscardHalfRequest.newInstance(localPlayer.getUsername(), p.getUsername());
                CatanGame.client.sendTCP(request);
            }
        }
    }

    /**
     * handles the robber after the first barbarian attack.
     * Player may move the robber, current player may steal a resource from any opponent player whose village is adjacent to the new position
     * */
    private void robberHandleAfterBarbarian() {
        //happens when 7 is rolled AND after the barbarians reach island of catan for the first time
        List<Hex> hexesToChooseFrom = new ArrayList<>();
        List<Hex> boardHexes = aGameBoardManager.getHexes();
        for (Hex h : boardHexes) {
            if (h.getKind() != TerrainKind.SEA && h.getKind() != TerrainKind.BIG_FISHERY && h.getKind() != TerrainKind.SMALL_FISHERY) {
                hexesToChooseFrom.add(h);
            }
        }

        MultiStepMove moveRobber = new MultiStepMove();

        moveRobber.<Hex>addMove(hex -> {
            final Hex chosenHex =  hex;
            ArrayList<Player> potentialVictims = new ArrayList<>();
            List<Village> adjacentVillages = aGameBoardManager.getAdjacentVillages(chosenHex);

            for(Village v : adjacentVillages) {
                if (!localPlayer.equals(v.getOwner()) && !potentialVictims.contains(v.getOwner()) && v.getOwner().getResourceHandSize() != 0) {
                    potentialVictims.add(v.getOwner());
                }
            }

            moveRobber(hex, false);

            // if there is a potential victim, let the local player choose a victim to steal random card from
            if (!potentialVictims.isEmpty()) {
                chooseSteal(moveRobber);
                aSessionScreen.chooseOtherPlayer(potentialVictims,moveRobber);
            } else {
                aSessionScreen.interractionDone();
            }

        });
        aSessionScreen.initChooseHexMove(hexesToChooseFrom, moveRobber);
    }

    private void chooseSteal(MultiStepMove robber){
        robber.<Player>addMove(victim -> {

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
                        localPlayer.addResources(cardToSteal);
                        aSessionScreen.updateResourceBar(localPlayer.getResources());
                        //update opponent hand
                        TakeResources request = TakeResources.newInstance(cardToSteal, localPlayer.getUsername(), victim.getUsername());
                        CatanGame.client.sendTCP(request);
                        aSessionScreen.interractionDone();
                        break;
                    }
                }
            }
        });
    }

    /**
     * handles barbarian attack and the result of the two dice (each are handled sequentially)
     * */
    private void barbarianHandleAttack(DiceRollPair diceResults) {
        //System.out.println("barbarians attacked");

        int barbarianStrength = aGameBoardManager.getCityCount() + aGameBoardManager.getMetropolisCount();
        int activeKnightStrength = 0;

        //determine activeKnightStrength strength
        for (Player p : aSessionManager.getPlayers()) {
           for (Knight activeKnight : p.getActiveKnights()) {
               activeKnightStrength += activeKnight.getStrength();
           }
        }

        //System.out.println(String.format("Barbarians vs Catan: %d : %d", barbarianStrength, activeKnightStrength));

        if (barbarianStrength > activeKnightStrength) {
            //barbarians win
            int minKnightLevel = Integer.MAX_VALUE;
            List<Player> worstPlayers = new ArrayList<>();

            for (Player p : aSessionManager.getPlayers()) {
                for (Village v : p.getVillages()) {
                    VillageKind vk = v.getVillageKind();
                    if (vk == VillageKind.CITY) {
                        int playerKnightLevel = 0;
                        for (Knight k : p.getActiveKnights()) {
                            playerKnightLevel+= k.getStrength();
                        }
                        if (playerKnightLevel < minKnightLevel) {
                            minKnightLevel = playerKnightLevel;
                            worstPlayers.clear();
                            worstPlayers.add(p);
                        } else if (playerKnightLevel == minKnightLevel) {
                            worstPlayers.add(p);
                        }
                        break; // go on to next player
                    }
                }
            }


            // loop through each worst player and send them a request to pillage one of their villages. (this request may be sent back to localPlayer)
            // each request will respond with a VillagePillaged response. Local player will keep track of the number of pillaged villages, and will only
            // call handle dice roll results after all the worst players have sent back a response.
            for (Player p : worstPlayers) {
                PillageVillageRequest request = PillageVillageRequest.newInstance(localPlayer.getUsername(), p.getUsername(), diceResults, worstPlayers.size());
                CatanGame.client.sendTCP(request);
            }

        } else {
            aSessionScreen.addGameMessage("Islanders Win!");
            //islanders win
            int maxKnightLevel = 0;
            List<Player> bestPlayers = new ArrayList<>();

            for (Player p : aSessionManager.getPlayers()) {
                int playerKnightLevel = 0;
                for (Knight k : p.getActiveKnights()) {
                    playerKnightLevel += k.getStrength();
                }
                if (playerKnightLevel > maxKnightLevel) {
                    maxKnightLevel = playerKnightLevel;
                    bestPlayers.clear();
                    bestPlayers.add(p);
                } else if (playerKnightLevel == maxKnightLevel) {
                    bestPlayers.add(p);
                }
            }

            //System.out.println(bestPlayers.size());

            if (bestPlayers.size() == 1) {
                //this player is the defender of catan
                Player bestPlayer = bestPlayers.get(0);

                // send message accross network about defender of catan
                DefenderOfCatan request = DefenderOfCatan.newInstance(bestPlayer.getColor(), localPlayer.getUsername());
                CatanGame.client.sendTCP(request);
            } else {
                //no player will be declared defender of Catan. 
                //each eligible player will draw in CW order 1 card from any of the 3 PC decks
                for (Player bp : bestPlayers) {
                    // send a targeted request to that player that will make them choose a progress card kind, and draw
                    BestPlayersWin request = BestPlayersWin.newInstance(localPlayer.getUsername(), bp.getUsername());
                    CatanGame.client.sendTCP(request);
                }
            }

            // if islanders win, dice result handle can happen at the same time as defender of catan / best players
            diceResultHandle(diceResults);
        }
        //regardless of outcome, barbarians go home.
        aSessionManager.resetBarbarianPosition();
    }

    /**
     * distributes resources according to dice roll to local player
     * */
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
                    case BIG_FISHERY:
                    case SMALL_FISHERY:
                           getFishToken();
                           aSessionScreen.updateFishTable(localPlayer.getFishTokenHand());
                        break;
                    default:
                        break;
                }
            }
        }

        localPlayer.addResources(resAndComMap);

        aSessionScreen.updateResourceBar(localPlayer.getResources());
    }

    int getBootMalus(Player player) {
        return aGameBoardManager.getBootMalus(player);
    }


    void setBootOwner(Player newOwner) {
        aGameBoardManager.setaBootOwner(newOwner);
    }

    ArrayList<Player> getValidBootRecepients(){
        ArrayList<Player> result = new ArrayList<Player>(Arrays.asList(getPlayers()));
        result.remove(localPlayer);
        for (Player p : result) {
            if (currentVP(p) < currentVP(localPlayer)) {
                result.remove(p);
            }
        }
        return result;
    }


    /**
     * Roll the dice according to the phase of the game/session.
     */
    void rollDice() {
        // Roll the red and yellow dice
        DiceRollPair diceResults = random.rollTwoDice();
        RollDice request = null;

        switch (aSessionManager.getCurrentPhase()) {
            case SETUP_PHASE_ONE:
                // We're at the phase where we have to determine who rolled the highest number
                // We don't need to do anything else
                request = RollDice.newInstance(diceResults, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);
                break;
            case TURN_FIRST_PHASE:
                // We're at the phase where we the player rolls the dice and everyone gets appropriate resources
                // Roll the event die as well
                // EventKind eventDieResult = random.rollEventDieBarbarian(); // TODO roll using the correct method
                EventKind eventDieResult = random.rollEventDieBarbarian();

//                aSessionManager.getSession().barbarianPosition = 1;

             // Create the message that informs the other users of the dice roll
                request = RollDice.newInstance(diceResults, eventDieResult, CatanGame.account.getUsername());
                CatanGame.client.sendTCP(request);

                handleRoll(diceResults, eventDieResult);

                aSessionScreen.addGameMessage(String.format("Rolled a %d %d %s", diceResults.getRed(), diceResults.getYellow(), eventDieResult));

                break;
            default:
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
        tradeManager.domesticTrade(offer, request, tradeRatio, localPlayer);
        aSessionScreen.updateResourceBar(localPlayer.getResources());
    }

    /**
     * Start a new p2p trade.
     *
     * @param offer   offer of the local player
     * @param request request of the local player
     */
    void proposeTrade(ResourceMap offer, ResourceMap request) {
        TradeProposal tradeProposal = TradeProposal.newInstance(localPlayer.getUsername(), offer, request);
        CatanGame.client.sendTCP(tradeProposal);
    }

    void proposeOffer(ResourceMap offer) {
        TradeProposal tradeProposal = TradeProposal.newInstance(localPlayer.getUsername(), offer);
        CatanGame.client.sendTCP(tradeProposal);
    }

    /** Cancel the local player's offer. */
    void cancelOffer() {
        TradeOfferCancel tradeOfferCancel = TradeOfferCancel.newInstance(localPlayer.getUsername());
        CatanGame.client.sendTCP(tradeOfferCancel);
        aSessionScreen.onTradeCompleted();
    }

    /**
     * Accept a trade.
     *
     * @param username    username of the player whose offer was accepted
     * @param remoteOffer the offer accepted
     * @param localOffer  the offer of trade initiator
     */
    void acceptTrade(String username, ResourceMap remoteOffer, ResourceMap localOffer) {
        // Perform the trade locally
        tradeManager.trade(localPlayer, remoteOffer, localOffer);
        aSessionScreen.updateResourceBar(localPlayer.getResources());
        // Inform the other players of the trade
        TradeOfferAccept tradeOfferAccept = TradeOfferAccept.newInstance(localPlayer.getUsername(), username, remoteOffer, localOffer);
        CatanGame.client.sendTCP(tradeOfferAccept);
        // Trade completed
        aSessionScreen.onTradeCompleted();
    }

    /** Cancel the entire trade */
    void cancelTrade() {
        TradeCancel tradeCancel = TradeCancel.newInstance(localPlayer.getUsername());
        CatanGame.client.sendTCP(tradeCancel);
        aSessionScreen.onTradeCompleted();
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

            // if localPlayer has played MERCHANTFLEET and chosen resourceKind, set ratio to 2
            if (resourceKind == localPlayer.getTemporaryResourceKindTrade()) { ratio = 2; }

            // if localPlayer owns the merchant, and merchant is on hex of kind resourceKind, set ratio to 2
            Hex merchantPos = aGameBoardManager.getMerchantPosition();
            Player merchantOwner = aGameBoardManager.getMerchantOwner();
            if (merchantPos != null && merchantOwner != null) {
               if (merchantOwner.equals(localPlayer) && resourceKind == aGameRules.getProducingResource(merchantPos.getKind())) {
                   ratio = 2;
               }
            }

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
        knightController.onScreenShown();
    }

    /** Call this when the screen is hidden */
    void onScreenHidden() {
        CatanGame.client.removeListener(aSessionListener);
        knightController.onScreenHidden();
    }

    void getFishToken() {
        FishTokenType type  = aGameBoardManager.drawFishToken();
        if (type == FishTokenType.OLD_BOOT) {
            UpdateOldBoot request = UpdateOldBoot.newInstance(localPlayer.getUsername());
            CatanGame.client.sendTCP(request);
        } else {
            localPlayer.addFishToken(type);
        }
    }

    void fishActionHandle(FishTokenMap consumedFishToken) {
        int fishCount = 0;
        for (Map.Entry<FishTokenType, Integer> entry: consumedFishToken.entrySet()) {
            switch(entry.getKey()){
                case ONE_FISH:
                    fishCount += entry.getValue();
                    break;
                case TWO_FISH:
                    fishCount += entry.getValue() * 2;
                    break;
                case THREE_FISH:
                    fishCount += entry.getValue() * 3;
                    break;
                default:
                    break;
            }
        }
        if (fishCount < 2){
            aSessionScreen.addGameMessage("You have not given enough Fish!");
            return;
        }
        localPlayer.removeFishToken(consumedFishToken);
        aSessionScreen.updateFishTable(localPlayer.getFishTokenHand());
        if (fishCount >= 7) {
            MultiStepMove drawProgressCard = new MultiStepMove();
            drawProgressCard.<ProgressCardKind>addMove((kind) -> {
                drawProgressCard(kind);
                aSessionScreen.interractionDone();
            });
            aSessionScreen.chooseProgressCardKind(drawProgressCard, Arrays.asList(ProgressCardKind.values()));
        } else if (fishCount >= 5) {
            aProgressCardHandler.handle(ProgressCardType.ROADBUILDING, localPlayer.getColor());
        } else if (fishCount >= 4) {
            ArrayList<ResourceKind> choice = new ArrayList<>();
            choice.add(WOOD);
            choice.add(WOOL);
            choice.add(BRICK);
            choice.add(GRAIN);
            choice.add(ORE);
            MultiStepMove getCardFromBank = new MultiStepMove();
            getCardFromBank.<ResourceKind>addMove(resourcekind -> {
                ResourceMap toAdd = new ResourceMap();
                toAdd.add(resourcekind,1);
                localPlayer.addResources(toAdd);
            });
            aSessionScreen.chooseResource(choice, getCardFromBank);
            aSessionScreen.updateResourceBar(localPlayer.getResources());
        } else if (fishCount >= 3) {
            ArrayList<Player> victims = new ArrayList<>(Arrays.asList(getPlayers()));
            victims.remove(localPlayer);
            for (Player p : victims) {
                if (p.getResourceHandSize() == 0){
                    victims.remove(p);
                }
            }
            MultiStepMove rob = new MultiStepMove();
            aSessionScreen.chooseOtherPlayer(victims,rob);

        } else if (fishCount >= 2) {
            aSessionScreen.placeRobber(-15,-10 ); //TODO make this better seriously, arnaud.
        }
    }

    public void tradeCityImprovement(PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        currentP.getCityImprovements().upgradeTradeLevel();
        int level = currentP.getCityImprovements().getTradeLevel();
        aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getTradeCityImprovementCost(level, aSessionManager.getCurrentlyExecutingProgressCard()));
        aSessionScreen.updateResourceBar(currentP.getResources());
        aSessionScreen.updateTradeImprovements(level);
        TradeImprovementRequest request = TradeImprovementRequest.newInstance(owner, CatanGame.account.getUsername(), level);
        CatanGame.client.sendTCP(request);
    }

    public void scienceCityImprovement(PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        currentP.getCityImprovements().upgradeScienceLevel();
        int level = currentP.getCityImprovements().getScienceLevel();
        aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getScienceCityImprovementCost(level, aSessionManager.getCurrentlyExecutingProgressCard()));
        aSessionScreen.updateResourceBar(currentP.getResources());
        aSessionScreen.updateScienceImprovements(level);
        ScienceImprovementRequest request = ScienceImprovementRequest.newInstance(owner, CatanGame.account.getUsername(), level);
        CatanGame.client.sendTCP(request);
    }

    public void politicsCityImprovement(PlayerColor owner) {
        Player currentP = getCurrentPlayer();
        currentP.getCityImprovements().upgradePoliticsLevel();
        int level = currentP.getCityImprovements().getPoliticsLevel();
        aTransactionManager.payPlayerToBank(currentP, GameRules.getGameRulesInstance().getPoliticsCityImprovementCost(level, aSessionManager.getCurrentlyExecutingProgressCard()));
        aSessionScreen.updateResourceBar(currentP.getResources());
        aSessionScreen.updatePoliticsImprovements(level);
        PoliticsImprovementRequest request = PoliticsImprovementRequest.newInstance(owner, CatanGame.account.getUsername(), level);
        CatanGame.client.sendTCP(request);
    }

}
