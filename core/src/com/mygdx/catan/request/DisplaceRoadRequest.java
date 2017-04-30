package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

public class DisplaceRoadRequest extends ForwardedRequest {
    
    private Pair<Integer,Integer> firstCoordinate;
    private Pair<Integer,Integer> secondCoordinate;
    
    public static DisplaceRoadRequest newInstance(Pair<Integer,Integer> firstCor, Pair<Integer,Integer> secondCor, String username) {
        DisplaceRoadRequest request = new DisplaceRoadRequest();
        
        request.firstCoordinate = firstCor;
        request.secondCoordinate = secondCor;
        request.username = username;
        request.universal = true;
        
        return request;
    }
    
    public Pair<Integer,Integer> getFirstCoordinate() {
        return firstCoordinate;
    }
    
    public Pair<Integer,Integer> getSecondCoordinate() {
        return secondCoordinate;
    }

}
