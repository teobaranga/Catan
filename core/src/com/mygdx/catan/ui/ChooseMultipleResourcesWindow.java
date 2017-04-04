package com.mygdx.catan.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

public class ChooseMultipleResourcesWindow extends Window {

	ChooseCardsListener chooseCardsListener;
	List<ResourceWidget> widgets;
	TextButton confirm;
    
    private final float width, height;
	
	public ChooseMultipleResourcesWindow(String title, Skin skin, EnumMap<ResourceKind, Integer> cards, int number) {
		super(title, skin);
        
        widgets = new ArrayList<>();
        int tableWidth = 0;
        
        // create widget for each kind
        for (Entry<ResourceKind, Integer> entry : cards.entrySet()) {
        	if (entry.getValue() <= 0) {continue;}
            
        	ResourceWidget widget = new ResourceWidget(entry.getKey(), skin);
        	widget.setMaxResource(entry.getValue());
        	widgets.add(widget);
        	add(widget).padTop(10);
        	
        	tableWidth += widget.getPrefWidth() + 10;
        }

        // create button to confirm choice
        confirm = new TextButton("confirm", skin);
        confirm.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
                if (chooseCardsListener != null && getResourceCount() == number) {
					chooseCardsListener.onMapChosen(getResources());
                    remove();
                } 
			}
        });
        
        // set width and height
        width = tableWidth;
        int tableHeight = 100;
        if (widgets.size() > 0) {
        	tableHeight += widgets.get(0).getPrefHeight();
        }
        height = tableHeight;
        setWidth(width);
        setHeight(height);

        // add confirm button
        row();
        add(confirm).colspan(widgets.size()).padTop(10).align(Align.right);
        
        // set position on screen
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
        
        // enable moving the window
        setMovable(true);
	}
	
	/**
     * Get the total resources represented in the widgets.
     */
    private ResourceMap getResources() {
        final ResourceMap resourceMap = new ResourceMap();
        for (ResourceWidget widget : widgets) {
            resourceMap.add(widget.getCount());
        }
        return resourceMap;
    }
	
    /**
     * Get the total number of resources chosen
     * */
    private int getResourceCount() {
    	int resourceCount = 0;
    	for(ResourceWidget widget : widgets) {
    		resourceCount += widget.getCount().get(widget.getKind());
    	}
    	return resourceCount;
    }
    
	public void setChooseCardListener(ChooseCardsListener listener) {
		chooseCardsListener = listener;
	}

	public interface ChooseCardsListener {
        void onMapChosen(ResourceMap map);
    }
	
}
