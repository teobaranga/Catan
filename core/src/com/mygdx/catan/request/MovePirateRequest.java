package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Forwarded request that indicates the merchant was moved, and its new owner
 */
public class MovePirateRequest extends ForwardedRequest{

    private Pair<Integer,Integer> newPos;
    private boolean outOfBoard;

    public static MovePirateRequest newInstance(Pair<Integer,Integer> newPos, String username, boolean outOfBoard) {
        MovePirateRequest request = new MovePirateRequest();
        request.outOfBoard = outOfBoard;
        request.newPos = newPos;
        request.username = username;
        request.universal = false;

        return request;
    }
    public  boolean getIfOutOfBoard() { return  outOfBoard; }
    public Pair<Integer,Integer> getNewPos() {
        return newPos;
    }

}
