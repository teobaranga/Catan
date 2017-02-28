package com.mygdx.catan.request;

import com.mygdx.catan.account.Account;

/**
 * Request indicating an intention to join a random, already
 * existing game.
 */
public class JoinRandomGame extends ForwardedRequest {

    /** The account of the player sending this request */
    public Account account;

    public static JoinRandomGame newInstance(Account account) {
        final JoinRandomGame request = new JoinRandomGame();
        request.account = account;
        request.username = account.getUsername();
        return request;
    }
}
