package com.mygdx.catan.ui;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.ProgressCardType;

public class PlayProgressCardWindow extends CatanWindow {
    
    private PlayProgressCardListener playProgressCardListener;
    
    public PlayProgressCardWindow(String title, Skin skin, List<ProgressCardType> hand, GamePhase currentPhase, boolean isMyTurn) {
        super(title, skin);
        
        if (hand.isEmpty()) {
            add(new Label("Your hand is empty", skin)).pad(10);
        }
        
        // adds images of the card and the associated button that will play the card
        for (ProgressCardType type : hand) {
            Table cardTable = new Table(CatanGame.skin);
            
            // TODO: add image of correct progress card
            // cardTable.add(cardMap.get(type)).padBottom(10).row();
            
            TextButton playCard = new TextButton("Play " + type.toString().toLowerCase(), CatanGame.skin);
            playCard.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (playProgressCardListener != null) {
                        playProgressCardListener.onCardChosen(type);
                        remove();
                    } 
                }
            });
            playCard.setDisabled(!isLegalPlay(type, currentPhase, isMyTurn)); 
            
            cardTable.add(playCard).pad(5);
            
            add(cardTable).pad(5);
        }
        
        pack();
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
    }
    
    private boolean isLegalPlay(ProgressCardType type, GamePhase currentPhase, boolean isMyTurn) {
        boolean isLegal = isMyTurn && (currentPhase == GamePhase.TURN_FIRST_PHASE && type == ProgressCardType.ALCHEMIST) || (currentPhase == GamePhase.TURN_SECOND_PHASE && type != ProgressCardType.ALCHEMIST);
        
        return isLegal;
    }
    
    public void setPlayProgressCardListener(PlayProgressCardListener listener) {
        playProgressCardListener = listener;
    }
    
    public interface PlayProgressCardListener {
        void onCardChosen(ProgressCardType type);
    }
    
}
