package com.mygdx.catan.request;

import java.util.ArrayList;

import com.mygdx.catan.enums.ProgressCardType;

/**
 * Forwarded request for target player to choose a progress card from given hand
 * */
public class ChooseOpponentProgressCard extends TargetedRequest {
    
    private ArrayList<ProgressCardType> hand;
    
    public static ChooseOpponentProgressCard newInstance(ArrayList<ProgressCardType> hand, String sender, String target) {
        ChooseOpponentProgressCard request = new ChooseOpponentProgressCard();
        
        request.hand = hand;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public ArrayList<ProgressCardType> getHand() {
        return hand;
    }
    
}
