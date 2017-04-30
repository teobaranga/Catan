package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.FishTokenMap;
import com.mygdx.catan.enums.FishTokenType;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

public class FishTradeWindow extends CatanWindow {

    private FishTable offerTable;
    private TradeTokenListener tradeTokenListener;
    private Button acceptButton;
    private int choice;
    private Button rule2;
    private Button rule3;
    private Button rule4;
    private Button rule5;
    private Button rule7;


    public FishTradeWindow(String title, Skin skin, FishTokenMap hand) {
        super(title, skin);

        // Setup the width of the window
        setWidth(3f / 4f * Gdx.graphics.getWidth());
        setX(Gdx.graphics.getWidth() / 2 - getWidth() / 2);

        final Table exchangeRules = new Table(skin);

        rule2 = new TextButton("2 Fish\nMove Robber out\nor Pirate out", skin);
        initButton(rule2, 2);

        rule3 = new TextButton("3 Fish\n\nSteal a Resource", skin);
        initButton(rule3, 3);

        rule4 = new TextButton("4 Fish\nChoose a Resource\nfrom the Bank", skin);
        initButton(rule4, 4);

        rule5 = new TextButton("5 Fish\n\nBuild a Ship or Road", skin);
        initButton(rule5, 5);

        rule7 = new TextButton("7 Fish\nChoose a Progress \nCard Type", skin);
        initButton(rule7,7);

        add(new Label("Choose Trade",CatanGame.skin)).left().padBottom(20).padTop(20).colspan(3).row();

        exchangeRules.add(rule2).size(150, 100).pad(5);
        exchangeRules.add(rule3).size(150, 100).pad(5);
        exchangeRules.add(rule4).size(150, 100).pad(5);
        exchangeRules.add(rule5).size(150, 100).pad(5);
        exchangeRules.add(rule7).size(150, 100).pad(5);

        add(exchangeRules).colspan(3).row();

        add(new Label("Choose Tokens to Trade", CatanGame.skin)).left().padBottom(20).padTop(50).row();

        offerTable = new FishTable(CatanGame.skin, hand);
        add(offerTable).row();

        acceptButton = new TextButton("Trade", CatanGame.skin);
        acceptButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tradeTokenListener != null) {
                    tradeTokenListener.withGivenToken(new ImmutablePair<>(offerTable.getFishtoken(),choice));
                    remove();
                }
            }
        });
        add(acceptButton);
    }

    private void initButton(Button b, int choiceNumber) {
        b.setColor(Color.BLUE);
        b.pad(5);
        b.pack();
        b.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                choice = choiceNumber;
                ableall();
                b.setDisabled(true);
            }
        });
    }

    private void ableall() {
        rule2.setDisabled(false);
        rule3.setDisabled(false);
        rule4.setDisabled(false);
        rule5.setDisabled(false);
        rule7.setDisabled(false);

    }

    public void setTradeTokenListener(TradeTokenListener listener) {
        tradeTokenListener = listener;
    }

    public interface TradeTokenListener {
        void withGivenToken(ImmutablePair tokenChoicePair);
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
            add(widget_one).pad(100).padTop(50).padBottom(30);
            add(widget_two).pad(100).padTop(50).padBottom(30);
            add(widget_three).pad(100).padTop(50).padBottom(30);
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
