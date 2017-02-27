package com.mygdx.catan.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.Config;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.ui.CatanWindow;

import java.io.IOException;

public class MenuScreen implements Screen {

    private final CatanGame aGame;
    private Stage aMenuStage;
    private Texture bg;
    private TextButton aJoinRandomButton;
    private TextButton aCreateGameButton;
    private TextButton aBrowseGamesButton;
    private TextButton aResumeGameButton;
    private Table aMenuTable;

    public MenuScreen(CatanGame pGame) {
        aGame = pGame;
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aMenuStage = new Stage();
        Gdx.input.setInputProcessor(aMenuStage);

        // Setup table for buttons
        aMenuTable = new Table();
//        aMenuTable.debugAll();
//        aMenuTable.setFillParent(true);
        aMenuStage.addActor(aMenuTable);
        aMenuTable.left().bottom().pad(25);

        // Setup buttons
        aJoinRandomButton = new TextButton("Join Random Game", CatanGame.skin);
        aJoinRandomButton.pad(30, 20, 30, 20);
        setupJoinRandomGame();
        aMenuTable.add(aJoinRandomButton).expandX().padLeft(25).padRight(25);

        aCreateGameButton = new TextButton("Create Game", CatanGame.skin);
        aCreateGameButton.pad(30, 20, 30, 20);
        setupButton(aCreateGameButton, ScreenKind.CREATE_GAME);

        aBrowseGamesButton = new TextButton("Browse Games", CatanGame.skin);
        aBrowseGamesButton.pad(30, 20, 30, 20);
        setupButton(aBrowseGamesButton, ScreenKind.BROWSE_GAMES);
        aBrowseGamesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Setup the window
                CatanWindow window = new CatanWindow("Browse Games", CatanGame.skin);
                window.setWidth(3f / 4f * Gdx.graphics.getWidth());
                window.setHeight(3f / 4f * Gdx.graphics.getHeight());
                window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2, Gdx.graphics.getHeight() / 2 - window.getHeight() / 2);
                window.setWindowCloseListener(window::remove);

                // Display the window
                aMenuStage.addActor(window);

                // TODO: display a list of servers
            }
        });

        aResumeGameButton = new TextButton("Resume Game", CatanGame.skin);
        aResumeGameButton.pad(30, 20, 30, 20);
        setupButton(aResumeGameButton, ScreenKind.LOBBY);

        aMenuTable.pack();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        aMenuStage.act(delta);
        aMenuStage.draw();

    }

    @Override
    public void resize(int width, int height) {
        aMenuStage.getViewport().update(width, height, false);
        aGame.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

    }

    @Override
    public void pause() {
        // Nothing to do
    }

    @Override
    public void resume() {
        // Nothing to do
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        aMenuStage.dispose();
    }

    private void setupButton(TextButton pTextButton, ScreenKind pScreenkind) {
        // add listener to button
        pTextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pTextButton.setChecked(false);
                aGame.switchScreen(pScreenkind);
            }
        });

        aMenuTable.add(pTextButton).expandX().padLeft(25).padRight(25);
    }

    /**
     * Setup the Join Random Game button.
     * Displays a window indicating progress towards finding a game.
     */
    private void setupJoinRandomGame() {
        // add listener to button
        aJoinRandomButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Reset the checked state of the button
                aJoinRandomButton.setChecked(false);

                // Setup the window
                CatanWindow window = new CatanWindow("Joining Game", CatanGame.skin);
                window.setWidth(1f / 4f * Gdx.graphics.getWidth());
                window.setHeight(1f / 4f * Gdx.graphics.getHeight());
                window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2, Gdx.graphics.getHeight() / 2 - window.getHeight() / 2);
                window.setWindowCloseListener(window::remove);

                // Display the window
                aMenuStage.addActor(window);

                new Thread(() -> {
                    // TODO: remove this
                    // simulate some loading
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    // TODO find a random game and connect it it
                    try {
                        CatanGame.client.connect(5000, Config.IP, Config.TCP, Config.UDP);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO: connection failed, inform the user
                    }
                    Gdx.app.postRunnable(() -> {
                        // Remove the window
                        window.remove();
                        // Bring the user to the lobby screen
                        if (CatanGame.client.isConnected()) {
                            aGame.switchScreen(ScreenKind.LOBBY);
                        }
                    });
                }).start();
            }

        });
    }
}
