package com.mygdx.catan.request;

/**
 * Tagging interface indicating that a request sent by a peer
 * should be forwarded to another targeted peer of a game.
 */
public abstract class TargetedRequest {

    /** The username of the sender */
    public String sender = null;
    
    /** The username of the target */
    public String target = null;
    
}
