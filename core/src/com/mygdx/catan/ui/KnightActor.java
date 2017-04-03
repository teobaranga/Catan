package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.session.GamePieces;
import com.mygdx.catan.ui.window.KnightActionsWindow;

public class KnightActor extends ImageButton {

    private final TextureRegionDrawable[] activeStates, inactiveStates;

    private final Image bgColor;

    private final Knight knight;

    private KnightActionsWindow actionWindow;

    public KnightActor(Knight knight) {
        super(new ImageButtonStyle());

        GamePieces gamePieces = GamePieces.getInstance();

        this.knight = knight;
        activeStates = gamePieces.knightActive;
        inactiveStates = gamePieces.knightInactive;

        // Set the background color
        bgColor = new Image(gamePieces.knightBg);
        switch (knight.getOwner().getColor()) {
            case WHITE:
                bgColor.setColor(Color.WHITE);
                break;
            case BLUE:
                bgColor.setColor(Color.BLUE);
                break;
            case RED:
                bgColor.setColor(Color.RED);
                break;
            case ORANGE:
                bgColor.setColor(Color.ORANGE);
                break;
            case YELLOW:
                bgColor.setColor(Color.YELLOW);
                break;
        }

        refresh();
        setSize(getPrefWidth(), getPrefHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (bgColor != null)
            bgColor.draw(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (bgColor != null)
            bgColor.setSize(width, height);
    }

    @Override
    public void setOrigin(float originX, float originY) {
        super.setOrigin(originX, originY);
        if (bgColor != null)
            bgColor.setOrigin(originX, originY);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        if (bgColor != null)
            bgColor.setPosition(x, y);
    }

    public Knight getKnight() {
        return knight;
    }

    public void refresh() {
        // Set the correct image
        TextureRegionDrawable[] imageArray = knight.isActive() ? activeStates : inactiveStates;
        getStyle().imageUp = imageArray[knight.getStrength() - 1];
        setStyle(getStyle());
    }

    public KnightActionsWindow displayActions() {
        if (getStage() != null) {
            // Create the window if it's not created
            if (actionWindow == null)
                actionWindow = new KnightActionsWindow(this);

            actionWindow.setPosition(getX() + getWidth(), getTop());

            // Add the window if not already added
            if (!actionWindow.hasParent())
                getStage().addActor(actionWindow);

            return actionWindow;
        }
        return null;
    }
}
