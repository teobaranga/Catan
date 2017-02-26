package com.mygdx.catan.screens.login;

///**
// * Created by amandaivey on 1/22/17.
// */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;

public class LoginScreen implements Screen {

    private final CatanGame aGame;
    private static final String TITLE = "Login";
    private Stage aLoginStage;
    private Texture bg;
    private TextButton aLoginButton;
    private TextButtonStyle aButtonStyle;
    private Table aLoginTable;

    public LoginScreen(CatanGame pGame) {
        aGame = pGame;
    }

    @Override
    public void show() {

        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aLoginStage = new Stage();
        Gdx.input.setInputProcessor(aLoginStage);

        Label usernameLabel = new Label ("Username:", aGame.skin);
        TextField usernameText = new TextField ("", aGame.skin);
        usernameText.setMaxLength(20);

        aLoginTable = new Table(aGame.skin);
        aLoginTable.add(usernameLabel);
        aLoginTable.add(usernameText).width(200);
        aLoginTable.row();
        aLoginTable.setFillParent(true);
        aLoginStage.addActor(aLoginTable);
        aLoginTable.bottom().center();

        // Generate a 1x1 white texture and store it in skin named white
        Pixmap pixmap = new Pixmap(1,1, Format.RGBA8888);
        pixmap.setColor(Color.GRAY);
        pixmap.fill();
        aGame.skin.add("white", new Texture(pixmap));

        // store default font into skin under name default
        aGame.skin.add("default", new BitmapFont());

        aButtonStyle = new TextButtonStyle();
        aButtonStyle.font = aGame.skin.getFont("default");
        aButtonStyle.up = aGame.skin.getDrawable("white");

        aLoginButton = new TextButton("Login", aButtonStyle);
        aLoginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                aLoginButton.setChecked(false);
                if(!(usernameText.getText() == null || usernameText.getText().trim().isEmpty())){
                    //aLoginButton.setText("Logging In \n Please click to continue to the Main Menu");
                    setupButton(aLoginButton, ScreenKind.MAIN_MENU);
                }
                else{
                    aLoginButton.setText("Please enter a valid username \n Please click to go back to the Login Screen");
                    setupButton(aLoginButton, ScreenKind.LOGIN);
                }

            }

        });

        aLoginTable.add(aLoginButton).pad(50);
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

    public void setupButton(TextButton pTextButton, ScreenKind pScreenkind) {
        // add listener to button
        pTextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pTextButton.setChecked(false);
                aGame.switchScreen(pScreenkind);
            }

        });

        aLoginTable.add(pTextButton).pad(50);
    }

    @Override
    public void resize(int width, int height) {
        aLoginStage.getViewport().update(width, height, false);
        aGame.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        aLoginStage.dispose();
    }

}

