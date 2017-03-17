package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeOfferItem extends WidgetGroup {

    private static final float SCALE = 0.60f;

    private static final int PAD = 10;

    /** Label displaying the name of the offering player */
    private final Label label;

    private final TextButton accept;

    private final List<Image> resources;
    private final List<Label> counts;

    public TradeOfferItem(String player, ResourceMap offer, Skin skin) {
        resources = new ArrayList<>(offer.size());
        counts = new ArrayList<>(offer.size());
        label = new Label(player, skin);
        accept = new TextButton("Accept", skin);

        int index = 0;
        for (Map.Entry<ResourceKind, Integer> entry : offer.entrySet()) {
            // Skip over 0 resources
//            if (entry.getValue() == 0)
//                continue;

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
            image.setX(index * image.getWidth() * SCALE);
            addActor(image);
            resources.add(image);

            final Label label = new Label(entry.getValue().toString(), skin);
            label.setX(image.getX() + image.getWidth() / 2f - label.getWidth() / 2f);
            counts.add(label);
            addActor(label);

            // Reposition the image, taking into account the label
            image.setY(label.getPrefHeight());

            index++;
        }

        if (resources.isEmpty())
            return;

        label.setY(resources.get(0).getHeight() + counts.get(0).getHeight() + PAD);
        addActor(label);

        accept.setWidth(accept.getWidth() + 40);
        accept.setPosition(getPrefWidth(), getPrefHeight(), Align.topRight);
        addActor(accept);
    }

    @Override
    public float getPrefWidth() {
        return resources.isEmpty() ? 0 : Math.max(resources.get(0).getWidth() / 2f + resources.get(0).getWidth() * resources.size() * SCALE,
                label.getWidth() + accept.getWidth());
    }

    @Override
    public float getPrefHeight() {
        return resources.isEmpty() ? 0 : resources.get(0).getHeight() + accept.getHeight() + counts.get(0).getHeight() + PAD;
    }
}
