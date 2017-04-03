package com.mygdx.catan.response.game;

import com.mygdx.catan.game.Game;
import com.mygdx.catan.response.Response;

import java.util.List;

public class GameList implements Response {

    private List<Game> games;

    public static GameList newInstance(List<Game> games) {
        GameList gameList = new GameList();
        gameList.games = games;
        return gameList;
    }

    public List<Game> getGames() {
        return games;
    }
}
