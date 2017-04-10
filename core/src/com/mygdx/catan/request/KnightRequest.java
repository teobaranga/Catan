package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;
import org.apache.commons.lang3.tuple.Pair;

public class KnightRequest extends ForwardedRequest {
    private PlayerColor owner;

    // Need pair to indicate the position
    private Pair<Integer, Integer> position;

    /** ID of the knight. Used only when activating or promoting */
    private int id;

    private Type type;

    public static KnightRequest build(String username, PlayerColor owner, Pair<Integer, Integer> position) {
        KnightRequest request = new KnightRequest();
        request.username = username;
        request.universal = false;
        request.type = Type.BUILD;
        request.owner = owner;
        request.position = position;
        return request;
    }

    public static KnightRequest activate(String username, int id) {
        KnightRequest request = new KnightRequest();
        request.username = username;
        request.universal = false;
        request.type = Type.ACTIVATE;
        request.id = id;
        return request;
    }

    public static KnightRequest promote(String username, int id) {
        KnightRequest request = new KnightRequest();
        request.username = username;
        request.universal = false;
        request.type = Type.PROMOTE;
        request.id = id;
        return request;
    }

    public Type getType() {
        return type;
    }

    public PlayerColor getOwner() {
        return owner;
    }

    public Pair<Integer, Integer> getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public enum Type {
        BUILD, ACTIVATE, PROMOTE
    }
}
