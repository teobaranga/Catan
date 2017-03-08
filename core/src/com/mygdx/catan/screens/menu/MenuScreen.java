package com.mygdx.catan.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.request.CreateGame;
import com.mygdx.catan.request.JoinRandomGame;
import com.mygdx.catan.response.GameResponse;
import com.mygdx.catan.ui.CatanWindow;

public class MenuScreen implements Screen {

    private final CatanGame aGame;
    private Stage aMenuStage;
    private Texture bg;
    private TextButton aJoinRandomButton;
    private TextButton aCreateGameButton;
    private TextButton aBrowseGamesButton;
    private TextButton aResumeGameButton;
    private TextButton aStopMusicButton;
    private Table aMenuTable;

    public MenuScreen(CatanGame pGame) {
        aGame = pGame;
        aGame.menuMusic.play();
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
        aMenuTable.setFillParent(true);
        aMenuTable.left().bottom().pad(25);

        final Label welcomeLabel = new Label("Welcome, " + CatanGame.account.getUsername(), CatanGame.skin);
        aMenuTable.add(welcomeLabel).top().left().expandY().row();

        // Setup buttons
        aJoinRandomButton = new TextButton("Join Random Game", CatanGame.skin);
        aJoinRandomButton.pad(30, 20, 30, 20);
        setupJoinRandomGame();
        aMenuTable.add(aJoinRandomButton).padLeft(25).padRight(25);

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
                window.setWindowCloseListener(() -> {
                    window.remove();
                    aGame.clickSound.play(0.2F);
                });

                // Display the window
                aMenuStage.addActor(window);

                // TODO: display a list of servers
            }
        });

        aResumeGameButton = new TextButton("Resume Game", CatanGame.skin);
        aResumeGameButton.pad(30, 20, 30, 20);
        setupButton(aResumeGameButton, ScreenKind.LOBBY);

        aStopMusicButton = new TextButton("Stop\nMusic", CatanGame.skin);
        aStopMusicButton.pad(20, 20, 20, 20);
        aStopMusicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (aGame.menuMusic.isPlaying()) {
                    aGame.menuMusic.pause();
                    aStopMusicButton.setText("Play\nMusic");
                } else {
                    aGame.menuMusic.play();
                    aStopMusicButton.setText("Stop\nMusic");
                }
            }
        });
        aMenuTable.add(aStopMusicButton).padLeft(300).padRight(25).left();


        aMenuTable.pack();

        aMenuStage.addActor(aMenuTable);
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
                aGame.clickSound.play(0.2F);
                pTextButton.setChecked(false);
                aGame.switchScreen(pScreenkind);
            }
        });

        aMenuTable.add(pTextButton).padLeft(25).padRight(25).left();
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

                aGame.clickSound.play(0.2F);
                // Setup the window
                CatanWindow window = new CatanWindow("Join Game", CatanGame.skin,
                        1f / 4f * Gdx.graphics.getWidth(), 1f / 4f * Gdx.graphics.getHeight());

                final Table contentTable = new Table(CatanGame.skin);

                final Label messageLabel = new Label("Looking for games...", CatanGame.skin);
                messageLabel.setAlignment(Align.center);
                contentTable.add(messageLabel).colspan(2);

                window.row();
                window.add(contentTable).expandX().fillX();

                // Display the window
                aMenuStage.addActor(window);

                Listener listener = new Listener() {
                    @Override
                    public void received(Connection connection, Object object) {
                        if (object instanceof GameResponse) {
                            Gdx.app.postRunnable(() -> {
                                // No game found
                                if (((GameResponse) object).getGame() == null) {
                                    // Inform the user
                                    messageLabel.setText("No games found, would you\nlike to create a new one?");

                                    // Add the choices
                                    contentTable.row();

                                    final TextButton yes = new TextButton("Yes", CatanGame.skin);
                                    yes.addListener(new ClickListener() {
                                        @Override
                                        public void clicked(InputEvent event, float x, float y) {
                                            // TODO handle creation of new game
                                            aGame.clickSound.play(0.2F);
                                            final CreateGame createGame = new CreateGame();
                                            createGame.account = CatanGame.account;
                                            CatanGame.client.sendTCP(createGame);
                                        }
                                    });
                                    final TextButton no = new TextButton("No", CatanGame.skin);
                                    no.addListener(new ClickListener() {
                                        @Override
                                        public void clicked(InputEvent event, float x, float y) {
                                            aGame.clickSound.play(0.2F);
                                            window.close();
                                        }
                                    });
                                    contentTable.add(yes).right().padRight(10).width(50).padTop(20);
                                    contentTable.add(no).left().padLeft(10).width(50).padTop(20);
                                } else {
                                    // Set the current game
                                    GameManager.getInstance().setCurrentGame(((GameResponse) object).getGame());
                                    // Remove the window
                                    window.close();
                                    // Bring the user to the lobby screen
                                    if (CatanGame.client.isConnected()) {
                                        aGame.switchScreen(ScreenKind.LOBBY);
                                    }
                                }
                            });
                        }
                    }
                };

                CatanGame.client.addListener(listener);

                window.setWindowCloseListener(() -> {
                    aGame.clickSound.play(0.2F);
                    CatanGame.client.removeListener(listener);
                });

                // Request to join a random game
                CatanGame.client.sendTCP(JoinRandomGame.newInstance(CatanGame.account));
            }

        });
    }
}
