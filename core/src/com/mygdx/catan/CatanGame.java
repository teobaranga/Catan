package com.mygdx.catan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.mygdx.catan.screens.lobby.LobbyScreen;
import com.mygdx.catan.screens.login.LoginScreen;
import com.mygdx.catan.screens.menu.MenuScreen;

public class CatanGame extends Game {

    public Skin skin;

    public SpriteBatch batch;

    @Override
    public void create() {
        createBasicSkin();
        batch = new SpriteBatch();
        setScreen(new LoginScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    private void createBasicSkin() {
        // Create a font
        BitmapFont font = new BitmapFont();
        skin = new Skin();
        skin.add("default", font);

        // Create a texture
        Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 10, Pixmap.Format.RGB888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("background", new Texture(pixmap));

        // Create a button style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("background", Color.GRAY);
        textButtonStyle.down = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.over = skin.newDrawable("background", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        // Create a window style
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("background", Color.DARK_GRAY);
        // Dim background
        windowStyle.stageBackground = skin.newDrawable("background", Color.valueOf("#00000099"));
        windowStyle.titleFont = skin.getFont("default");
        windowStyle.titleFontColor = Color.WHITE;
        skin.add("default", windowStyle);

        //create a label style

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.background = skin.newDrawable("background", Color.BLACK);
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.background = skin.newDrawable("background", Color.BLUE);
        textFieldStyle.font = skin.getFont("default");
        textFieldStyle.fontColor = Color.YELLOW;
        skin.add("default", textFieldStyle);
    }
}
