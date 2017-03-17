package com.mygdx.catan.request;

import com.mygdx.catan.ResourceMap;

public class TradeProposal extends ForwardedRequest {

    private ResourceMap offer, request;

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
