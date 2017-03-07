package com.mygdx.catan.request;

import com.mygdx.catan.CatanGame;

/** Request indicating that a player ended his/her turn */
public class EndTurn extends ForwardedRequest {

    public static EndTurn newInstance() {
        final EndTurn endTurn = new EndTurn();
        endTurn.username = CatanGame.account.getUsername();
        return endTurn;
    }
}
