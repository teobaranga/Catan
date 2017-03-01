package com.mygdx.catan.request;

/**
 * Tagging interface indicating that a request sent by a peer
 * should be forwarded to the other peers of a game.
 */
public abstract class ForwardedRequest {

    /** The username of the sender */
    public String username = null;

    /**
     * Flag indicating whether the request should be forwarded to all
     * players including the sender (when true) or excluding the sender (when false)
     */
    public boolean universal = false;
}
