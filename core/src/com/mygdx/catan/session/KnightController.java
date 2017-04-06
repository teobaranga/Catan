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
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.BuildKnightRequest;
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
                if (object instanceof BuildKnightRequest) {
                    // Add the knight created by another player
                    Gdx.app.postRunnable(() -> {
                        BuildKnightRequest request = (BuildKnightRequest) object;
                        Pair<Integer, Integer> position = request.getPosition();
                        CoordinatePair coordinates = gameBoardManager.getCoordinatePairFromCoordinates(position.getLeft(), position.getRight());
                        KnightActor knightActor = buildKnight(coordinates, request.getOwner());
                        sessionScreen.addKnight(knightActor);
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
            BuildKnightRequest request = BuildKnightRequest.newInstance(localPlayer.getUsername(), color, knightCoords);
            CatanGame.client.sendTCP(request);
        }

        return knightActor;
    }
}
