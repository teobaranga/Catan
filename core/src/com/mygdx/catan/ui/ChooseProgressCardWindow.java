package com.mygdx.catan.ui;

import java.util.EnumMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.ProgressCardType;

public class ChooseProgressCardWindow extends Window {

	ChooseProgressCardListener chooseTypeListener;
    
    private final float width, height;
	
	public ChooseProgressCardWindow(String title, Skin skin, EnumMap<ProgressCardType, Integer> cards) {
		super(title, skin);
		
		// create content table for buttons
        final Table buttonTable = new Table(skin);
        
        // finds the number of progress card types in cards
        int sizeOfCardsByType = 0;
        for (Entry<ProgressCardType, Integer> entry : cards.entrySet()) {
        	if (entry.getValue() > 0) { sizeOfCardsByType++; }
        }
        
        // set width and height
        width = sizeOfCardsByType * 100 + (sizeOfCardsByType * 10);
        height = 1f / 5f * Gdx.graphics.getHeight();
        setWidth(width);
        setHeight(height);
        
     // create button with listener for each kind
        for (Entry<ProgressCardType, Integer> entry : cards.entrySet()) {
            TextButton kindButton = new TextButton(entry.getKey().toString().toLowerCase()+": "+entry.getValue().toString(), skin);
            switch (GameRules.getGameRulesInstance().getProgressCardKind(entry.getKey())) {
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
                    if (chooseTypeListener != null) {
                    	chooseTypeListener.onTypeChosen(entry.getKey());
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
	
	public void setChooseProgressCardListener(ChooseProgressCardListener listener) {
		chooseTypeListener = listener;
	}

	public interface ChooseProgressCardListener {
        void onTypeChosen(ProgressCardType kind);
    }
	
}
