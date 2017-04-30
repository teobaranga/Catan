package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

public class DomesticTradeWindow extends CatanWindow {

    private static final String tradeRatio = "%d : 1";

    private final ImageButton oreOffer, woolOffer, brickOffer, wheatOffer, woodOffer, clothOffer, coinOffer, paperOffer;
    private final ImageButton oreRequest, woolRequest, brickRequest, wheatRequest, woodRequest, clothRequest, coinRequest, paperRequest;
    private final Label oreLabel, woolLabel, brickLabel, wheatLabel, woodLabel, clothLabel, coinLabel, paperLabel;

    private final ButtonGroup<ImageButton> offerGroup, requestGroup;

    private final TextButton tradeButton;

    private boolean offerPicked, requestPicked;

    private ResourceKind offer, request;

    /** The listener to be notified when a trade has been completed */
    private DomesticTradeListener listener;

    private ResourceMap tradeRatios;

    /**
     * Create a new trade window
     *
     * @param title       Title of the window
     * @param skin        The skin used to theme this window
     * @param tradeRatios Map of resources to integers representing the trade ratios. Since the
     *                    ratio is always x:1, only one integer is necessary to represent how many
     *                    units of a certain resource are needed in exchange for any other.
     */
    public DomesticTradeWindow(String title, Skin skin, ResourceMap tradeRatios) {
        super(title, skin);

        // Create the listener for the offer buttons
        ChangeListener offerListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((ImageButton) actor).isChecked()) {
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
                    } else if (actor == clothOffer) {
                        offer = ResourceKind.CLOTH;
                    } else if (actor == coinOffer) {
                        offer = ResourceKind.COIN;
                    } else if (actor == paperOffer) {
                        offer = ResourceKind.PAPER;
                    }
                    System.out.println("offer picked " + offerPicked + " " + requestPicked);
                    offerPicked = true;
                    if (requestPicked)
                        tradeButton.setDisabled(false);
                }
            }
        };

        // Create the listener for the request buttons
        ChangeListener requestListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((ImageButton) actor).isChecked()) {
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
                    } else if (actor == clothRequest) {
                        request = ResourceKind.CLOTH;
                    } else if (actor == coinRequest) {
                        request = ResourceKind.COIN;
                    } else if (actor == paperRequest) {
                        request = ResourceKind.PAPER;
                    }
                    System.out.println("request picked " + offerPicked + " " + requestPicked);
                    requestPicked = true;
                    if (offerPicked)
                        tradeButton.setDisabled(false);
                }
            }
        };

        // Create the container
        final Table contentTable = new Table(skin);

        // Create the buttons used to pick the type of resource the player is willing to give
        oreOffer = new ImageButton(skin, "ore");
        woolOffer = new ImageButton(skin, "wool");
        brickOffer = new ImageButton(skin, "brick");
        wheatOffer = new ImageButton(skin, "wheat");
        woodOffer = new ImageButton(skin, "wood");
        clothOffer = new ImageButton(skin, "cloth");
        coinOffer = new ImageButton(skin, "coin");
        paperOffer = new ImageButton(skin, "paper");

        oreOffer.addListener(offerListener);
        woolOffer.addListener(offerListener);
        brickOffer.addListener(offerListener);
        wheatOffer.addListener(offerListener);
        woodOffer.addListener(offerListener);
        clothOffer.addListener(offerListener);
        coinOffer.addListener(offerListener);
        paperOffer.addListener(offerListener);

        // Create the buttons used to pick the type of resource the player would like to receive
        oreRequest = new ImageButton(skin, "ore");
        woolRequest = new ImageButton(skin, "wool");
        brickRequest = new ImageButton(skin, "brick");
        wheatRequest = new ImageButton(skin, "wheat");
        woodRequest = new ImageButton(skin, "wood");
        clothRequest = new ImageButton(skin, "cloth");
        coinRequest = new ImageButton(skin, "coin");
        paperRequest = new ImageButton(skin, "paper");

        oreRequest.addListener(requestListener);
        woolRequest.addListener(requestListener);
        brickRequest.addListener(requestListener);
        wheatRequest.addListener(requestListener);
        woodRequest.addListener(requestListener);
        clothRequest.addListener(requestListener);
        coinRequest.addListener(requestListener);
        paperRequest.addListener(requestListener);

        // Create a group for the offer buttons so that only one resource type can be selected at any given time
        offerGroup = new ButtonGroup<>(oreOffer, woolOffer, brickOffer, wheatOffer, woodOffer, clothOffer, coinOffer, paperOffer);
        offerGroup.setMinCheckCount(1);
        offerGroup.setMaxCheckCount(1);
        offerGroup.setUncheckLast(true);

        // Create a group for the request buttons so that only one resource type can be selected at any given time
        requestGroup = new ButtonGroup<>(oreRequest, woolRequest, brickRequest, wheatRequest, woodRequest, clothRequest, coinRequest, paperRequest);
        requestGroup.setMinCheckCount(1);
        requestGroup.setMaxCheckCount(1);
        requestGroup.setUncheckLast(true);

        // Trade window can be moved
        setMovable(true);
        contentTable.pad(50f);

        contentTable.add();
        contentTable.add(new Label("Ore", skin));
        contentTable.add(new Label("Wool", skin));
        contentTable.add(new Label("Brick", skin));
        contentTable.add(new Label("Wheat", skin));
        contentTable.add(new Label("Wood", skin));
        contentTable.add(new Label("Cloth", skin));
        contentTable.add(new Label("Coin", skin));
        contentTable.add(new Label("Paper", skin));

        contentTable.row();

        contentTable.add(new Label("Offer", skin));

        contentTable.add(oreOffer).width(100).height(100);
        contentTable.add(woolOffer).width(100).height(100);
        contentTable.add(brickOffer).width(100).height(100);
        contentTable.add(wheatOffer).width(100).height(100);
        contentTable.add(woodOffer).width(100).height(100);
        contentTable.add(clothOffer).width(100).height(100);
        contentTable.add(coinOffer).width(100).height(100);
        contentTable.add(paperOffer).width(100).height(100);

        contentTable.row();

        contentTable.add(new Label("Trade Ratio:", skin)).padRight(20).padBottom(20);

        contentTable.add(oreLabel = new Label("", skin)).top();
        contentTable.add(woolLabel = new Label("", skin)).top();
        contentTable.add(brickLabel = new Label("", skin)).top();
        contentTable.add(wheatLabel = new Label("", skin)).top();
        contentTable.add(woodLabel = new Label("", skin)).top();
        contentTable.add(clothLabel = new Label("", skin)).top();
        contentTable.add(coinLabel = new Label("", skin)).top();
        contentTable.add(paperLabel = new Label("", skin)).top();

        contentTable.row();

        contentTable.add(new Label("Request", skin));

        contentTable.add(oreRequest).width(100).height(100);
        contentTable.add(woolRequest).width(100).height(100);
        contentTable.add(brickRequest).width(100).height(100);
        contentTable.add(wheatRequest).width(100).height(100);
        contentTable.add(woodRequest).width(100).height(100);
        contentTable.add(clothRequest).width(100).height(100);
        contentTable.add(coinRequest).width(100).height(100);
        contentTable.add(paperRequest).width(100).height(100);

        contentTable.row();

        tradeButton = new TextButton("Trade", skin);
        tradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null)
                    listener.onTrade(offer, request, DomesticTradeWindow.this.tradeRatios.get(offer));
            }
        });
        tradeButton.pad(10);
        contentTable.add(tradeButton).colspan(9).right().padTop(30);

        add(contentTable);

        setSize(getPrefWidth(), getPrefHeight());

        // Re-position window in the middle
        setPosition((int) (Gdx.graphics.getWidth() / 2 - getWidth() / 2),
                (int) (Gdx.graphics.getHeight() / 2 - getHeight() / 2));

        updateTradeRatios(tradeRatios);
    }

    public void setDomesticTradeListener(DomesticTradeListener listener) {
        this.listener = listener;
    }

    public void updateTradeRatios(ResourceMap tradeRatios) {
        this.tradeRatios = tradeRatios;

        // Disable the types of resources that the player doesn't have enough of for a trade
        oreOffer.setDisabled(tradeRatios.get(ResourceKind.ORE) == 0);
        woolOffer.setDisabled(tradeRatios.get(ResourceKind.WOOL) == 0);
        brickOffer.setDisabled(tradeRatios.get(ResourceKind.BRICK) == 0);
        wheatOffer.setDisabled(tradeRatios.get(ResourceKind.GRAIN) == 0);
        woodOffer.setDisabled(tradeRatios.get(ResourceKind.WOOD) == 0);
        clothOffer.setDisabled(tradeRatios.get(ResourceKind.CLOTH) == 0);
        coinOffer.setDisabled(tradeRatios.get(ResourceKind.COIN) == 0);
        paperOffer.setDisabled(tradeRatios.get(ResourceKind.PAPER) == 0);


        // Display the trade ratio, if there is a valid one
        oreLabel.setText(oreOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.ORE)));
        woolLabel.setText(woolOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.WOOL)));
        brickLabel.setText(brickOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.BRICK)));
        wheatLabel.setText(wheatOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.GRAIN)));
        woodLabel.setText(woodOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.WOOD)));
        clothLabel.setText(clothOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.CLOTH)));
        coinLabel.setText(coinOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.COIN)));
        paperLabel.setText(paperOffer.isDisabled() ? "X" : String.format(tradeRatio, tradeRatios.get(ResourceKind.PAPER)));

        // Reset everything
        offerGroup.uncheckAll();
        requestGroup.uncheckAll();
        offerPicked = false;
        requestPicked = false;
        tradeButton.setDisabled(true);
    }

    public interface DomesticTradeListener {
        void onTrade(ResourceKind offer, ResourceKind request, int tradeRatio);
    }
}
