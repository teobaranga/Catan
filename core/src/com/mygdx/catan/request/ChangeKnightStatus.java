package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by amandaivey on 3/26/17.
 */
public class ChangeKnightStatus extends ForwardedRequest {
    private boolean active;
    private PlayerColor owner;
    private Pair<Integer,Integer> position;

    //need pair to indicate the position

    public static ChangeKnightStatus newInstance(boolean active, PlayerColor owner, String username, Pair<Integer,Integer> position) {
        ChangeKnightStatus request = new ChangeKnightStatus();
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
