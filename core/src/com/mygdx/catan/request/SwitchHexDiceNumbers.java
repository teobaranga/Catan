package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * request indicating that the sender player switched hex numbers of the two given hex coordinates
 * */
public class SwitchHexDiceNumbers extends ForwardedRequest {
    private Pair<Integer,Integer> firstHex;
    private Pair<Integer,Integer> secondHex;
    
    public static SwitchHexDiceNumbers newInstance(Pair<Integer,Integer> firstPos, Pair<Integer,Integer> secondPos, String username) {
        SwitchHexDiceNumbers request = new SwitchHexDiceNumbers();
        request.firstHex = firstPos;
        request.secondHex = secondPos;
        request.username = username;
        request.universal = false;
        return request;
    }
    
    public Pair<Integer,Integer> getFirstHex() {
        return firstHex;
    }
    
    public Pair<Integer,Integer> getSecondHex() {
        return secondHex;
    }
}
