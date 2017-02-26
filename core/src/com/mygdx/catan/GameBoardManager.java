package com.mygdx.catan;


import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.GameBoard;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.session.SessionManager;

import java.util.ArrayList;

public class GameBoardManager {

    private GameBoard gameBoard;
    private SessionManager sessionManager;

    public GameBoardManager(GameBoard gameBoard, SessionManager sessionManager) {
        this.gameBoard = gameBoard;
        this.sessionManager = sessionManager;
    }

    /** Allows the user to place a city and an edge unit and then receive the resources near the city */
    public void placeCityAndRoads(CoordinatePair cityPos, CoordinatePair edgeUnitPos1, CoordinatePair edgeUnitPos2, boolean isShip) {
        //TODO UI CHANGES.
        Player cp = sessionManager.getCurrentPlayer();

        EdgeUnit eu;
        if (isShip) {
            eu = new EdgeUnit(edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.SHIP, cp);
        } else {
            eu = new EdgeUnit(edgeUnitPos1, edgeUnitPos2, EdgeUnitKind.ROAD, cp);
        }
        cp.addEdgeUnit(eu);

        Village v = new Village(cp, cityPos);
        cp.addVillage(v);

        ArrayList<Hex> neighbourHexes = new ArrayList<Hex>();
        ArrayList<Hex> hexes = gameBoard.getHexes();

        for (Hex h : hexes) {
            if (h.isAdjacent(cityPos)) {
                neighbourHexes.add(h);
            }
        }

        ResourceMap cost = new ResourceMap();
        for( Hex h: neighbourHexes) {
            switch (h.getKind()) {
                case FOREST:
                    cost.put(ResourceKind.WOOD, 1);
                    break;
                case MOUNTAINS:
                    cost.put(ResourceKind.ORE, 1);
                    break;
                case HILLS:
                    cost.put(ResourceKind.BRICK, 1);
                    break;
                case FIELDS:
                    cost.put(ResourceKind.GRAIN, 1);
                    break;
                case PASTURE:
                    cost.put(ResourceKind.WOOL, 1);
                    break;
                // GOLDFIELDS ?
            }
        }
        cp.addResources(cost);
    }
}
