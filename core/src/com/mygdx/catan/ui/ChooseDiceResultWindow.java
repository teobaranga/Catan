package com.mygdx.catan.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.CatanGame;

public class ChooseDiceResultWindow extends Window {

    private ChooseDiceResultsListener chooseDiceResultListener;
    private List<Table> redDieOptions;
    private List<Table> yellowDieOptions;
    private int diceOptionSize = 60;
    private TextButton confirmButton;
    private int chosenRedDice, chosenYellowDice;
    
    private Drawable redBackground;
    private Drawable redBackgroundClicked;
    
    private Drawable yellowBackground;
    private Drawable yellowBackgroundClicked;
    
    public ChooseDiceResultWindow(String title, Skin skin) {
        super(title, skin);
        
        setWidth(diceOptionSize * 6 + 7 * 10);
        setHeight(2 * diceOptionSize + 100);
        
        redDieOptions = new ArrayList<>();
        yellowDieOptions = new ArrayList<>();
        
        yellowBackgroundClicked = CatanGame.skin.newDrawable("white", Color.valueOf("999900"));
        yellowBackground = CatanGame.skin.newDrawable("white", Color.YELLOW);
        redBackgroundClicked = CatanGame.skin.newDrawable("white", Color.valueOf("990000"));
        redBackground = CatanGame.skin.newDrawable("white", Color.RED);
        
        for (int i = 1; i < 7; i++) {
            Table redDiceOption = setupRedDiceOption(i);
            redDieOptions.add(redDiceOption);
            add(redDiceOption).padLeft(5).padTop(10).padRight(5).size(diceOptionSize, diceOptionSize);
        }
        
        row();
        
        for (int i = 1; i < 7; i++) {
            Table yellowDiceOption = setupYellowDiceOption(i);
            yellowDieOptions.add(yellowDiceOption);
            add(yellowDiceOption).padLeft(5).padTop(10).padRight(5).padBottom(10).size(diceOptionSize, diceOptionSize);
        }
        
        confirmButton = new TextButton("confirm", skin);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (chooseDiceResultListener != null && chosenRedDice != 0 && chosenYellowDice != 0) {
                    chooseDiceResultListener.onDiceChosen(chosenRedDice, chosenYellowDice);
                    remove();
                } 
            }
        });
        
        row();
        add(confirmButton).colspan(6).padTop(10).align(Align.right);
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
        
    }
    
    private Table setupRedDiceOption(int diceNumber) {
        Table redDiceOption = new Table();
        redDiceOption.setBackground(redBackground);
        
        redDiceOption.add(new Label("" + diceNumber, CatanGame.skin));
        
        redDiceOption.setTouchable(Touchable.enabled); 
        redDiceOption.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (chosenRedDice == diceNumber) {
                    System.out.println("discard choice of " + diceNumber);
                    chosenRedDice = 0;
                    enableAllTouchablesRed();
                } else {
                    System.out.println("choose " + diceNumber);
                    chosenRedDice = diceNumber;
                    disableAllTouchablesRed();
                    redDiceOption.setTouchable(Touchable.enabled);
                    redDiceOption.setBackground(redBackground);
                }
            }
        });
        
        return redDiceOption;
    }
    
    private Table setupYellowDiceOption(int diceNumber) {
        Table yellowDiceOption = new Table();
        yellowDiceOption.setBackground(yellowBackground);
        
        yellowDiceOption.add(new Label("" + diceNumber, CatanGame.skin));
        
        yellowDiceOption.setTouchable(Touchable.enabled); 
        yellowDiceOption.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (chosenYellowDice == diceNumber) {
                    System.out.println("discard choice of " + diceNumber);
                    chosenYellowDice = 0;
                    enableAllTouchablesYellow();
                } else {
                    System.out.println("choose " + diceNumber);
                    chosenYellowDice = diceNumber;
                    disableAllTouchablesYellow();
                    yellowDiceOption.setTouchable(Touchable.enabled);
                    yellowDiceOption.setBackground(yellowBackground);
                }
            }
        });
        
        return yellowDiceOption;
    }
    
    private void disableAllTouchablesRed() {
        for (Table t : redDieOptions) {
            t.setBackground(redBackgroundClicked);
            t.setTouchable(Touchable.disabled);
        }
    }
    
    private void enableAllTouchablesRed() {
        for (Table t : redDieOptions) {
            t.setBackground(redBackground);
            t.setTouchable(Touchable.enabled);
        }
    }
    
    private void disableAllTouchablesYellow() {
        for (Table t : yellowDieOptions) {
            t.setBackground(yellowBackgroundClicked);
            t.setTouchable(Touchable.disabled);
        }
    }
    
    private void enableAllTouchablesYellow() {
        for (Table t : yellowDieOptions) {
            t.setBackground(yellowBackground);
            t.setTouchable(Touchable.enabled);
        }
    }
    
    public void setChooseDiceResultsListener(ChooseDiceResultsListener listener) {
        chooseDiceResultListener = listener;
    } 

    public interface ChooseDiceResultsListener {
        void onDiceChosen(int dice1, int dice2);
    }
}
