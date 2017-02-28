package com.mygdx.catan.request;

/**
 * Requesting indicating that a player has left the lobby.
 */
public class LeaveGame extends ForwardedRequest {

    public static LeaveGame newInstance(String username) {
        LeaveGame request = new LeaveGame();
        request.username = username;
        return request;
    }
}
