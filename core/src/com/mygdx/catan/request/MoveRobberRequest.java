package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Forwarded request that indicates the merchant was moved, and its new owner
 * */
public class MoveRobberRequest extends ForwardedRequest {
    
    private Pair<Integer,Integer> newPos;
    
    public static MoveRobberRequest newInstance(Pair<Integer,Integer> newPos, String username) {
        MoveRobberRequest request = new MoveRobberRequest();
        
        request.newPos = newPos;
        request.username = username;
        request.universal = false;
        
        return request;
    }
    
    public Pair<Integer,Integer> getNewPos() {
        return newPos;
    }
}
