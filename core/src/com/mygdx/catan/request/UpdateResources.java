package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.PlayerColor;

/**
 * Forwarded request indicating that the sender player's resources has updated
 * */
public class UpdateResources extends ForwardedRequest {
    
    private ResourceMap updatedResources;
    private PlayerColor resourceOwner;
    
    public static UpdateResources newInstance(ResourceMap updatedResources, PlayerColor resourceOwner, String username) {
        UpdateResources request = new UpdateResources();
        request.username = username;
        request.updatedResources = updatedResources;
        request.resourceOwner = resourceOwner;
        request.universal = false;
        
        return request;
    }
    
    public ResourceMap getUpdatedResources() {
        return updatedResources;
    }
    
    public PlayerColor getResourceOwner() {
        return resourceOwner;
    }
    
}
