package com.mygdx.catan.request;

public class BestPlayersWin extends TargetedRequest {
    
    public static BestPlayersWin newInstance(String sender, String target) {
        BestPlayersWin request = new BestPlayersWin();
        
        request.sender = sender;
        request.target = target;
        
        return request;
    } 

}
