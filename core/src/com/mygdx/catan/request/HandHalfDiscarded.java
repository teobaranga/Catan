package com.mygdx.catan.request;

public class HandHalfDiscarded extends TargetedRequest {

    public static HandHalfDiscarded newInstance(String sender, String target) {
        HandHalfDiscarded request = new HandHalfDiscarded();
        
        request.sender = sender;
        request.target = target;
        
        return request;
    }
}
