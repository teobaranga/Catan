package com.mygdx.catan.game;

import com.mygdx.catan.CatanGame;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static GameManager instance;

    private Game currentGame;

    private GameManager() {
    }

    public static GameManager getInstance() {
        if (instance == null)
            instance = new GameManager();
        return instance;
    }

    /** Create a test placeholder game so you can play with yourself :) */
    public static Game newPlaceholderGame() {
        Game game = new Game();

        List<Account> accounts = new ArrayList<>();
        accounts.add(CatanGame.account);

        game.session = SessionManager.newPlaceholderSession(accounts);

        return game;
    }

    public void setCurrentGame(Game game) {
        currentGame = game;
    }

    public Game getCurrentGame() {
        return currentGame;
    }
}
