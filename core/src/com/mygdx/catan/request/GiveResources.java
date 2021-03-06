package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

public class GiveResources extends TargetedRequest {
    
    private ResourceMap resources;
    
    /**
     * @param resources to give to target player
     * @param sender (username) of resources
     * @param target (username) of resources
     * */
    public static GiveResources newInstance(ResourceMap resources, String sender, String target) {
        GiveResources request = new GiveResources();
        request.resources = resources;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public ResourceMap getResources() {
        return resources;
    }
}
