package com.mygdx.catan.response;

/**
 * Response sent to the other players of a game when a certain
 * player leaves.
 */
public class PlayerLeft {

    /** Username of the player that left */
    public String username;

    public static PlayerLeft newInstance(String username) {
        final PlayerLeft response = new PlayerLeft();
        response.username = username;
        return response;
    }
}
