package com.mygdx.catan.screens.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.ui.CatanWindow;

public class LobbyScreen implements Screen {

    private static final String TITLE = "Lobby";

    private static Stage stage;
    private final CatanGame game;
    private Texture bg;

    public LobbyScreen(CatanGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create & add the window
        CatanWindow window = new CatanWindow(TITLE, game.skin);
        window.setWindowListener(() -> Gdx.app.exit());
        window.debugAll();

        window.add(new Table(game.skin).background(game.skin.newDrawable("background", Color.RED)));
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
