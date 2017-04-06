package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ChooseDraw extends Window {

    ChooseDrawListener chooseDrawListener;
    
    public ChooseDraw(String title, Skin skin) {
        super(title, skin);
        
        TextButton accept = new TextButton("Yes", skin);
        TextButton reject = new TextButton("No", skin);
        
        accept.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (chooseDrawListener != null) {
                    chooseDrawListener.onKindChosen(true);
                    remove();
                } 
            }
        });
        
        reject.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (chooseDrawListener != null) {
                    chooseDrawListener.onKindChosen(false);
                    remove();
                } 
            }
        });
        
        add(accept).pad(10);
        add(reject).pad(10);
        pack();
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
        
    }

    public void setChooseDrawListener(ChooseDrawListener listener) {
        chooseDrawListener = listener;
    }

    public interface ChooseDrawListener {
        void onKindChosen(Boolean kind);
    }
}
