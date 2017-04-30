package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

public class TradeOfferAccept extends ForwardedRequest {

    /** The username of the player whose offer was accepted */
    private String chosenUsername;

    /** The offer that was accepted by the trade initiator */
    private ResourceMap remoteOffer;

    /** The offer of the trade initiator */
    private ResourceMap localOffer;

    /**
     * Create a new message of type TradeOfferAccept.
     *
     * @param username       username of the sender
     * @param chosenUsername username of the player whose offer was accepted
     */
    public static TradeOfferAccept newInstance(String username, String chosenUsername, ResourceMap remoteOffer, ResourceMap localOffer) {
        final TradeOfferAccept tradeOfferAccept = new TradeOfferAccept();
        tradeOfferAccept.username = username;
        tradeOfferAccept.chosenUsername = chosenUsername;
        tradeOfferAccept.remoteOffer = remoteOffer;
        tradeOfferAccept.localOffer = localOffer;
        return tradeOfferAccept;
    }

    public String getChosenUsername() {
        return chosenUsername;
    }

    public ResourceMap getRemoteOffer() {
        return remoteOffer;
    }

    public ResourceMap getLocalOffer() {
        return localOffer;
    }
}
