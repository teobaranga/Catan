package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.player.Player;

import java.util.ArrayList;


public class WinnerWindow extends Window {

    private final float width, height;

    public WinnerWindow(String title, Skin skin, Player opponent) {
        super(title, skin);

        final Table TextTable = new Table(skin);

        width = 300;
        height = 1f / 5f * Gdx.graphics.getHeight() ;
        setWidth(width);
        setHeight(height);

        Label winnerLabel = new Label(opponent.getAccount().getUsername(), skin);
        switch (opponent.getColor()) {
            case BLUE:
                winnerLabel.setColor(Color.BLUE);
                break;
            case ORANGE:
                winnerLabel.setColor(Color.ORANGE);
                break;
            case RED:
                winnerLabel.setColor(Color.RED);
                break;
            case WHITE:
                winnerLabel.setColor(Color.WHITE);
                break;
            case YELLOW:
                winnerLabel.setColor(Color.YELLOW);
                break;
            default:
                break;
        }
        TextTable.add(winnerLabel).padLeft(5).padRight(5).padTop(10).padBottom(20).size(90, height - 80);
        Button quitButton = new TextButton("Quit", CatanGame.skin);

        add(TextTable).row();
        add(quitButton).row();

        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        setMovable(true);

    }

}
