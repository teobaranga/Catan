package com.mygdx.catan.request.game;

public class LoadGame {

    private String username;

    /** The ID of the game being loaded, aka the name */
    private String gameId;

    public static LoadGame newInstance(String username, String gameId) {
        LoadGame loadGame = new LoadGame();
        loadGame.username = username;
        loadGame.gameId = gameId;
        return loadGame;
    }

    public String getUsername() {
        return username;
    }

    public String getGameId() {
        return gameId;
    }
}
