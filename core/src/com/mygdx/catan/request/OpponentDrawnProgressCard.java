package com.mygdx.catan.request;

import com.mygdx.catan.enums.ProgressCardType;

/**
 * Created by emmaakouri on 2017-03-26.
 */
public class OpponentDrawnProgressCard extends ForwardedRequest {
    private ProgressCardType card;

    public static OpponentDrawnProgressCard newInstance(ProgressCardType card, String username) {
        OpponentDrawnProgressCard drawProgressCard = new OpponentDrawnProgressCard();
        drawProgressCard.username = username;
        drawProgressCard.card = card;
        drawProgressCard.universal = false;
        return drawProgressCard;
    }

    public ProgressCardType getCard() {
        return card;
    }
}
