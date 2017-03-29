package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.catan.gameboard.Knight;

public class KnightActor extends Image {

    private final TextureRegionDrawable[] activeStates, inactiveStates;

    private final Knight knight;

    public KnightActor(Knight knight, TextureAtlas.AtlasRegion[] activeStates, TextureAtlas.AtlasRegion[] inactiveStates) {
        super();

        this.activeStates = new TextureRegionDrawable[activeStates.length];
        this.inactiveStates = new TextureRegionDrawable[inactiveStates.length];

        for (int i = 0; i < activeStates.length; i++)
            this.activeStates[i] = new TextureRegionDrawable(activeStates[i]);
        for (int i = 0; i < inactiveStates.length; i++)
            this.inactiveStates[i] = new TextureRegionDrawable(inactiveStates[i]);

        this.knight = knight;

        // Set the correct image
        setDrawable(this.activeStates[knight.getStrength() - 1]);
        setSize(getPrefWidth(), getPrefHeight());
        setBounds(0, 0, getWidth(), getHeight());

        debug();
    }
}
