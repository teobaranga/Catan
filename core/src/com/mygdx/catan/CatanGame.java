package com.mygdx.catan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.screens.create.CreateScreen;
import com.mygdx.catan.screens.lobby.LobbyScreen;
import com.mygdx.catan.screens.menu.MenuScreen;
import com.mygdx.catan.session.SessionScreen;

public class CatanGame extends Game {
	
	private GameBoardManager aGameBoardManager = new GameBoardManager();

    public Skin skin;

    public SpriteBatch batch;

    MenuScreen menuScreen;

    @Override
    public void create() {
        createBasicSkin();
        batch = new SpriteBatch();
        if (menuScreen == null)
            menuScreen = new MenuScreen(this);
        setScreen(menuScreen);
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
        Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() / 6, Gdx.graphics.getHeight() / 10, Pixmap.Format.RGB888);
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
        labelStyle.background = skin.newDrawable("background", (Color.CORAL));
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);


        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.background = skin.newDrawable("background", Color.WHITE);
        textFieldStyle.font = skin.getFont("default");
        textFieldStyle.fontColor = Color.YELLOW;
        skin.add("default", textFieldStyle);
    }

    public void switchScreen(ScreenKind pScreenKind) {
        switch (pScreenKind) {
            case MAIN_MENU:
                this.setScreen(menuScreen);
                break;
            case BROWSE_GAMES:
                break;
            case CREATE_GAME:
                //this.setScreen(new CreateScreen(this, menuScreen));
            	this.setScreen(new SessionScreen(this, aGameBoardManager));
                break;
            case LOBBY:
                this.setScreen(new LobbyScreen(this, menuScreen));
                break;
            case RESUME_GAME:
                break;
            default:
                break;
        }
    }
}
