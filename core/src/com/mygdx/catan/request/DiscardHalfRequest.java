package com.mygdx.catan.request;

public class DiscardHalfRequest extends TargetedRequest {
    
    public static DiscardHalfRequest newInstance(String sender, String target) {
        DiscardHalfRequest request = new DiscardHalfRequest();
        
        request.sender = sender;
        request.target = target;
        
        return request;
    }

}
