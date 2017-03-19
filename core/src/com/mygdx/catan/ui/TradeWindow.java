package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;

public class TradeWindow extends CatanWindow {

    /** ResourceTable containing the offer of the local player */
    private ResourceTable offerTable;

    private ResourceTable requestTable;

    /** Table containing the offers of the other players */
    private Table offersTable;

    private ScrollPane scrollPane;

    private List<TradeOfferItem> offers;

    private TradeProposalListener tradeProposalListener;

    private OfferProposalListener offerProposalListener;

    private OfferAcceptListener offerAcceptListener;

    /**
     * Constructor for creating a window for the players on the receiving side of the trade.
     * This creates a trade window containing information about the initializing player, the offer,
     * and the request. Players may then propose a counter-offer.
     *
     * @param username username of the player that initialized the trade
     * @param offer    the offer of the initializing player
     * @param request  the request of the initializing player
     */
    public TradeWindow(String username, ResourceMap offer, ResourceMap request, Skin skin) {
        super("Trade", skin);

        // Setup the width of the window
        setWidth(3f / 4f * Gdx.graphics.getWidth());
        setX(Gdx.graphics.getWidth() / 2 - getWidth() / 2);

        // Create the header table contain information about the player who started the trade,
        // their offer and their request
        final Table header = new Table(skin);

        final Label offerLabel = new Label("Offer", skin);
        header.add(offerLabel).right().padRight(50);

        final Label info = new Label(username + " proposed a trade", skin);
        header.add(info).padTop(10).padBottom(10);

        final Label requestLabel = new Label("Request", skin);
        header.add(requestLabel).left().padLeft(50);

        header.row();

        final HorizontalResourceView offerView = new HorizontalResourceView(offer, skin);
        header.add(offerView).right().padRight(25).padBottom(20);

        header.add();

        final HorizontalResourceView requestView = new HorizontalResourceView(request, skin);
        header.add(requestView).left().padLeft(25).padBottom(20);

        // Add the header table
        add(header).colspan(2);

        row();

        init(false, skin);

        pack();
    }

    /**
     * Create a new trade window
     *
     * @param skin The skin used to theme this window
     */
    public TradeWindow(Skin skin) {
        super("Trade", skin);

        // Setup the width of the window
        setWidth(3.7f / 4f * Gdx.graphics.getWidth());
        setX(Gdx.graphics.getWidth() / 2 - getWidth() / 2);

        init(true, skin);
    }

