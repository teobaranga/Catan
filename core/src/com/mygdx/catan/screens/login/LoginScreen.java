package com.mygdx.catan.screens.login;

///**
// * Created by amandaivey on 1/22/17.
// */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.account.AccountManager;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.request.LoginRequest;
import com.mygdx.catan.response.LoginResponse;
import com.mygdx.catan.ui.CatanWindow;

public class LoginScreen implements Screen {

    private final static String ERROR_USERNAME = "Please enter a valid username";

    private final CatanGame aGame;

    private Stage aLoginStage;

    private Texture bg;

    private TextButton aLoginButton;
    private Label errorMessageLabel;

    private Listener listener;

    /** The account that attempted to log in last */
    private Account account;

    public LoginScreen(CatanGame pGame) {
        aGame = pGame;
        listener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof LoginResponse) {
                    Gdx.app.postRunnable(() -> {
                        // Handle the login response from the server
                        if (((LoginResponse) object).success) {
                            // Cache the account used to login successfully
                            AccountManager.writeLocalAccount(account);
                            CatanGame.account = account;
                            // Move on to the main screen
                            aGame.switchScreen(ScreenKind.MAIN_MENU);
                        } else {
                            errorMessageLabel.setText(ERROR_USERNAME);
                        }
                    });
                }
            }
        };
    }

    @Override
    public void show() {
        CatanGame.client.addListener(listener);

        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aLoginStage = new Stage();
        Gdx.input.setInputProcessor(aLoginStage);

        // Setup the window
        CatanWindow window = new CatanWindow("", CatanGame.skin);
        window.setWidth(2f / 4f * Gdx.graphics.getWidth());
        window.setHeight(2f / 4f * Gdx.graphics.getHeight());
        window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2, Gdx.graphics.getHeight() / 2 - window.getHeight() / 2);
        window.setWindowCloseListener(() -> {
            aGame.clickSound.play(0.2F);
            window.remove();
        });
        aLoginStage.addActor(window);

        // Create the table holding the login elements
        Table aLoginTable = new Table(CatanGame.skin);
        aLoginTable.setFillParent(true);

        // Create the username label & input field
        Label usernameLabel = new Label("Username:", CatanGame.skin);
        TextField usernameText = new TextField("", CatanGame.skin);
        usernameText.setMaxLength(20);
        aLoginTable.add(usernameLabel);
        aLoginTable.row();
        aLoginTable.add(usernameText).width(200);

        // Create the login button
        aLoginButton = new TextButton("Login", CatanGame.skin);
        aLoginButton.padLeft(10).padRight(10);
        aLoginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                aGame.clickSound.play(0.2F);
                aLoginButton.setChecked(false);
                // Clear the error message if there is one
                errorMessageLabel.setText(null);
                // Send a login request to the server if the input is valid
                if (usernameText.getText() != null && !usernameText.getText().trim().isEmpty()) {
                    // Set the current account optimistically
                    account = new Account(usernameText.getText().trim(), null);
                    // Create the login request
                    final LoginRequest request = new LoginRequest();
                    request.account = account;
                    // Send the login request
                    CatanGame.client.sendTCP(request);
                } else {
                    errorMessageLabel.setText(ERROR_USERNAME);
                }

            }

        });
        aLoginTable.row();
        aLoginTable.add(aLoginButton).colspan(2).pad(20);

        // Create the error message label
        errorMessageLabel = new Label("", CatanGame.skin);
        aLoginTable.row();
        aLoginTable.add(errorMessageLabel).colspan(2);

        aLoginTable.pack();

        window.addActor(aLoginTable);

        // Focus on the text field
        aLoginStage.setKeyboardFocus(usernameText);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        aLoginStage.act(delta);
        aLoginStage.draw();

    }

    @Override
    public void resize(int width, int height) {
        aLoginStage.getViewport().update(width, height, false);
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
        CatanGame.client.removeListener(listener);
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        aLoginStage.dispose();
    }
}
