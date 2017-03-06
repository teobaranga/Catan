package com.mygdx.catan.request;

import java.util.List;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;

public class ResourceProduction extends ForwardedRequest {
    private List<ResourceMap> resourceUpdates;
    
    private ResourceProduction(List<ResourceMap> updates) {
        resourceUpdates = updates;
    }
    
    public List<ResourceMap> getResourceUpdates() {
        return resourceUpdates;
    }
    
    public static ResourceProduction newInstance(List<ResourceMap> updates, String username) {
        ResourceProduction request = new ResourceProduction(updates);
        request.username = username;
        //should we include sender here?
        request.universal = false;
        return request; 
    }
}
