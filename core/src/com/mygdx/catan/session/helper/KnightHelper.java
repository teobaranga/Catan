package com.mygdx.catan.session.helper;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.enums.SessionScreenModes;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.request.knight.KnightRequest;
import com.mygdx.catan.session.GamePieces;
import com.mygdx.catan.session.KnightController;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.session.SessionScreen;
import com.mygdx.catan.ui.KnightActor;
import com.mygdx.catan.ui.window.KnightActionsWindow;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.mygdx.catan.session.SessionScreen.LENGTH;
import static com.mygdx.catan.session.SessionScreen.OFFX;

public class KnightHelper {

    private final List<CoordinatePair> validIntersections;
    private final MutablePair<Integer, Integer> boardOrigin;
    private final HashSet<Window> popups;
    private final SessionScreen sessionScreen;
    private final List<Image> highlightedPositions;
    @Inject SessionController sessionController;
    @Inject KnightController knightController;
    @Inject GamePieces gamePieces;
    private SessionScreenModes prevMode;

    public KnightHelper(SessionScreen sessionScreen) {
        sessionScreen.sessionComponent.inject(this);
        validIntersections = sessionScreen.validIntersections;
        boardOrigin = sessionScreen.boardOrigin;
        popups = sessionScreen.popups;

        this.sessionScreen = sessionScreen;

        highlightedPositions = new ArrayList<>();
    }

