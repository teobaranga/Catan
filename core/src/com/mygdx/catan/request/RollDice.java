package com.mygdx.catan.request;

import java.util.Map;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;

public class RollDice extends ForwardedRequest {
    private Map<Player, ResourceMap> resourceUpdates;
    
    private RollDice(Map<Player, ResourceMap> updates) {
        resourceUpdates = updates;
    }
    
    public Map<Player, ResourceMap> getResourceUpdates() {
        return resourceUpdates;
    }
    
    public static RollDice newInstance(Map<Player, ResourceMap> updates, String username) {
        RollDice request = new RollDice(updates);
        request.username = username;
        //should we include sender here?
        request.universal = false;
        return request; 
    }
}
