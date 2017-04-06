package com.mygdx.catan.session;

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
import com.mygdx.catan.ui.KnightActor;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KnightController {

    private final SessionScreen sessionScreen;

    private final Player localPlayer;

    @Inject TransactionManager transactionManager;
    @Inject SessionManager sessionManager;
    @Inject GameRules gameRules;
    @Inject GamePieces gamePieces;
    @Inject GameBoardManager gameBoardManager;

    KnightController(SessionController sessionController) {
        sessionScreen = sessionController.getSessionScreen();
        localPlayer = sessionController.getLocalPlayer();
        sessionController.sessionComponent.inject(this);
    }

    /**
     * Checks if a player has everything that's required to build a knight.
     *
     * @return true if the player can build a knight, false otherwise
     */
    public boolean requestBuildKnight() {
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
        // Get the player trying to build this knight
        Player player = sessionManager.getPlayerFromColor(color);

        // Build the knight and add it to the game board
        final Knight knight = gameBoardManager.buildKnight(player, position);

        // Display the knight
        KnightActor knightActor = gamePieces.createKnight(knight);
        knightActor.setPosition(position.getLeft() - knightActor.getOriginX(), position.getRight() - knightActor.getOriginY());

        // TODO: inform other players

        return knightActor;
    }
}
