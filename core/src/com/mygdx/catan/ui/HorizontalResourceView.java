package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HorizontalResourceView extends WidgetGroup {

    private static final float SCALE = 0.5f;

    private final List<Image> resources;
    private final List<Label> counts;

    HorizontalResourceView(ResourceMap resourceMap, Skin skin) {
        resources = new ArrayList<>(resourceMap.size());
        counts = new ArrayList<>(resourceMap.size());

        int index = 0;
        for (Map.Entry<ResourceKind, Integer> entry : resourceMap.entrySet()) {
            // Skip over 0 resources
            if (entry.getValue() == 0)
                continue;

            Image image;
            switch (entry.getKey()) {
                case GRAIN:
                    image = new Image(skin, "wheat");
                    break;
                default:
                    final TextureRegion region = skin.getRegion(entry.getKey().name().toLowerCase());
                    region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    image = new Image(region);
                    break;
            }
            image.setSize(image.getWidth() * SCALE, image.getHeight() * SCALE);
            image.setX(index * image.getWidth() / 2f);
            addActor(image);
            resources.add(image);

            final Label label = new Label(entry.getValue().toString(), skin);
            label.setPosition(image.getX() + image.getWidth() / 2f - label.getWidth() / 2f, 0);
            addActor(label);
            counts.add(label);

            image.setY(label.getHeight());

            index++;
        }
    }

    @Override
    public float getPrefWidth() {
        return resources.isEmpty() ? 0 : resources.get(0).getWidth() / 2f * (resources.size() + 1);
    }

    @Override
    public float getPrefHeight() {
        return resources.isEmpty() ? 0 : resources.get(0).getHeight() + counts.get(0).getHeight();
    }
}
