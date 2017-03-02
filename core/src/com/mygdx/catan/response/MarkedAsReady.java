package com.mygdx.catan.response;

/**
 * Response from the server indicating the player was marked
 * as ready.
 */
public class MarkedAsReady implements Response {

    /** Username of the player that was marked as ready */
    private String username;

    /** Flag indicating whether the game is now ready to start */
    private boolean readyToStart;

    public static MarkedAsReady newInstance(String username, boolean readyToStart) {
        final MarkedAsReady response = new MarkedAsReady();
        response.username = username;
        response.readyToStart = readyToStart;
        return response;
    }

    /** Get the username of the player that was marked as ready */
    public String getUsername() {
        return username;
    }

    /**
     * Check if the game is ready to start after the player was marked as ready.
     */
    public boolean isReadyToStart() {
        return readyToStart;
    }
}
