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

    private final GameBoardManager aGameBoardManager;
    private final SessionManager aSessionManager;

    private final SessionScreen aSessionScreen;


    /** The local player and player color */
    private Player localPlayer;
    private PlayerColor aPlayerColor;

    //private final Listener aSessionListener;

    public ProgressCardHandler (SessionScreen sessionScreen) {


        final Game currentGame = GameManager.getInstance().getCurrentGame();


        aGameBoardManager = GameBoardManager.getInstance();
        aSessionManager = SessionManager.getInstance(currentGame == null ? null : currentGame.session);

        aSessionScreen = sessionScreen;

    }

    public void handle (ProgressCardType pType, PlayerColor currentPColor) {
        final Player currentP = aSessionManager.getCurrentPlayer();
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
                //aSessionScreen.initChooseIntersection(validCityWallIntersections, playEngineer);
                MultiStepMove playEngineer = new MultiStepMove();
                playEngineer.addMove(new Move() {
                    @Override
                    public void doMove(Object o) {
                        CoordinatePair myCityWallCoordinates = (CoordinatePair) o;
                        aGameBoardManager.buildCityWall(currentP, myCityWallCoordinates);
                        //revert back to choose action mode and enable buttons
                        //aSessionScreen.interactionDone();
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
