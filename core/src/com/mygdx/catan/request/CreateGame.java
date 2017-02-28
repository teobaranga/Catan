package com.mygdx.catan.request;

import com.mygdx.catan.account.Account;

/**
 * Request indicating an intention to create a new game.
 */
public class CreateGame {

    /** The account of the player sending this request */
    public Account account;
}
