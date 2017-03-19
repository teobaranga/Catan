package com.mygdx.catan.request;

/** Request indicating that a trade was cancelled by the trade initiator */
public class TradeCancel extends ForwardedRequest {

    public static TradeCancel newInstance(String username) {
        final TradeCancel tradeCancel = new TradeCancel();
        tradeCancel.username = username;
        return tradeCancel;
    }
}
