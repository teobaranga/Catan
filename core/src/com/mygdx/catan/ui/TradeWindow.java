package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

import java.util.ArrayList;
import java.util.List;

public class TradeWindow extends CatanWindow {

    /**
     * Create a new trade window
     *
     * @param title       Title of the window
     * @param skin        The skin used to theme this window
     */
    public TradeWindow(String title, Skin skin) {
        super(title, skin);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DroidSans.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        BitmapFont font12 = generator.generateFont(parameter);
        generator.dispose(); // don't forget to dispose to avoid memory leaks!

        final Label.LabelStyle labelStyle = new Label.LabelStyle(font12, Color.WHITE);

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

        final Table offerTable = new ResourceTable(skin);
        leftTable.add(offerTable).left();

        final ResourceTable demandTable = new ResourceTable(skin);
        leftTable.add(demandTable).left().padLeft(10);

        leftTable.row();

        final TextButton proposeTrade = new TextButton("Propose Trade", skin);
        proposeTrade.pad(10, 50, 10, 50);
        leftTable.add(proposeTrade).colspan(2).padTop(20);

//        leftTable.debug();
        add(leftTable).width(getWidth() / 2f).padLeft(40);

        // Create the right-side table where the offers from the other players are displayed
        final Table rightTable = new Table(skin);

        final Label offersLabel = new Label("Offers from players", skin);
        offersLabel.setStyle(labelStyle);
        rightTable.add(offersLabel).padBottom(20);

        rightTable.row();

        final ResourceMap offer = new ResourceMap();
        offer.put(ResourceKind.ORE, 1);
        final TradeOfferItem playerOffer = new TradeOfferItem("Test Player", offer, skin);
        rightTable.add(playerOffer).padLeft(10).center();

//        rightTable.debugAll();

        add(rightTable).width(getWidth() / 2f).top();

//        debugAll();
    }

    private class ResourceTable extends Table {

        private final Table middleTable;

        private List<ResourceWidget> offerResourceWidgets;

        public ResourceTable(Skin skin) {
            super(skin);

            offerResourceWidgets = new ArrayList<>(ResourceKind.values().length);

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
    }
}
