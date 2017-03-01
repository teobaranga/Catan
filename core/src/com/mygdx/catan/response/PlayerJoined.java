package com.mygdx.catan.response;

/**
 * Response sent to the other players of a game when a certain
 * player has joined them.
 */
public class PlayerJoined {

    /** Username of the player that joined */
    public String username;

    public static PlayerJoined newInstance(String username) {
        final PlayerJoined response = new PlayerJoined();
        response.username = username;
        return response;
    }
}
