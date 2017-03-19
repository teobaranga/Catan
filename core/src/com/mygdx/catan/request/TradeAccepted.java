package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

public class TradeAccepted extends ForwardedRequest {

    /** The username of the player whose offer was accepted */
    private String chosenUsername;

    /** The offer that was accepted by the trade initiator */
    private ResourceMap remoteOffer;

    /** The offer of the trade initiator */
    private ResourceMap localOffer;

    /**
     * Create a new message of type TradeAccepted.
     *
     * @param username       username of the sender
     * @param chosenUsername username of the player whose offer was accepted
     */
    public static TradeAccepted newInstance(String username, String chosenUsername, ResourceMap remoteOffer, ResourceMap localOffer) {
        final TradeAccepted tradeAccepted = new TradeAccepted();
        tradeAccepted.username = username;
        tradeAccepted.chosenUsername = chosenUsername;
        tradeAccepted.remoteOffer = remoteOffer;
        tradeAccepted.localOffer = localOffer;
        return tradeAccepted;
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
