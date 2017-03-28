package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by amandaivey on 3/26/17.
 */
public class MoveKnightRequest extends ForwardedRequest {

    private Pair<Integer,Integer> originalPos;
    private Pair<Integer,Integer> newPos;

    private PlayerColor owner;

    public static MoveKnightRequest newInstance(Pair<Integer,Integer> originalPos, Pair<Integer,Integer> newPos, PlayerColor owner, String username) {
        MoveKnightRequest request = new MoveKnightRequest();
        request.originalPos = originalPos;
        request.newPos = newPos;
        request.owner = owner;
        request.username = username;
        request.universal = false;

        return request;
    }

}
