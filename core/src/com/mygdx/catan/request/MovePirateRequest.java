package com.mygdx.catan.request;


import org.apache.commons.lang3.tuple.Pair;

public class MovePirateRequest extends ForwardedRequest{

    private Pair<Integer,Integer> newPos;

    public static MovePirateRequest newInstance(Pair<Integer,Integer> newPos, String username) {
        MovePirateRequest request = new MovePirateRequest();

        request.newPos = newPos;
        request.username = username;
        request.universal = false;

        return request;
    }

    public Pair<Integer,Integer> getNewPos() {
        return newPos;
    }

}
