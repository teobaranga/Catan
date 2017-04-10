package com.mygdx.catan;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.account.AccountManager;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.injection.component.AppComponent;
import com.mygdx.catan.injection.component.DaggerAppComponent;
import com.mygdx.catan.screens.lobby.LobbyScreen;
import com.mygdx.catan.screens.login.LoginScreen;
import com.mygdx.catan.screens.menu.MenuScreen;
import com.mygdx.catan.session.SessionScreen;

import java.io.IOException;

public class CatanGame extends Game {

    public static final AppComponent appComponent;

    /** The Client representing the current user */
    public static final Client client;

    /** The skin used throughout the whole game */
    @SuppressWarnings("LibGDXStaticResource")
    public static Skin skin;

    /** The account of the user currently logged in */
    public static Account account;

    static {
        appComponent = DaggerAppComponent.builder().build();

        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        KryoSerialization serialization = new KryoSerialization(kryo);
        client = new Client(8192, 9182, serialization);

        // Register request & response classes (needed for networking)
        // Must be registered in the same order in the server
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

    public Music menuMusic;

    public Sound clickSound;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Load the UI skin
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        clickSound = Gdx.audio.newSound(Gdx.files.internal("sound/buttonClick.mp3"));
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/menuMusic.mp3"));
        menuMusic.setLooping(true);

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

    public void switchScreen(ScreenKind pScreenKind) {
        switch (pScreenKind) {
            case MAIN_MENU:
                setScreen(new MenuScreen(this));
                break;
            case BROWSE_GAMES:
                break;
            case CREATE_GAME:
//                setScreen(new CreateScreen(this, new MenuScreen(this)));
            case IN_GAME:
                menuMusic.stop();
                setScreen(new SessionScreen(this));
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
}
