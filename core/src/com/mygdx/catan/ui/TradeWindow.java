package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;

public class TradeWindow extends CatanWindow {

    private final ScrollPane scrollPane;

    /** ResourceTable containing the offer of the local player */
    private final ResourceTable offerTable;

    /** Table containing the offers of the other players */
    private final Table offersTable;

    /**
     * Create a new trade window
     *
     * @param title Title of the window
     * @param skin  The skin used to theme this window
     */
    public TradeWindow(String title, Skin skin) {
        super(title, skin);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DroidSans.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        final Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        setSize(3.7f / 4f * Gdx.graphics.getWidth(), 3f / 4f * Gdx.graphics.getHeight());
        setX(Gdx.graphics.getWidth() / 2 - getWidth() / 2);

        setMovable(true);

        row();

        // Create the left-side table where the player offer and demand exist
        final Table leftTable = new Table(skin);

        final Label yourOffer = new Label("Your offer", skin);
        yourOffer.setStyle(labelStyle);
        leftTable.add(yourOffer).padBottom(20);

        final Label yourRequest = new Label("Your request", skin);
        yourRequest.setStyle(labelStyle);
        leftTable.add(yourRequest).padBottom(20);

        leftTable.row();

        offerTable = new ResourceTable(skin);
        leftTable.add(offerTable).left();

        final ResourceTable demandTable = new ResourceTable(skin);
        leftTable.add(demandTable).left().padLeft(10);

        leftTable.row();

        final TextButton proposeTrade = new TextButton("Propose Trade", skin);
        proposeTrade.pad(10, 50, 10, 50);
        leftTable.add(proposeTrade).colspan(2).padTop(20);

        add(leftTable).width(getWidth() / 2f).padLeft(40);

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
        rightTable.add(scrollPane).height(3 / 4f * getHeight());

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

    public void addTradeOffer(String player, ResourceMap offer) {
        final TradeOfferItem playerOffer = new TradeOfferItem(player, offer, getSkin());
        offersTable.add(playerOffer).padLeft(10).expandY().top();
    }

    private class ResourceTable extends Table {

        private final Table middleTable;

        private List<ResourceWidget> offerResourceWidgets;

        ResourceTable(Skin skin) {
            super(skin);

            offerResourceWidgets = new ArrayList<>(ResourceKind.SIZE);

            middleTable = new Table();

            ResourceKind[] values = ResourceKind.values();
            for (int i = 0; i < values.length; i++) {
                ResourceKind resourceKind = values[i];
                final ResourceWidget widget = new ResourceWidget(resourceKind, skin);
                offerResourceWidgets.add(widget);
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
            for (ResourceWidget widget : offerResourceWidgets) {
                widget.setMaxResource(maxResources.get(widget.getKind()));
            }
        }
    }
}
