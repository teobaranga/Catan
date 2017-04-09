package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by amandaivey on 4/9/17.
 */
public class DisplaceKnightRequest  extends ForwardedRequest{
    private Pair<Integer, Integer> newPosition;

    public static com.mygdx.catan.request.DisplaceKnightRequest newInstance(Pair<Integer,Integer> newPos, String username) {
        com.mygdx.catan.request.DisplaceKnightRequest request = new com.mygdx.catan.request.DisplaceKnightRequest();

        request.newPosition = newPos;
        request.username = username;
        request.universal = true;

        return request;
    }

    public Pair<Integer,Integer> getNewPosition() {
        return newPosition;
    }
}
