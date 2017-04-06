package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.FishTokenMap;
import com.mygdx.catan.enums.FishTokenType;

import java.util.ArrayList;
import java.util.List;

public class FishTradeWindow extends CatanWindow {

    private FishTable offerTable;
    private TradeTokenListener tradeTokenListener;
    private Button acceptButton;

    public FishTradeWindow(String title, Skin skin, FishTokenMap hand) {
        super(title, skin);

        // Setup the width of the window
        setWidth(3f / 4f * Gdx.graphics.getWidth());
        setX(Gdx.graphics.getWidth() / 2 - getWidth() / 2);

        final Table exchangeRules = new Table(skin);

        //TODO this is f* ugly, try to use a loop
        Table rule2 = new Table(skin);
        rule2.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        rule2.add(new Label("2 Fish\n\nMove Robber out", CatanGame.skin));
        rule2.pad(5);
        rule2.pack();

        Table rule3 = new Table(skin);
        rule3.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        rule3.add(new Label("3 Fish\n\nSteal a Resource", CatanGame.skin));
        rule3.pad(5);
        rule3.pack();

        Table rule4 = new Table(skin);
        rule4.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        rule4.add(new Label("4 Fish\nChoose a Resource\nfrom the Bank", CatanGame.skin));
        rule4.pad(5);
        rule4.pack();

        Table rule5 = new Table(skin);
        rule5.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        rule5.add(new Label("5 Fish\n\nBuild a Ship or Road", CatanGame.skin));
        rule5.pad(5);
        rule5.pack();

        Table rule7 = new Table(skin);
        rule7.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        rule7.add(new Label("7 Fish\nChoose a Progress \nCard Type", CatanGame.skin));
        rule7.pad(5);
        rule7.pack();

        exchangeRules.add(rule2).size(150, 100).pad(5);
        exchangeRules.add(rule3).size(150, 100).pad(5);
        exchangeRules.add(rule4).size(150, 100).pad(5);
        exchangeRules.add(rule5).size(150, 100).pad(5);
        exchangeRules.add(rule7).size(150, 100).pad(5);

        add(exchangeRules).colspan(3).row();


        offerTable = new FishTable(CatanGame.skin, hand);
        add(offerTable).row();

        acceptButton = new TextButton("Trade", CatanGame.skin);
        acceptButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (tradeTokenListener != null) {
                    tradeTokenListener.withGivenToken(offerTable.getFishtoken());
                    remove();
                }
            }});
        add(acceptButton);


    }

    public void setTradeTokenListener(TradeTokenListener listener) {
        tradeTokenListener = listener;
    }

    public interface TradeTokenListener {
        void withGivenToken(FishTokenMap givenToken);
    }

    private class FishTable extends Table {

        private List<FishTokenWidget> fishTokenWidgets;

        FishTable(Skin skin, FishTokenMap hand) {
            super(skin);

            fishTokenWidgets = new ArrayList<>();

            final FishTokenWidget widget_one = new FishTokenWidget(FishTokenType.ONE_FISH, skin, hand.getOneFish());
            final FishTokenWidget widget_two = new FishTokenWidget(FishTokenType.TWO_FISH, skin, hand.getTwoFish());
            final FishTokenWidget widget_three = new FishTokenWidget(FishTokenType.THREE_FISH, skin, hand.getThreeFish());
            fishTokenWidgets.add(widget_one);
            fishTokenWidgets.add(widget_two);
            fishTokenWidgets.add(widget_three);
            add(widget_one).pad(100);
            add(widget_two).pad(100);
            add(widget_three).pad(100);
        }

        /**
         * Get the total token represented in this FishTable.
         */
        private FishTokenMap getFishtoken() {
            final FishTokenMap tokenMap = new FishTokenMap();
            for (FishTokenWidget widget : fishTokenWidgets) {
                tokenMap.put(widget.getKind(), widget.getCount());
            }
            return tokenMap;
        }
    }
}
