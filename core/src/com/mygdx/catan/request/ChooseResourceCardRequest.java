package com.mygdx.catan.request;

/**
 * Forwarded Request for players to choose resource cards from their hand.
 */
public class ChooseResourceCardRequest extends ForwardedRequest {
    
    private int cardsToChoose;
    
    public static ChooseResourceCardRequest newInstance(int numberOfCards, String username) {
        ChooseResourceCardRequest request = new ChooseResourceCardRequest();
        
        request.username = username;
        request.cardsToChoose = numberOfCards;
        request.universal = false;
        
        return request;
    }
    
    public int getNumberOfCards() {
        return cardsToChoose;
    }
    

}
