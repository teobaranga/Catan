package com.mygdx.catan.request;

/**
 * Forwarded Request for targeted player to choose resource cards from their hand.
 */
public class TargetedChooseResourceCardRequest extends TargetedRequest {

    private int cardsToChoose;
    
    public static TargetedChooseResourceCardRequest newInstance(int numberOfCards, String sender, String target) {
        TargetedChooseResourceCardRequest request = new TargetedChooseResourceCardRequest();
        
        request.sender = sender;
        request.target = target;
        request.cardsToChoose = numberOfCards;
        
        return request;
    }
    
    public int getNumberOfCards() {
        return cardsToChoose;
    }
    
}
