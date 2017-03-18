package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.enums.ProgressCardKind;

public class ChooseProgressCardKindWindow extends Window {

	ChooseProgressCardKindListener chooseKindListener;
    
    private final float width, height;
	
	public ChooseProgressCardKindWindow(String title, Skin skin) {
		super(title, skin);
		
		// create content table for buttons
        final Table buttonTable = new Table(skin);
        
        // set width and height
        width = 330;
        height = 1f / 5f * Gdx.graphics.getHeight();
        setWidth(width);
        setHeight(height);
        
        // create button with listener for each kind
        for (ProgressCardKind kind : ProgressCardKind.values()) {
            TextButton kindButton = new TextButton(kind.toString().toLowerCase(), skin);
            switch (kind) {
			case POLITICS:
				kindButton.setColor(Color.BLUE);
				break;
			case SCIENCE:
				kindButton.setColor(Color.GREEN);
				break;
			case TRADE:
				kindButton.setColor(Color.ORANGE);
				break;
			default:
				break;
            }
            
            kindButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (chooseKindListener != null) {
                    	chooseKindListener.onKindChosen(kind);
                        remove();
                    } 
                }
            });
            
            buttonTable.add(kindButton).padLeft(5).padRight(5).padTop(10).padBottom(10).size(90, height - 40);
        }
        
        add(buttonTable);
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
	}
	
	public void setChooseProgressCardKindListener(ChooseProgressCardKindListener listener) {
		chooseKindListener = listener;
	}

	public interface ChooseProgressCardKindListener {
        void onKindChosen(ProgressCardKind kind);
    }
	
}
