package com.mygdx.catan.request.knight;

import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.request.ForwardedRequest;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by amandaivey on 3/26/17.
 */
public class ActivateKnightRequest extends ForwardedRequest {
    private boolean active;
    private PlayerColor owner;
    private Pair<Integer,Integer> position;

    //need pair to indicate the position

    public static ActivateKnightRequest newInstance(boolean active, PlayerColor owner, String username, Pair<Integer,Integer> position) {
        ActivateKnightRequest request = new ActivateKnightRequest();
        request.active = active;
        request.owner = owner;
        request.username = username;
        request.position = position;
        request.universal = false;

        return request;

    }

    public PlayerColor getOwner() { return owner; }

    public Pair<Integer,Integer> getPosition() {
        return position;
    }

}
