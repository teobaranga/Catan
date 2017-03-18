package com.mygdx.catan;

import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.moves.MultiStepMovingshipMove;
import com.mygdx.catan.session.SessionManager;
import com.mygdx.catan.session.SessionScreen;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.moves.Move;
import com.mygdx.catan.enums.ProgressCardType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amandaivey on 3/14/17.
 */
public class ProgressCardHandler {

    private static ProgressCardHandler instance;
    private SessionController aSessionController;
    SessionScreen aSessionScreen = aSessionController.getSessionScreen();

    public static ProgressCardHandler getInstance() {
        if (instance == null)
            instance = new ProgressCardHandler();
        return instance;
    }


    public void handle (ProgressCardType pType, PlayerColor currentPColor) {
        final Player currentP = aSessionController.getCurrentPlayer();
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
                aSessionScreen.initChooseIntersectionMove(validCityWallIntersections, playEngineer);
                playEngineer.addMove(new Move() {
                    @Override
                    public void doMove(Object o) {
                        CoordinatePair myCityWallCoordinates = (CoordinatePair) o;
                        aSessionController.buildCityWall(currentPColor, myCityWallCoordinates, true);
                        //revert back to choose action mode and enable buttons
                        aSessionScreen.interractionDone();
                    }
                });
            case INVENTOR:
                break;
            case IRRIGATION:
                break;
            case MEDICINE:
                break;
            case MINING:
                break;
            case PRINTER:
                break;
            case ROADBUILDING:
                break;
            case SMITH:
                break;
            case BISHOP:
                break;
            case CONSTITUTION:
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


}
