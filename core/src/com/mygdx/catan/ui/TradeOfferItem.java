package com.mygdx.catan.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;

public class TradeOfferItem extends WidgetGroup {

    private static final float SCALE = 0.60f;

    private static final float IMAGE_WIDTH = 128f * SCALE;

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
        final ResourceKind[] array = offer.keySet().toArray(new ResourceKind[offer.size()]);
        for (int i = array.length - 1; i >= 0; i--) {
            ResourceKind resKind = array[i];

            // Skip over 0 resources
            if (offer.get(resKind) == 0)
                continue;

            Image image;
            switch (resKind) {
                case GRAIN:
                    image = new Image(skin, "wheat");
                    break;
                default:
                    final TextureRegion region = skin.getRegion(resKind.name().toLowerCase());
                    region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    image = new Image(region);
                    break;
            }
            image.setSize(image.getWidth() * SCALE, image.getHeight() * SCALE);
            image.setY(index * image.getWidth() * SCALE);
            addActor(image);
            resources.add(image);

            final Label label = new Label(offer.get(resKind).toString(), skin);
            label.setPosition(image.getWidth() + 10 - label.getWidth() / 2f,
                    image.getY() + image.getHeight() / 2f - label.getHeight() / 2f);
            counts.add(label);
            addActor(label);

            index++;
        }

        if (resources.isEmpty())
            return;

        label.setY(getPrefHeight() - label.getHeight());
        addActor(label);

        accept.setWidth(accept.getWidth() + 40);
        accept.setY(getPrefHeight() - label.getHeight() - accept.getHeight());
        addActor(accept);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(IMAGE_WIDTH + 30f, label.getWidth());
    }

    @Override
    public float getPrefHeight() {
        return label.getHeight() + accept.getHeight() + IMAGE_WIDTH / 2f + IMAGE_WIDTH * SCALE * resources.size();
    }
}
