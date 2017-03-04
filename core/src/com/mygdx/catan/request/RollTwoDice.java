package com.mygdx.catan.request;

public class RollTwoDice extends ForwardedRequest {
    private int rollResult;
    
    private RollTwoDice(int result) {
        rollResult = result;
    }
    
    public int getRollResult() {
        return rollResult;
    }
    
    public static RollTwoDice newInstance(int result, String username) {
        RollTwoDice request = new RollTwoDice(result);
        request.username = username;
        //should we include sender here?
        request.universal = false;
        return request; 
    }
    
}
