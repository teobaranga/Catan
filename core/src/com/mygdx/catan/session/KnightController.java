package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.KnightRequest;
import com.mygdx.catan.ui.KnightActor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mygdx.catan.session.SessionScreen.LENGTH;
import static com.mygdx.catan.session.SessionScreen.OFFX;

public class KnightController {

    private final SessionScreen sessionScreen;

    private final Player localPlayer;

    private final Listener listener;

    @Inject TransactionManager transactionManager;
    @Inject SessionManager sessionManager;
    @Inject GameRules gameRules;
    @Inject GamePieces gamePieces;
    @Inject GameBoardManager gameBoardManager;

    KnightController(SessionController sessionController) {
        sessionController.sessionComponent.inject(this);

        sessionScreen = sessionController.getSessionScreen();
        localPlayer = sessionController.getLocalPlayer();

        listener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof KnightRequest) {
                    // Add the knight created by another player
                    Gdx.app.postRunnable(() -> {
                        KnightRequest request = (KnightRequest) object;
                        switch (request.getType()) {
                            case BUILD:
                                Pair<Integer, Integer> position = request.getPosition();
                                CoordinatePair coordinates = gameBoardManager.getCoordinatePairFromCoordinates(position.getLeft(), position.getRight());
                                KnightActor knightActor = buildKnight(coordinates, request.getOwner());
                                sessionScreen.addKnight(knightActor);
                                break;
                            case ACTIVATE: {
                                // Activate the knight and inform the GUI
                                int id = request.getId();
                                gameBoardManager.activateKnight(id);
                                sessionScreen.refreshKnight(id);
                                break;
                            }
                            case PROMOTE: {
                                // Promote the knight and inform the GUI
                                int id = request.getId();
                                gameBoardManager.promoteKnight(id);
                                sessionScreen.refreshKnight(id);
                                break;
                            }
                        }
                    });
                }
            }
        };
    }

    void onScreenShown() {
        CatanGame.client.addListener(listener);
    }

    void onScreenHidden() {
        CatanGame.client.removeListener(listener);
    }

    /**
     * Checks if a player has everything that's required to build a knight.
     *
     * @return true if the player can build a knight, false otherwise
     */
    boolean requestBuildKnight() {
        // Check if the player has any roads first
        List<EdgeUnit> roads = localPlayer.getRoadsAndShips().stream()
                .filter(edgeUnit -> edgeUnit.getKind() == EdgeUnitKind.ROAD)
                .collect(Collectors.toList());

        if (roads.isEmpty())
            return false;

        // Check if the player has any unoccupied intersections (aka valid positions for the knight)
        List<CoordinatePair> unoccupiedIntersections = roads.stream()
                .flatMap(edgeUnit -> Stream.of(edgeUnit.getAFirstCoordinate(), edgeUnit.getASecondCoordinate()))
                .filter(coordinatePair -> !coordinatePair.isOccupied())
                .collect(Collectors.toList());

        if (unoccupiedIntersections.isEmpty())
            return false;

        // Lastly, make sure the player can pay for the knight
        ResourceMap basicKnightCost = gameRules.getbuildBasicKnightCost();
        return localPlayer.hasEnoughResources(basicKnightCost);
    }

    /** Build a new basic knight for the local player. */
    KnightActor buildKnight(CoordinatePair position) {
        // Make the local player pay for the knight
        transactionManager.payPlayerToBank(localPlayer, gameRules.getbuildBasicKnightCost());
        sessionScreen.updateResourceBar(localPlayer.getResources());

        return buildKnight(position, localPlayer.getColor());
    }

    /** Build a new basic knight for a given player */
    KnightActor buildKnight(CoordinatePair position, PlayerColor color) {
        CoordinatePair intersection = CoordinatePair.of(
                sessionScreen.getBoardOrigin().getLeft() + position.getLeft() * OFFX,
                sessionScreen.getBoardOrigin().getRight() + position.getRight() * -LENGTH / 2, null);

        // Get the player trying to build this knight
        Player player = sessionManager.getPlayerFromColor(color);

        // Build the knight and add it to the game board
        final Knight knight = gameBoardManager.buildKnight(player, position);

        // Display the knight
        KnightActor knightActor = gamePieces.createKnight(knight);
        knightActor.setPosition(intersection.getLeft() - knightActor.getOriginX(),
                intersection.getRight() - knightActor.getOriginY());

        // Inform other players
        if (localPlayer.getColor() == color) {
            Pair<Integer, Integer> knightCoords = new ImmutablePair<>(position.getLeft(), position.getRight());
            KnightRequest request = KnightRequest.build(localPlayer.getUsername(), color, knightCoords);
            CatanGame.client.sendTCP(request);
        }

        return knightActor;
    }

    /**
     * Attempt to activate a knight. If the player has enough resources, the activation
     * cost is deducted from the player and the knight is activated.
     *
     * @return true if the knight was activated, false otherwise
     */
    boolean requestActivateKnight(Knight knight) {
        ProgressCardType type = sessionManager.getCurrentlyExecutingProgressCard();
        ResourceMap activateKnightCost = gameRules.getActivateKnightCost(type);
        boolean enough = localPlayer.hasEnoughResources(activateKnightCost);
        if (!enough) {
            knight.activate();
            transactionManager.payPlayerToBank(localPlayer, activateKnightCost);
            sessionScreen.updateResourceBar(localPlayer.getResources());

            // Inform the other players
            KnightRequest msg = KnightRequest.activate(localPlayer.getUsername(), knight.getId());
            CatanGame.client.sendTCP(msg);

            return true;
        }
        return false;
    }

    /**
     * Attempt to promote a knight. If the player has enough resources, the promotion
     * cost is deducted from the player and the knight is promoted.
     *
     * @return true if the knight was promoted, false otherwise
     */
    boolean requestPromoteKnight(Knight knight) {
        ProgressCardType type = sessionManager.getCurrentlyExecutingProgressCard();
        ResourceMap promoteKnightCost = gameRules.getPromoteKnightCost(type);
        boolean enough = localPlayer.hasEnoughResources(promoteKnightCost);
        if (!enough) {
            knight.promote();
            transactionManager.payPlayerToBank(localPlayer, promoteKnightCost);
            sessionScreen.updateResourceBar(localPlayer.getResources());

            // Inform the other players
            KnightRequest msg = KnightRequest.promote(localPlayer.getUsername(), knight.getId());
            CatanGame.client.sendTCP(msg);

            return true;
        }
        return false;
    }

    //TODO
    public void displaceKnight(CoordinatePair firstPos, CoordinatePair secondPos) {

    }

    //TODO
    public void removeKnight(CoordinatePair knightpos) {

    }
}
