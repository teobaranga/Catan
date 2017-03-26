package com.mygdx.catan.request;

import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ProgressCardType;

/**
 * Created by emmaakouri on 2017-03-26.
 */
public class DrawProgressCard extends ForwardedRequest {
    private ProgressCardType card;

    public static DrawProgressCard newInstance(ProgressCardType card) {
        DrawProgressCard drawProgressCard = new DrawProgressCard();
        drawProgressCard.username = CatanGame.account.getUsername();
        drawProgressCard.card = card;
        return drawProgressCard;
    }

    public ProgressCardType getCard() {
        return card;
    }
}
