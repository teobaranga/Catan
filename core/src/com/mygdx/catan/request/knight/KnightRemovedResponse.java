package com.mygdx.catan.request.knight;

import com.mygdx.catan.request.TargetedRequest;

public class KnightRemovedResponse extends TargetedRequest {

    private int knightStrength;
    
    public static KnightRemovedResponse newInstance(int knightStrength, String sender, String target) {
        KnightRemovedResponse request = new KnightRemovedResponse();
        
        request.knightStrength = knightStrength;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public int getKnightStrength() {
        return knightStrength;
    }
}
