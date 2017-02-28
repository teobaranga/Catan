package com.mygdx.catan.response;

/**
 * Response from the server indicating the player was marked
 * as ready.
 */
public class MarkedAsReady implements Response {

    public String username;

    public static MarkedAsReady newInstance(String username) {
        final MarkedAsReady response = new MarkedAsReady();
        response.username = username;
        return response;
    }
}