    public MultiStepMove buildKnight() {
        Stage sessionStage = sessionScreen.aSessionStage;
        Stage gamePiecesStage = sessionScreen.gamePiecesStage;
        // Make sure the player has everything required to build a knight
        if (!knightController.requestBuildKnight()) {
            sessionScreen.addGameMessage("You can't build a knight (not enough resources or no valid positions)");
            return null;
        }

        // Create the highlighted positions showing where a knight could be placed
        List<CoordinatePair> validBuildKnightPositions = sessionController.getValidBuildKnightPositions();
        //    List<CoordinatePair> validBuildKnightPositions = sessionController.requestValidInitializationBuildIntersections();

        if (validBuildKnightPositions.isEmpty()) {
            Label msg = new Label("There are no valid positions\nwhere you can build a knight.", CatanGame.skin);
            msg.setAlignment(Align.center);
            new Dialog("Warning", CatanGame.skin)
                    .text(msg)
                    .button("OK")
                    .show(sessionStage);
            return null;
        }

        for (CoordinatePair position : validBuildKnightPositions) {
            validIntersections.add(position);

            CoordinatePair intersection = CoordinatePair.of(
                    boardOrigin.getLeft() + position.getLeft() * OFFX,
                    boardOrigin.getRight() + position.getRight() * -LENGTH / 2, null);

            Image knightPosition = gamePieces.createKnightPosition(sessionController.getLocalPlayer());

            knightPosition.setPosition(intersection.getLeft() - knightPosition.getOriginX(),
                    intersection.getRight() - knightPosition.getOriginY());

            gamePiecesStage.addActor(knightPosition);
            highlightedPositions.add(knightPosition);
        }

        // Create a new multi-step move to allow the player to place a knight
        MultiStepMove move = new MultiStepMove();

        startMove();

        // Make the current move place a knight at the specified position
        move.<CoordinatePair>addMove(chosenIntersection -> {
            // Clear the highlighted positions
            endMove();

            // Build the knight
            KnightActor knightActor = knightController.buildKnight(chosenIntersection);
            knightActor.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    KnightActionsWindow actionWindow = knightActor.displayActions(sessionStage);
                    actionWindow.setWindowCloseListener(() -> popups.remove(actionWindow));
                    actionWindow.setOnKnightActivateClick(knightActor1 -> {
                        // Attempt to activate the knight
                        if (knightController.requestActivateKnight(knightActor.getKnight())) {
                            return true;
                        }
                        // Inform the player
                        Label msg = new Label("You do not have sufficient resources\nto activate this knight.", CatanGame.skin);
                        msg.setAlignment(Align.center);
                        new Dialog("Insufficient resources", CatanGame.skin)
                                .text(msg)
                                .button("OK")
                                .show(sessionStage);
                        return false;
                    });
                    actionWindow.setOnKnightUpgradeClick(knightActor1 -> {
                        // Attempt to promote the knight
                        if (knightController.requestPromoteKnight(knightActor.getKnight())) {
                            return true;
                        }
                        // Inform the player
                        Label msg = new Label("You do not have sufficient resources\nto promote this knight.", CatanGame.skin);
                        msg.setAlignment(Align.center);
                        new Dialog("Insufficient resources", CatanGame.skin)
                                .text(msg)
                                .button("OK")
                                .show(sessionStage);
                        return false;
                    });
                    actionWindow.setOnKnightMoveClick(knightActor1 -> {
                        MultiStepMove moveKnight = moveKnight(knightActor1);
                        if (moveKnight != null)
                            sessionScreen.setCurrentlyPerformingMove(moveKnight);
                        return true;
                    });
                    popups.add(actionWindow);
                }
            });
            sessionScreen.addKnight(knightActor);
            // Mark the coordinate position as occupied
            chosenIntersection.putKnight(knightActor.getKnight());
        });

        return move;
    }

    private MultiStepMove moveKnight(KnightActor knightActor) {
        Stage sessionStage = sessionScreen.aSessionStage;
        List<CoordinatePair> validMovePositions = knightController.getValidMovePositions(knightActor.getKnight());
        if (validMovePositions.isEmpty()) {
            Label msg = new Label("There are no valid positions\nwhere you can move this knight.", CatanGame.skin);
            msg.setAlignment(Align.center);
            new Dialog("Warning", CatanGame.skin)
                    .text(msg)
                    .button("OK")
                    .show(sessionStage);
            return null;
        }

        Stage gamePiecesStage = sessionScreen.gamePiecesStage;

        for (CoordinatePair position : validMovePositions) {
            validIntersections.add(position);

            CoordinatePair intersection = CoordinatePair.of(
                    boardOrigin.getLeft() + position.getLeft() * OFFX,
                    boardOrigin.getRight() + position.getRight() * -LENGTH / 2, null);

            Image knightPosition = gamePieces.createKnightPosition(sessionController.getLocalPlayer());

            knightPosition.setPosition(intersection.getLeft() - knightPosition.getOriginX(),
                    intersection.getRight() - knightPosition.getOriginY());

            gamePiecesStage.addActor(knightPosition);
            highlightedPositions.add(knightPosition);
        }

        MultiStepMove move = new MultiStepMove();

        startMove();

        move.<CoordinatePair>addMove(chosenIntersection -> {
            // Clear the highlighted positions
            endMove();

            KnightRequest request = KnightRequest.move(sessionController.getLocalPlayer().getUsername(),
                    knightActor.getKnight().getId(),
                    chosenIntersection);
            CatanGame.client.sendTCP(request);

            // Move the knight
            knightController.moveKnight(knightActor.getKnight().getId(), chosenIntersection);
            knightActor.refresh();
        });

        return move;
    }

    private void startMove() {
        // Keep track of the previous session mode
        prevMode = sessionScreen.aMode;

        // Switch the mode to allow the player to choose an intersection
        sessionScreen.aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
    }

    /** Clear the highlighted positions */
    public void endMove() {
        if (prevMode != null) {
            validIntersections.clear();
            for (Image knightPosition : highlightedPositions) {
                knightPosition.remove();
            }
            highlightedPositions.clear();

            // Go back to the previous mode
            sessionScreen.aMode = prevMode;
            prevMode = null;

            // re-enable all appropriate actions
            sessionScreen.enablePhase(sessionController.getCurrentGamePhase());
        }
    }
}
