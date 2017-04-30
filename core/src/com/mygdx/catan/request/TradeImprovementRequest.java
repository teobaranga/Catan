package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Created by amandaivey on 4/2/17.
 */
public class TradeImprovementRequest extends ForwardedRequest {
    private PlayerColor owner;
    private int level;

    public static TradeImprovementRequest newInstance(PlayerColor owner, String username, int level) {
        TradeImprovementRequest request = new TradeImprovementRequest();
        request.owner = owner;
        request.level = level;
        request.username = username;
        request.universal = false;

        return request;
    }
    
    public PlayerColor getOwner() {
        return owner;
    }
    
    public int getNewLevel() {
        return level;
    }
}
