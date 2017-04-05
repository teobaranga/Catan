package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;

/**
 * Created by amandaivey on 4/2/17.
 */
public class PoliticsImprovementRequest extends ForwardedRequest {
    private PlayerColor owner;
    private int level;

    public static PoliticsImprovementRequest newInstance(PlayerColor owner, String username, int level) {
        PoliticsImprovementRequest request = new PoliticsImprovementRequest();
        request.owner = owner;
        request.level = level;
        request.username = username;
        request.universal = false;

        return request;
    }
}
