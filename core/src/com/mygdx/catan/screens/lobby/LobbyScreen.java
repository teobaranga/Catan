package com.mygdx.catan.screens.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.ui.CatanWindow;

public class LobbyScreen implements Screen {

    private static final String TITLE = "Lobby";

    private static Stage stage;
    private final CatanGame game;
    private final Screen parentScreen;
    private Texture bg;

    public LobbyScreen(CatanGame game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create & add the window
        CatanWindow window = new CatanWindow(TITLE, game.skin);
        // TODO: Closing the lobby window should bring the user back to the MainMenu
        window.setWindowListener(() -> game.setScreen(parentScreen));
        window.debugAll();

        Table contentTable = new Table(game.skin);
        contentTable.background(game.skin.newDrawable("background", Color.RED));
//        contentTable.debugAll();
        contentTable.padTop(window.getPadTop());
        contentTable.setFillParent(true);

        Table rulesTable = new Table(game.skin);
//        rulesTable.debugAll();
        rulesTable.background(game.skin.newDrawable("background", Color.BLUE));
        rulesTable.pack();

        Table chatTable = new Table(game.skin);
//        chatTable.debugAll();
        chatTable.background(game.skin.newDrawable("background", Color.YELLOW));
        chatTable.pack();

        TextButton ready = new TextButton("Ready", game.skin);

        contentTable.top().left();
        contentTable.add(rulesTable).width(425).expandX().height(400).pad(20);
        contentTable.add(chatTable).width(425).expandX().height(400).pad(20);
        contentTable.row();
        contentTable.add(ready).height(50).width(400).colspan(2);

        contentTable.pack();
        window.row().fill().expand();
        window.add(contentTable);
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
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        stage.dispose();
    }
}
