package com.mygdx.catan.request;

/**
 * Request indicating an intention to start a game.
 */
public class StartGame extends ForwardedRequest {

    public static StartGame newInstance(String username) {
        final StartGame request = new StartGame();
        request.username = username;
        request.universal = true;
        return request;
    }
}
