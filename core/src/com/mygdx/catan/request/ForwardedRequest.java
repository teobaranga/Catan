package com.mygdx.catan.request;

/**
 * Tagging interface indicating that a request sent by a peer
 * should be forwarded to the other peers of a game.
 */
public abstract class ForwardedRequest {

    /** The username of the sender */
    public String username = null;
}
