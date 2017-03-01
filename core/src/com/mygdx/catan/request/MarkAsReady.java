package com.mygdx.catan.request;

/**
 * Request to the server to mark the client as "ready"
 * to start the game.
 */
public class MarkAsReady extends ForwardedRequest {

    public static MarkAsReady newInstance(String username) {
        MarkAsReady request = new MarkAsReady();
        request.username = username;
        request.universal = true;
        return request;
    }
}
