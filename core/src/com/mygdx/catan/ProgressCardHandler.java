package com.mygdx.catan;

import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.session.SessionController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amandaivey on 3/14/17.
 */
public class ProgressCardHandler {

    private SessionController aSessionController;
    //private SessionScreen aSessionScreen;

    public ProgressCardHandler(SessionController sessionController) {
        aSessionController = sessionController;
        //SessionScreen aSessionScreen = aSessionController.getSessionScreen();
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
                aSessionController.getSessionScreen().initChooseIntersectionMove(validCityWallIntersections, playEngineer);
                playEngineer.<CoordinatePair>addMove(myCityWallCoordinates -> {
                    aSessionController.buildCityWall(currentPColor, myCityWallCoordinates, true);
                    //revert back to choose action mode and enable buttons
                    aSessionController.getSessionScreen().interractionDone();
                });
            case INVENTOR:
                break;
            case IRRIGATION:
                break;
            //allows player to upgrade a settlement to a city for 2 ore and 1 grain
            case MEDICINE:
                ArrayList<CoordinatePair> validUpgradeIntersections = new ArrayList<>();
                List<Village> listOfSettlements = currentP.getVillages();
                for (Village v : listOfSettlements) {
                    if(v.getVillageKind() == VillageKind.SETTLEMENT) {
                        validUpgradeIntersections.add(v.getPosition());
                    }
                }
                MultiStepMove playMedicine = new MultiStepMove();
                aSessionController.getSessionScreen().initChooseIntersectionMove(validUpgradeIntersections, playMedicine);
                playMedicine.addMove(new Move() {
                    @Override
                    public void doMove(Object o) {
                        CoordinatePair myCityCoordinates = (CoordinatePair) o;
                        aSessionController.buildCityWall(currentPColor, myCityCoordinates, true);
                        //revert back to choose action mode and enable buttons
                        aSessionController.getSessionScreen().interractionDone();
                    }
                });
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
