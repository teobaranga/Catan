package com.mygdx.catan.request;

/** Request sent when a player cancelled his/her trade offer */
public class TradeOfferCancel extends ForwardedRequest {

    public static TradeOfferCancel newInstance(String username) {
        final TradeOfferCancel tradeOfferCancel = new TradeOfferCancel();
        tradeOfferCancel.username = username;
        return tradeOfferCancel;
    }
}
