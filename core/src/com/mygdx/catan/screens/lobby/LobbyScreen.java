package com.mygdx.catan.screens.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.request.MarkAsReady;
import com.mygdx.catan.response.MarkedAsReady;
import com.mygdx.catan.screens.lobby.chat.ChatMessage;
import com.mygdx.catan.ui.CatanWindow;

public class LobbyScreen implements Screen {

    private static final String TITLE = "Lobby";

    private final Listener lobbyListener;

    private Stage stage;
    private final CatanGame game;
    private Texture bg;

    public LobbyScreen(CatanGame game) {
        this.game = game;
        lobbyListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MarkedAsReady) {
                    Gdx.app.postRunnable(() -> {
                        System.out.println("You have been marked as ready.");
                        game.switchScreen(ScreenKind.IN_GAME);
                    });
                }
            }
        };
    }

    @Override
    public void show() {
        CatanGame.client.addListener(lobbyListener);

        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create & add the window
        CatanWindow window = new CatanWindow(TITLE + " : " + CatanGame.client.getRemoteAddressTCP(), CatanGame.skin);
        // TODO: Closing the lobby window should bring the user back to the MainMenu
        window.setWindowCloseListener(() -> game.switchScreen(ScreenKind.MAIN_MENU));
//        window.debugAll();

        Table contentTable = new Table(CatanGame.skin);
//        contentTable.background(game.skin.newDrawable("background", Color.RED));
//        contentTable.debugAll();

        Table mapAndRules = new Table(CatanGame.skin);
        mapAndRules.top();
//        mapAndRules.debugAll();
//        mapAndRules.background(CatanGame.skin.newDrawable("background", Color.BLUE));
        Label mapAndRulesLabel = new Label(" Maps & Rules:", CatanGame.skin, "lobby-header");
        mapAndRules.add(mapAndRulesLabel).expandX().fillX();

        Table chatTable = new Table(CatanGame.skin);
        chatTable.debugAll();
//        chatTable.background(game.skin.newDrawable("background", Color.YELLOW));
        setupChat(chatTable);
//        chatTable.pack();

        TextButton ready = new TextButton("Ready", CatanGame.skin);
        ready.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CatanGame.client.sendTCP(new MarkAsReady());
            }
        });

//        contentTable.top().left();
        contentTable.add(mapAndRules).width(400).height(400).expandX();
        contentTable.add(chatTable).width(400).height(400).expandX().fillX();
        contentTable.row();
        contentTable.add(ready).height(50).width(400).colspan(2).padTop(20);

//        contentTable.pack();
        window.row();
        window.add(contentTable).expandX().fillX();
        stage.addActor(window);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void resume() {
        // Nothing to do
    }

    @Override
    public void pause() {
        // Nothing to do
    }

    @Override
    public void hide() {
        CatanGame.client.removeListener(lobbyListener);
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        stage.dispose();
    }

    private void setupChat(Table chatTable) {
        final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        String reallyLongString = "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n"
                + "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n"
                + "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n";
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi feugiat mollis nisl, a egestas enim aliquet et. Fusce rhoncus non erat ultricies fringilla. Aliquam auctor est ut neque congue accumsan. Aliquam sed ligula metus. Phasellus luctus arcu sodales leo dictum, vitae iaculis justo pretium. Sed eget vestibulum tortor. Vivamus euismod et libero at tempus. Phasellus ac dolor in urna efficitur laoreet id eu massa. In sit amet nulla at turpis fermentum porttitor nec nec lectus. Nullam ornare nisl at purus venenatis luctus. Suspendisse potenti. Donec finibus sem ac malesuada luctus. Pellentesque auctor felis at feugiat posuere. Duis sollicitudin, urna ut tristique commodo, dolor sapien consectetur tortor, ac convallis ante odio in sem.";
        final ChatMessage msg = new ChatMessage("Sender", lorem);
        final ChatMessage msg2 = new ChatMessage("Sender", lorem);
//        final Label text = new Label(reallyLongString, skin);
//        text.setAlignment(Align.center);
//        text.setWrap(true);
//        final Label text2 = new Label("This is a short string!", skin);
//        text2.setAlignment(Align.center);
//        text2.setWrap(true);
//        final Label text3 = new Label(reallyLongString, skin);
//        text3.setAlignment(Align.center);
//        text3.setWrap(true);

        final Table scrollTable = new Table();
        scrollTable.left().top();
        scrollTable.add(msg).expandX().fillX();
        scrollTable.row();
        scrollTable.add(msg2).expandX().fillX();
        scrollTable.row();
//        scrollTable.add(text2);
//        scrollTable.row();
//        scrollTable.add(text3);

        final ScrollPane scroller = new ScrollPane(scrollTable);

//        chatTable.setFillParent(true);
        chatTable.add(scroller).fill().expand();
    }
}
