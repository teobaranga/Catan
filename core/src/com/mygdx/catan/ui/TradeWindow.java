package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

public class TradeWindow extends CatanWindow {

    private static final String tradeRatio = "%d : 1";

    private final ImageButton oreOffer, woolOffer, brickOffer, wheatOffer, woodOffer;
    private final ImageButton oreRequest, woolRequest, brickRequest, wheatRequest, woodRequest;

    private final TextButton tradeButton;

    private boolean offerPicked, requestPicked;

    private ResourceKind offer, request;

    public TradeWindow(String title, ResourceMap tradeRatios, Skin skin, TradeListener listener) {
        super(title, skin);

        ChangeListener offerListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == oreOffer) {
                    offer = ResourceKind.ORE;
                } else if (actor == woolOffer) {
                    offer = ResourceKind.WOOL;
                } else if (actor == brickOffer) {
                    offer = ResourceKind.BRICK;
                } else if (actor == wheatOffer) {
                    offer = ResourceKind.GRAIN;
                } else if (actor == woodOffer) {
                    offer = ResourceKind.WOOD;
                }
                offerPicked = true;
                if (requestPicked)
                    tradeButton.setDisabled(false);
            }
        };

        ChangeListener requestListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == oreRequest) {
                    request = ResourceKind.ORE;
                } else if (actor == woolRequest) {
                    request = ResourceKind.WOOL;
                } else if (actor == brickRequest) {
                    request = ResourceKind.BRICK;
                } else if (actor == wheatRequest) {
                    request = ResourceKind.GRAIN;
                } else if (actor == woodRequest) {
                    request = ResourceKind.WOOD;
                }
                requestPicked = true;
                if (offerPicked)
                    tradeButton.setDisabled(false);
            }
        };

        final Table contentTable = new Table(skin);

        oreOffer = new ImageButton(skin, "ore");
        woolOffer = new ImageButton(skin, "wool");
        brickOffer = new ImageButton(skin, "brick");
        wheatOffer = new ImageButton(skin, "wheat");
        woodOffer = new ImageButton(skin, "wood");

        oreOffer.addListener(offerListener);
        woodOffer.addListener(offerListener);
        brickOffer.addListener(offerListener);
        wheatOffer.addListener(offerListener);
        woodOffer.addListener(offerListener);

        oreOffer.setDisabled(tradeRatios.get(ResourceKind.ORE) == 0);
        woolOffer.setDisabled(tradeRatios.get(ResourceKind.WOOL) == 0);
        brickOffer.setDisabled(tradeRatios.get(ResourceKind.BRICK) == 0);
        wheatOffer.setDisabled(tradeRatios.get(ResourceKind.GRAIN) == 0);
        woodOffer.setDisabled(tradeRatios.get(ResourceKind.WOOD) == 0);

        oreRequest = new ImageButton(skin, "ore");
        woolRequest = new ImageButton(skin, "wool");
        brickRequest = new ImageButton(skin, "brick");
        wheatRequest = new ImageButton(skin, "wheat");
        woodRequest = new ImageButton(skin, "wood");

        oreRequest.addListener(requestListener);
        woolRequest.addListener(requestListener);
        brickRequest.addListener(requestListener);
        wheatRequest.addListener(requestListener);
        woodRequest.addListener(requestListener);

        final ButtonGroup<ImageButton> offerGroup = new ButtonGroup<>(oreOffer, woolOffer, brickOffer, wheatOffer, woodOffer);
        offerGroup.setMinCheckCount(1);
        offerGroup.setMaxCheckCount(1);
        offerGroup.setUncheckLast(true);

        final ButtonGroup<ImageButton> requestGroup = new ButtonGroup<>(oreRequest, woolRequest, brickRequest, wheatRequest, woodRequest);
        requestGroup.setMinCheckCount(1);
        requestGroup.setMaxCheckCount(1);
        requestGroup.setUncheckLast(true);

        setMovable(true);
        contentTable.pad(50f);

        row();

        contentTable.add();
        contentTable.add(new Label("Ore", skin));
        contentTable.add(new Label("Wool", skin));
        contentTable.add(new Label("Brick", skin));
        contentTable.add(new Label("Wheat", skin));
        contentTable.add(new Label("Wood", skin));

        contentTable.row();

        contentTable.add(new Label("Offer", skin));

        contentTable.add(oreOffer).width(100).height(100);
        contentTable.add(woolOffer).width(100).height(100);
        contentTable.add(brickOffer).width(100).height(100);
        contentTable.add(wheatOffer).width(100).height(100);
        contentTable.add(woodOffer).width(100).height(100);

        contentTable.row();

        contentTable.add(new Label("Trade Ratio:", skin)).padRight(20).padBottom(20);

        if (oreOffer.isDisabled()) {
            contentTable.add();
        } else {
            contentTable.add(new Label(String.format(tradeRatio, tradeRatios.get(ResourceKind.ORE)), skin)).top();
        }
        if (woolOffer.isDisabled()) {
            contentTable.add();
        } else {
            contentTable.add(new Label(String.format(tradeRatio, tradeRatios.get(ResourceKind.WOOL)), skin)).top();
        }
        if (brickOffer.isDisabled()) {
            contentTable.add();
        } else {
            contentTable.add(new Label(String.format(tradeRatio, tradeRatios.get(ResourceKind.BRICK)), skin)).top();
        }
        if (wheatOffer.isDisabled()) {
            contentTable.add();
        } else {
            contentTable.add(new Label(String.format(tradeRatio, tradeRatios.get(ResourceKind.GRAIN)), skin)).top();
        }
        if (woodOffer.isDisabled()) {
            contentTable.add();
        } else {
            contentTable.add(new Label(String.format(tradeRatio, tradeRatios.get(ResourceKind.WOOD)), skin)).top();
        }

        contentTable.row();

        contentTable.add(new Label("Request", skin));

        contentTable.add(oreRequest).width(100).height(100);
        contentTable.add(woolRequest).width(100).height(100);
        contentTable.add(brickRequest).width(100).height(100);
        contentTable.add(wheatRequest).width(100).height(100);
        contentTable.add(woodRequest).width(100).height(100);

        contentTable.row();

        tradeButton = new TextButton("Trade", skin);
        tradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onTrade(offer, request, tradeRatios.get(offer));
            }
        });
        tradeButton.pad(10);
        tradeButton.setDisabled(true);
        contentTable.add(tradeButton).colspan(6).right().padTop(30);

        add(contentTable);

        setSize(getPrefWidth(), getPrefHeight());

        // Re-position window in the middle
        setPosition((int) (Gdx.graphics.getWidth() / 2 - getWidth() / 2),
                (int) (Gdx.graphics.getHeight() / 2 - getHeight() / 2));
    }

    public interface TradeListener {
        void onTrade(ResourceKind offer, ResourceKind request, int tradeRatio);
    }
}