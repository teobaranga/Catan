package com.mygdx.catan.request;

/**
 * Forwarded request for target player to show sender their hand
 * */
public class TargetedShowProgressCardsRequest extends TargetedRequest {
    
    public static TargetedShowProgressCardsRequest newInstance(String sender, String target) {
        TargetedShowProgressCardsRequest request = new TargetedShowProgressCardsRequest();
        
        request.sender = sender;
        request.target = target;
        
        return request;
    }
}
