package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Forwarded request that indicates the merchant was moved, and its new owner
 * */
public class MoveMerchantRequest extends ForwardedRequest {
    
    private PlayerColor owner;
    private Pair<Integer,Integer> newPos;
    
    public static MoveMerchantRequest newInstance(Pair<Integer,Integer> newPos, PlayerColor owner, String username) {
        MoveMerchantRequest request = new MoveMerchantRequest();
        
        request.newPos = newPos;
        request.owner = owner;
        request.username = username;
        request.universal = false;
        
        return request;
    }
    
    public PlayerColor getOwner() {
        return owner;
    }
    
    public Pair<Integer,Integer> getNewPos() {
        return newPos;
    }
}
