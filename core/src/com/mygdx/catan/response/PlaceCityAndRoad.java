package com.mygdx.catan.response;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.enums.PlayerColor;

/**
 * Created by Arnaud on 02/03/2017.
 */

public class PlaceCityAndRoad implements Response {

    public CoordinatePair<Integer, Integer> cityPos;
    public CoordinatePair<Integer, Integer> edgeUnitPos1;
    public CoordinatePair<Integer, Integer> edgeUnitPos2;
    public boolean isShip;
    public boolean fromPeer;
    public PlayerColor aPlayerColor;

    public PlaceCityAndRoad(CoordinatePair<Integer, Integer> cityPos, CoordinatePair<Integer, Integer> edgeUnitPos1, CoordinatePair<Integer, Integer> edgeUnitPos2, boolean isShip, boolean fromPeer, PlayerColor aPlayerColor){
        this.cityPos = cityPos;
        this.edgeUnitPos1 = edgeUnitPos1;
        this.edgeUnitPos2 = edgeUnitPos2;
        this.isShip =isShip;
        this.fromPeer = fromPeer;
        this.aPlayerColor = aPlayerColor;
    }
}
