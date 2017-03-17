package com.mygdx.catan.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

public class ResourceWidget extends WidgetGroup {

    private static final int BUTTON_SIZE = 17;
    private static final int BUTTON_PADDING = 15;
    private static final float SCALE = 0.75f;

    /** The image displaying the resource */
    private final Image image;

    /** Label containing the name of the resource and the count */
    private final Label label;

    private final ResourceKind kind;

    /** The number to be displayed next to the image */
    private int count;

    public ResourceWidget(ResourceKind kind, Skin skin) {
        // Add the image
        this.kind = kind;
        switch (kind) {
            case GRAIN:
                image = new Image(skin, "wheat");
                break;
            default:
                image = new Image(skin, kind.name().toLowerCase());
                break;
        }
        image.setSize(image.getWidth() * SCALE, image.getHeight() * SCALE);
        addActor(image);

        // Add the label
        label = new Label("X", skin);
        refreshCount();
        label.setPosition(getPrefWidth() / 2f - label.getPrefWidth() / 2f, 0);
        addActor(label);

        // Shift the image up
        image.setPosition(0, label.getHeight());

        // Add the "remove resources" button
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

        // Add the "add resources" button
        TextButton addButton = new TextButton("+", skin);
        addButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        addButton.setPosition(getPrefWidth() - BUTTON_PADDING, label.getHeight(), Align.bottomRight);
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                count++;
                refreshCount();
            }
        });
        addActor(addButton);

//        debug();
    }

    @Override
    public float getPrefWidth() {
        return image.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return image.getHeight() + label.getPrefHeight();
    }

    public ResourceMap getCount() {
        final ResourceMap resourceMap = new ResourceMap();
        resourceMap.add(kind, count);
        return resourceMap;
    }

    private void refreshCount() {
        String resName = kind.name();
        label.setText(resName.substring(0, 1).toUpperCase() + resName.substring(1).toLowerCase() + ": " + count);
    }
}
