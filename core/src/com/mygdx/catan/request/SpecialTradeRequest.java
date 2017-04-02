package com.mygdx.catan.request;

import com.mygdx.catan.enums.ResourceKind;

/**
 * Targeted request that will give target given resource, and force them to choose a commodity to give back.
 * Assumes the target has commodities to choose from.
 * */
public class SpecialTradeRequest extends TargetedRequest {

    private ResourceKind kind;
    
    public static SpecialTradeRequest newInstance(ResourceKind kind, String sender, String target) {
        SpecialTradeRequest request = new SpecialTradeRequest();
        
        request.kind = kind;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public ResourceKind getKind() {
        return kind;
    }
}
