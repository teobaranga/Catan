package com.mygdx.catan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.account.AccountManager;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.screens.create.CreateScreen;
import com.mygdx.catan.screens.lobby.LobbyScreen;
import com.mygdx.catan.screens.login.LoginScreen;
import com.mygdx.catan.screens.menu.MenuScreen;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.session.SessionManager;
import com.mygdx.catan.session.SessionScreen;

import java.io.IOException;

public class CatanGame extends Game {
    /** The Client representing the current user */
    public static final Client client;

    /** The skin used throughout the whole game */
    @SuppressWarnings("LibGDXStaticResource")
    public static Skin skin;

    /** The account of the user currently logged in */
    public static Account account;

    static {
        client = new Client();

        // Register request & response classes (needed for networking)
        // Must be registered in the same order in the server
        Kryo kryo = client.getKryo();
        Config.registerKryoClasses(kryo);
        client.start();

        // Connect to the server
        new Thread(() -> {
            try {
                CatanGame.client.connect(5000, Config.IP, Config.TCP, Config.UDP);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: connection failed, inform the user
            }
        }).start();
    }

    public SpriteBatch batch;
    private GameBoardManager aGameBoardManager = new GameBoardManager();
    private SessionManager aSessionManager = new SessionManager(4);

    @Override
    public void create() {
        batch = new SpriteBatch();
        createBasicSkin();
        // Load the current account if cached
        account = AccountManager.getLocalAccount();
        // The first screen that shows up when the game starts
        Screen mainScreen = account == null ? new LoginScreen(this) : new MenuScreen(this);
        setScreen(mainScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }

    private void createBasicSkin() {
        // Create a font
        BitmapFont font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
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

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    }

    public void switchScreen(ScreenKind pScreenKind) {
        switch (pScreenKind) {
            case MAIN_MENU:
                setScreen(new MenuScreen(this));
                break;
            case BROWSE_GAMES:
                break;
            case CREATE_GAME:
                setScreen(new CreateScreen(this, new MenuScreen(this)));
            case IN_GAME:
                //this.setScreen(new CreateScreen(this, menuScreen));
                SessionController sc = new SessionController(aGameBoardManager, aSessionManager);
                SessionScreen screen = new SessionScreen(this);
                sc.setSessionScreen(screen);
                screen.setSessionController(sc);
                setScreen(screen);
                break;
            case LOBBY:
                setScreen(new LobbyScreen(this));
                break;
            case RESUME_GAME:
                break;
            default:
                break;
        }
    }

    public GameBoardManager getGameBoardManager() {
        return aGameBoardManager;
    }

    public SessionManager getSessionManager() {
        return aSessionManager;
    }
}
