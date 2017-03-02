package com.mygdx.catan.response;

import com.mygdx.catan.game.Game;

public class GameResponse implements Response {

    /**
     * Game returned by this GameResponse.
     * Can be null (eg. when looking for a random game and none is found)
     */
    private Game game;

    public static GameResponse newInstance(Game game) {
        final GameResponse response = new GameResponse();
        response.game = game;
        return response;
    }

    /** Get the game. Can be null. */
    public Game getGame() {
        return game;
    }
}
