package com.mygdx.catan.response;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Created by Arnaud on 02/03/2017.
 */

public class PlaceCityAndRoad implements Response {

    public boolean fromPeer;
    public PlayerColor aPlayerColor;

    public PlaceCityAndRoad(boolean fromPeer, PlayerColor aPlayerColor){
        this.fromPeer = fromPeer;
        this.aPlayerColor = aPlayerColor;
    }
}
