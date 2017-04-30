package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

import java.util.List;

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
