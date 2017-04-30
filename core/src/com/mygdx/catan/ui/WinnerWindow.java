package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.player.Player;



public class WinnerWindow extends CatanWindow {

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
                TextTable.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
                break;
            case ORANGE:
                TextTable.setBackground(CatanGame.skin.newDrawable("white", Color.ORANGE));
                break;
            case RED:
                TextTable.setBackground(CatanGame.skin.newDrawable("white", Color.RED));
                break;
            case WHITE:
                TextTable.setBackground(CatanGame.skin.newDrawable("white", Color.WHITE));
                break;
            case YELLOW:
                TextTable.setBackground(CatanGame.skin.newDrawable("white", Color.YELLOW));
                break;
            default:
                break;
        }
        TextTable.add(winnerLabel).padLeft(5).padRight(5).padTop(10).padBottom(20).row();

        Button quitButton = new TextButton("Quit", CatanGame.skin);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
        }});

        add(TextTable).size(100,40).pad(20).row();
        add(quitButton).row();

        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        setMovable(true);

    }

}
