package com.mygdx.catan.request;

import java.util.List;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;

public class RollDice extends ForwardedRequest {
    private List<ResourceMap> resourceUpdates;
    
    private RollDice(List<ResourceMap> updates) {
        resourceUpdates = updates;
    }
    
    public List<ResourceMap> getResourceUpdates() {
        return resourceUpdates;
    }
    
    public static RollDice newInstance(List<ResourceMap> updates, String username) {
        RollDice request = new RollDice(updates);
        request.username = username;
        //should we include sender here?
        request.universal = false;
        return request; 
    }
}
