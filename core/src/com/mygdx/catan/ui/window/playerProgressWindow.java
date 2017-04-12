package com.mygdx.catan.ui.window;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.ui.ChoosePlayerWindow;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import java.util.ArrayList;

/**
 * Created by amandaivey on 4/9/17.
 */

public class playerProgressWindow extends Window {

    ChoosePlayerWindow.ChoosePlayerListener choosePlayerListener;

    private final float width, height;

    public playerProgressWindow(String title, Skin skin, ArrayList<Player> opponents) {
        super(title, skin);

        // create content table for buttons
        final Table buttonTable = new Table(skin);

        // set width and height
        width = opponents.size() * 100 + (opponents.size() + 1) * 10;
        height = 1f / 5f * Gdx.graphics.getHeight();
        setWidth(width);
        setHeight(height);

        // create button with listener for each opponent player
        for (Player opponent : opponents) {
            TextButton playerButton = new TextButton(opponent.getAccount().getUsername(), skin);
            switch (opponent.getColor()) {
                case BLUE:
                    playerButton.setColor(Color.BLUE);
                    break;
                case ORANGE:
                    playerButton.setColor(Color.ORANGE);
                    break;
                case RED:
                    playerButton.setColor(Color.RED);
                    break;
                case WHITE:
                    playerButton.setColor(Color.WHITE);
                    break;
                case YELLOW:
                    playerButton.setColor(Color.YELLOW);
                    break;
                default:
                    break;
            }

            playerButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (choosePlayerListener != null) {
                        choosePlayerListener.onPlayerChoosed(opponent);
                        remove();
                    }
                }
            });

            buttonTable.add(playerButton).padLeft(5).padRight(5).padTop(10).padBottom(10).size(90, height - 40);
        }

        add(buttonTable);

        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);

        // enable moving the window
        setMovable(true);
    }


    public void setChoosePlayerListener(ChoosePlayerWindow.ChoosePlayerListener listener) {
        choosePlayerListener = listener;
    }

    public interface ChoosePlayerListener {
        void onPlayerChoosed(Player chosenPlayer);
    }
}
