package com.mygdx.catan.moves;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.CoordinatePair;

/**
 * MultiStepMove for moving ship
 * */
public class MultiStepMovingshipMove extends MultiStepMove {
    private Pair<CoordinatePair, CoordinatePair> shipToMove;
    
    public void setShipToMove(Pair<CoordinatePair, CoordinatePair> edge) {
        shipToMove = edge;
    }
    
    public Pair<CoordinatePair, CoordinatePair> getShipToMove() {
        return shipToMove;
    }
}
