package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;
import org.apache.commons.lang3.tuple.Pair;

public class BuildKnightRequest extends ForwardedRequest {
    private PlayerColor owner;

    // Need pair to indicate the position
    private Pair<Integer, Integer> position;

    public static BuildKnightRequest newInstance(String username, PlayerColor owner, Pair<Integer, Integer> position) {
        BuildKnightRequest request = new BuildKnightRequest();
        request.username = username;
        request.owner = owner;
        request.position = position;
        request.universal = false;
        return request;
    }

    public PlayerColor getOwner() {
        return owner;
    }

    public Pair<Integer, Integer> getPosition() {
        return position;
    }
}
