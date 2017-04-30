package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Created by amandaivey on 4/2/17.
 */
public class ScienceImprovementRequest extends ForwardedRequest {
    private PlayerColor owner;
    private int level;

    public static ScienceImprovementRequest newInstance(PlayerColor owner, String username, int level) {
        ScienceImprovementRequest request = new ScienceImprovementRequest();
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
