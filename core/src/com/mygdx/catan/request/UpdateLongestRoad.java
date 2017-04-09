package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;

public class UpdateLongestRoad extends ForwardedRequest {

    private PlayerColor newOwner;
    
    public static UpdateLongestRoad newInstance(PlayerColor newOwner, String username) {
        UpdateLongestRoad request = new UpdateLongestRoad();
        
        request.newOwner = newOwner;
        request.universal = true;
        request.username = username;
        
        return request;
    }
    
    public PlayerColor getNewOwner() {
        return newOwner;
    }
}
