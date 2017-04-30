package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

/**
 * Request indicating a trade proposal.
 */
public class TradeProposal extends ForwardedRequest {

    private ResourceMap offer, request;

    /**
     * Create a new trade proposal used when giving a reply/counter-offer to an
     * already existing initial trade proposal (which is why there's no need for a
     * request ResourceMap).
     *
     * @param username username of the sender player
     * @param offer    offer of the player
     */
    public static TradeProposal newInstance(String username, ResourceMap offer) {
        return newInstance(username, offer, null);
    }

    public static TradeProposal newInstance(String username, ResourceMap offer, ResourceMap request) {
        final TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.username = username;
        tradeProposal.offer = offer;
        tradeProposal.request = request;
        return tradeProposal;
    }

    public ResourceMap getOffer() {
        return offer;
    }

    public ResourceMap getRequest() {
        return request;
    }
}
