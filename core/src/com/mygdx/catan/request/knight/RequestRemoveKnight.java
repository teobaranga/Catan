package com.mygdx.catan.request.knight;

import com.mygdx.catan.request.TargetedRequest;

public class RequestRemoveKnight extends TargetedRequest {
    
    public static RequestRemoveKnight newInstance(String sender, String target) {
        RequestRemoveKnight request = new RequestRemoveKnight();
        
        request.sender = sender;
        request.target = target;
        
        return request;
    }
}
