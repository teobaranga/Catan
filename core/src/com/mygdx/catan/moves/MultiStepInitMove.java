package com.mygdx.catan.moves;

import com.mygdx.catan.CoordinatePair;

/**
 * MultiStepMove for initialization (place village and road). 
 * */
public class MultiStepInitMove extends MultiStepMove {
    private CoordinatePair initIntersection;
    
    public void setInitIntersection(CoordinatePair intersection) {
        initIntersection = intersection;
    }
    
    public CoordinatePair getInitIntersection() {
        return initIntersection;
    }
}
