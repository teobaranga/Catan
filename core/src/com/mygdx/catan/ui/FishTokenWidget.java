package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.FishTokenType;

public class FishTokenWidget extends WidgetGroup {

    private static final int BUTTON_SIZE = 17;
    private static final int BUTTON_PADDING = 15;

    /** The table displaying the resource */
    private final Table tokenTable;

    /** Label containing the name of the resource and the count */
    private final Label label;

    private final FishTokenType kind;

    /** The number to be displayed next to the table */
    private int count;

    private int maxToken;

    FishTokenWidget(FishTokenType kind, Skin skin, int maxToken) {
        this.kind = kind;
        this.maxToken = maxToken;
        // Add the table
        tokenTable = new Table(CatanGame.skin);
        tokenTable.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        tokenTable.setSize(80, 80);
        switch (kind) {
            case ONE_FISH:
                tokenTable.add(new Label("1 Fish\nToken", CatanGame.skin));
                break;
            case TWO_FISH:
                tokenTable.add(new Label("2 Fish\nToken", CatanGame.skin));
                break;
            case THREE_FISH:
                tokenTable.add(new Label("3 Fish\nToken", CatanGame.skin));
                break;
            default:
                break;
        }
        addActor(tokenTable);

        // Add the label
        label = new Label("0", skin);
        refreshCount();
        label.setPosition(getPrefWidth() / 2f - label.getPrefWidth() / 2f, 0);
        addActor(label);

        // Position
        tokenTable.setPosition(0, label.getHeight() + 30);

        // Add the "remove token" button
        TextButton removeButton = new TextButton("-", skin);
        removeButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        removeButton.setPosition(BUTTON_PADDING, label.getHeight());
        removeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (count > 0) {
                    count--;
                    refreshCount();
                }
            }
        });
        addActor(removeButton);

        // Add the "add token" button
        TextButton addButton = new TextButton("+", skin);
        addButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        addButton.setPosition(getPrefWidth() - BUTTON_PADDING, label.getHeight(), Align.bottomRight);
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (count < maxToken) {
                    count++;
                    refreshCount();
                }
            }
        });
        addActor(addButton);

    }

    @Override
    public float getPrefWidth() {
        return tokenTable.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return tokenTable.getHeight() + label.getPrefHeight();
    }

    public FishTokenType getKind() {
        return kind;
    }

    public void setMaxResource(int maxToken) {
        this.maxToken = maxToken;
    }

    public int getCount() {
        return count;
    }

    private void refreshCount() {
        String resName = kind.name();
        label.setText("" + count);
    }
}
