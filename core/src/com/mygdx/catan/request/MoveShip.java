package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Request indicating that the sender player moved one of their ship at origin position to new position
 * */
public class MoveShip extends ForwardedRequest {

    private Pair<Integer,Integer> originleftPos;
    private Pair<Integer,Integer> originrightPos;
    private Pair<Integer,Integer> newleftPos;
    private Pair<Integer,Integer> newrightPos;
    
    private PlayerColor owner;
    
    public static MoveShip newInstance(Pair<Integer,Integer> originleftPos, Pair<Integer,Integer> originrightPos, Pair<Integer,Integer> newleftPos, Pair<Integer,Integer> newrightPos, PlayerColor owner, String username) {
        MoveShip request = new MoveShip();
        request.originleftPos = originleftPos;
        request.originrightPos = originrightPos;
        request.newleftPos = newleftPos;
        request.newrightPos = newrightPos;
        request.owner = owner;
        request.username = username;
        request.universal = false;
        return request;
    }
    
    public PlayerColor getOwner() { return owner; }
    public Pair<Integer,Integer> getOriginleftPos() { return originleftPos; }
    public Pair<Integer,Integer> getOriginrightPos() { return originrightPos; }
    public Pair<Integer,Integer> getnewleftPos() { return newleftPos; }
    public Pair<Integer,Integer> getnewrightPos() { return newrightPos; }
}