    /**
     * Initialize the common GUI elements of the trade window.
     *
     * @param instigator true if this window is created as a result of a player initializing p2p trade,
     *                   false if this window is created to notify a player of trade being initialized.
     */
    private void init(boolean instigator, Skin skin) {
        setMovable(true);

        offers = new ArrayList<>();

        final Label.LabelStyle labelStyle = new Label.LabelStyle(getFont(), Color.WHITE);

        // Create the left-side table where the player offer and demand exist
        final Table leftTable = new Table(skin);

        final Label yourOffer = new Label("Your offer", skin);
        yourOffer.setStyle(labelStyle);
        leftTable.add(yourOffer).padBottom(20);

        // If the player initialized the p2p trade then he/she can specify the request as well
        if (instigator) {
            final Label yourRequest = new Label("Your request", skin);
            yourRequest.setStyle(labelStyle);
            leftTable.add(yourRequest).padBottom(20);
        }

        leftTable.row();

        offerTable = new ResourceTable(skin);
        leftTable.add(offerTable).left();

        if (instigator) {
            requestTable = new ResourceTable(skin);
            leftTable.add(requestTable).left().padLeft(10);
        }

        leftTable.row();

        final TextButton proposeTrade = new TextButton("Propose Trade", skin);
        proposeTrade.pad(10, 50, 10, 50);
        proposeTrade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final ResourceMap offer = offerTable.getResources();

                // Check if the user actually set an offer
                if (offer.isEmpty())
                    return;

                if (instigator) {
                    final ResourceMap request = requestTable.getResources();

                    // Also check if the trade initiator set a request
                    // If not, the trade can't happen
                    if (request.isEmpty())
                        return;

                    if (tradeProposalListener != null)
                        tradeProposalListener.onTradeProposed(offer, request);
                } else {
                    if (offerProposalListener != null)
                        offerProposalListener.onOfferProposed(offer);
                }
            }
        });
        leftTable.add(proposeTrade).colspan(2).padTop(20);

        add(leftTable).width(getWidth() / 2f);

        // Create the right-side table where the offers from the other players are displayed
        final Table rightTable = new Table(skin);

        final Label offersLabel = new Label("Offers from players", skin);
        offersLabel.setStyle(labelStyle);
        rightTable.add(offersLabel).padBottom(20);

        rightTable.row();

        offersTable = new Table(skin);
        offersTable.padRight(20);

        scrollPane = new ScrollPane(offersTable, skin);
        scrollPane.setFlickScroll(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        rightTable.add(scrollPane).height(3 / 4f * getHeight()).expandX().left().padLeft(20);

//        rightTable.debugAll();

        add(rightTable).width(getWidth() / 2f).top();
    }

    /**
     * Request scroll focus on the ScrollPane. Use this so that the user doesn't need
     * to click on it to enable scrolling using the scroll wheel. Must be called after
     * the window was added to a stage.
     */
    public void requestScrollFocus() {
        if (getStage() == null)
            return;
        getStage().setScrollFocus(scrollPane);
    }

    /**
     * Set the maximum number of resources that the player can offer.
     * The player should not be able to offer more resources than are
     * available to him/her.
     *
     * @param maxOffer Map containing the max count allowed to offer for each resource kind
     */
    public void setMaxOffer(ResourceMap maxOffer) {
        offerTable.setMaxResources(maxOffer);
    }

    public void setTradeProposalListener(TradeProposalListener listener) {
        this.tradeProposalListener = listener;
    }

    public void setOfferProposalListener(OfferProposalListener offerProposalListener) {
        this.offerProposalListener = offerProposalListener;
    }

    public void setOfferAcceptListener(OfferAcceptListener offerAcceptListener) {
        this.offerAcceptListener = offerAcceptListener;
    }

    public void addTradeOffer(String player, ResourceMap offer) {
        // Update the offer if it already exists
        for (TradeOfferItem tradeOfferItem : offers) {
            if (tradeOfferItem.getOwner().equals(player)) {
                tradeOfferItem.setOffer(offer);
                return;
            }
        }
        // Create a new offer
        final TradeOfferItem playerOffer = new TradeOfferItem(player, offer, getSkin());
        playerOffer.setAcceptListener((username, offer1) -> {
            if (offerAcceptListener != null) {
                offerAcceptListener.onOfferAccepted(username, offer1, offerTable.getResources());
            }
        });
        offersTable.add(playerOffer).padLeft(10).expandY().top();
        offers.add(playerOffer);
    }

    private BitmapFont getFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DroidSans.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    public interface TradeProposalListener {
        void onTradeProposed(ResourceMap offer, ResourceMap request);
    }

    public interface OfferProposalListener {
        void onOfferProposed(ResourceMap offer);
    }

    public interface OfferAcceptListener {
        void onOfferAccepted(String username, ResourceMap remoteOffer, ResourceMap localOffer);
    }

    private class ResourceTable extends Table {

        private final Table middleTable;

        private List<ResourceWidget> resourceWidgets;

        ResourceTable(Skin skin) {
            super(skin);

            resourceWidgets = new ArrayList<>(ResourceKind.SIZE);

            middleTable = new Table();

            ResourceKind[] values = ResourceKind.values();
            for (int i = 0; i < values.length; i++) {
                ResourceKind resourceKind = values[i];
                final ResourceWidget widget = new ResourceWidget(resourceKind, skin);
                resourceWidgets.add(widget);
                if (i == 3 || i == 5)
                    row();
                if (i == 3)
                    add(middleTable).colspan(3).expandX().fillX();
                if (i == 3 || i == 4)
                    middleTable.add(widget).expandX().align(i == 3 ? Align.right : Align.left);
                else
                    add(widget);
            }
        }

        private void setMaxResources(ResourceMap maxResources) {
            for (ResourceWidget widget : resourceWidgets) {
                widget.setMaxResource(maxResources.get(widget.getKind()));
            }
        }

        /**
         * Get the total resources represented in this ResourceTable.
         */
        private ResourceMap getResources() {
            final ResourceMap resourceMap = new ResourceMap();
            for (ResourceWidget widget : resourceWidgets) {
                resourceMap.add(widget.getCount());
            }
            return resourceMap;
        }
    }
}
