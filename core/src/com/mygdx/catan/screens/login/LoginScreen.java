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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;

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
        Label passwordLabel = new Label ("Password:", aGame.skin);
        TextField passwordText = new TextField("", aGame.skin);

        aLoginTable = new Table(aGame.skin);
        aLoginTable.add(usernameLabel);
        aLoginTable.add(usernameText).width(200);
        aLoginTable.row();
        aLoginTable.add(passwordLabel);
        aLoginTable.add(passwordText).width(200);
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


        aLoginButton = new TextButton("Join Random", aButtonStyle);


        aLoginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aLoginButton.setText("Good job!");
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

