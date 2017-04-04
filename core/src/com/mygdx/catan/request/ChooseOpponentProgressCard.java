package com.mygdx.catan.request;

import com.mygdx.catan.enums.ProgressCardType;

import java.util.List;

/**
 * Forwarded request for target player to choose a progress card from given hand
 * */
public class ChooseOpponentProgressCard extends TargetedRequest {
    
    private List<ProgressCardType> hand;
    
    public static ChooseOpponentProgressCard newInstance(List<ProgressCardType> hand, String sender, String target) {
        ChooseOpponentProgressCard request = new ChooseOpponentProgressCard();
        
        request.hand = hand;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public List<ProgressCardType> getHand() {
        return hand;
    }
    
}
