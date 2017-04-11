package com.mygdx.catan.ui;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.RobberType;

public class ChooseFromEnumCollectionWindow<T extends Enum<T>> extends Window {

	ChooseCardListener<T> chooseTypeListener;
    
    private final float width, height;
	
	public ChooseFromEnumCollectionWindow(String title, Skin skin, Collection<T> cards) {
		super(title, skin);
		
		// create content table for buttons
        final Table buttonTable = new Table(skin);
        
        // finds the number of progress card types in cards
        int sizeOfCardsByType = cards.size();
        
        // set width and height
        width = sizeOfCardsByType * 150 + (sizeOfCardsByType * 10);
        height = 1f / 4f * Gdx.graphics.getHeight();
        setWidth(width);
        setHeight(height);
        
        // create button with listener for each kind
        for (T enumType : cards) {
            TextButton kindButton = new TextButton(enumType.toString().toLowerCase(), skin);
            
            kindButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (chooseTypeListener != null) {
                    	chooseTypeListener.onTypeChosen(enumType);
                        remove();
                    } 
                }
            });
            
            kindButton.setColor(new Color(Color.TEAL));
            
            // if enum has type resourceKind, set color according to its resourceKind color
            if (enumType instanceof ResourceKind) {
                ResourceKind resource = (ResourceKind) enumType;
                switch (resource) {
                case BRICK:
                    kindButton.setColor(CatanGame.skin.getColor("brick"));
                    break;
                case CLOTH:
                    kindButton.setColor(CatanGame.skin.getColor("cloth"));
                    break;
                case COIN:
                    kindButton.setColor(CatanGame.skin.getColor("coin"));
                    break;
                case GRAIN:
                    kindButton.setColor(CatanGame.skin.getColor("grain"));
                    break;
                case ORE:
                    kindButton.setColor(CatanGame.skin.getColor("ore"));
                    break;
                case PAPER:
                    kindButton.setColor(CatanGame.skin.getColor("paper"));
                    break;
                case WOOD:
                    kindButton.setColor(CatanGame.skin.getColor("wood"));
                    break;
                case WOOL:
                    kindButton.setColor(CatanGame.skin.getColor("wool"));
                    break;
                default:
                    break;
                }
            } else if (enumType instanceof RobberType) {
                RobberType type = (RobberType) enumType;
                switch (type) {
                    case ROBBER:
                        kindButton.setColor(Color.BLACK);
                        kindButton.setText("Robber");
                        break;
                    case PIRATE:
                        kindButton.setColor(Color.NAVY);
                        kindButton.setText("Pirate");
                        break;
                    default:
                        break;
                }
            }
            
            buttonTable.add(kindButton).padLeft(5).padRight(5).padTop(10).padBottom(10).size(130, height - 40);
        }
        
        add(buttonTable);
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
	}
	
	public void setChooseCardListener(ChooseCardListener<T> listener) {
		chooseTypeListener = listener;
	}

	public interface ChooseCardListener<T extends Enum<T>> {
        void onTypeChosen(T type);
    }
	
}
