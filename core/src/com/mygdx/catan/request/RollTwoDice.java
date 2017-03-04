package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

public class RollTwoDice extends ForwardedRequest {
    private Pair<Integer, Integer> rollResult;
    
    private RollTwoDice(Pair<Integer, Integer> result) {
        rollResult = result;
    }
    
    public Pair<Integer, Integer> getRollResult() {
        return rollResult;
    }
    
    public static RollTwoDice newInstance(Pair<Integer, Integer> result, String username) {
        RollTwoDice request = new RollTwoDice(result);
        request.username = username;
        //should we include sender here?
        request.universal = false;
        return request; 
    }
    
}
