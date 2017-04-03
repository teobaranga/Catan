package com.mygdx.catan.ui.window.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.ui.CatanWindow;

public class BrowseGamesWindow extends CatanWindow {

    private final List<Game> gameList;

    private GameListener gameListener;

    public BrowseGamesWindow() {
        super("Browse Games", CatanGame.skin);

        gameList = new List<>(CatanGame.skin);

        add(gameList).pad(20).width(300).row();

        TextButton loadGame = new TextButton("Load Game", CatanGame.skin);
        loadGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameListener != null) {
                    gameListener.onGameSelected(gameList.getSelected());
                    close();
                }
            }
        });
        add(loadGame).right().pad(20);

        setSize(400, 200);
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
    }

    public void setGames(java.util.List<Game> games) {
        gameList.setItems(games.toArray(new Game[games.size()]));
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    public interface GameListener {
        void onGameSelected(Game game);
    }
}
