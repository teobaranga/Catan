package com.mygdx.catan.screens.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.SnapshotArray;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;
import com.mygdx.catan.request.LeaveGame;
import com.mygdx.catan.request.MarkAsReady;
import com.mygdx.catan.request.StartGame;
import com.mygdx.catan.response.GameResponse;
import com.mygdx.catan.response.MarkedAsReady;
import com.mygdx.catan.response.PlayerJoined;
import com.mygdx.catan.response.PlayerLeft;
import com.mygdx.catan.screens.lobby.chat.ChatMessage;
import com.mygdx.catan.ui.CatanWindow;

public class LobbyScreen implements Screen {

    private static final String TITLE = "Lobby";

    private final Listener lobbyListener;

    private final CatanGame game;

    private Stage stage;
    private Texture bg;

    /** The table that displays all the players currently in this lobby */
    private Table playersTable;

    private TextButton startGameButton;

    public LobbyScreen(CatanGame game) {
        this.game = game;
        lobbyListener = new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MarkedAsReady) {
                    Gdx.app.postRunnable(() -> {
                        final MarkedAsReady markedAsReady = (MarkedAsReady) object;
                        final String username = markedAsReady.getUsername();
                        System.out.println(username + " has been marked as ready.");

                        markPlayerAsReady(username);

                        if (markedAsReady.isReadyToStart() && startGameButton != null) {
                            startGameButton.setDisabled(false);
                        }
                    });
                } else if (object instanceof PlayerJoined) {
                    Gdx.app.postRunnable(() -> {
                        final String username = ((PlayerJoined) object).username;
                        System.out.printf("%s has joined the game\n", username);
                        addPlayer(username);
                    });
                } else if (object instanceof PlayerLeft) {
                    Gdx.app.postRunnable(() -> {
                        final String username = ((PlayerLeft) object).username;
                        System.out.printf("%s has left the game\n", username);
                        removePlayer(username);
                    });
                } else if (object instanceof GameResponse) {
                    Gdx.app.postRunnable(() -> {
                        // Update the current game with the one received from the server, which
                        // contains the session
                        GameManager.getInstance().setCurrentGame(((GameResponse) object).getGame());
                        System.out.printf("Game started! Session: %s\n", GameManager.getInstance().getCurrentGame().session);
                        game.switchScreen(ScreenKind.IN_GAME);
                    });
                }
            }
        };
    }

    @Override
    public void show() {
        CatanGame.client.addListener(lobbyListener);

        final Game currentGame = GameManager.getInstance().getCurrentGame();
        if (currentGame != null && currentGame.getPlayerCount() == 1)
            System.out.printf("%s started a new game\n", currentGame.peers.keySet().iterator().next().getUsername());

        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create & add the window
        CatanWindow window = new CatanWindow(TITLE + " : " + CatanGame.client.getRemoteAddressTCP(), CatanGame.skin);
        // TODO: Closing the lobby window should bring the user back to the MainMenu
        window.setWindowCloseListener(() -> {
            game.switchScreen(ScreenKind.MAIN_MENU);
            CatanGame.client.sendTCP(LeaveGame.newInstance(CatanGame.account.getUsername()));
        });
//        window.debugAll();

        // Create the container table
        Table contentTable = new Table(CatanGame.skin);
//        contentTable.background(game.skin.newDrawable("background", Color.RED));
//        contentTable.debugAll();

        // Create the left table, which contains the map & game rules
        Table mapAndRules = new Table(CatanGame.skin);
        mapAndRules.top();
//        mapAndRules.debugAll();
//        mapAndRules.background(CatanGame.skin.newDrawable("background", Color.BLUE));
        Label mapAndRulesLabel = new Label(" Maps & Rules", CatanGame.skin, "lobby-header");
        mapAndRules.add(mapAndRulesLabel).expandX().fillX();

        // Create the right table, which contains the list of players along with the chat
        Table playersAndChatTable = new Table(CatanGame.skin);

        // Create the players table
        playersTable = new Table(CatanGame.skin).top();
        Label playersLabel = new Label(" Players", CatanGame.skin, "lobby-header");
        playersTable.add(playersLabel).expandX().fillX();
        if (currentGame != null) {
            for (Account account : currentGame.peers.keySet()) {
                addPlayer(account.getUsername());
            }
        }

        // Create the chat table
        Table chatTable = new Table(CatanGame.skin).top();
        Label chatLabel = new Label(" Chat", CatanGame.skin, "lobby-header");
        chatTable.add(chatLabel).expandX().fillX();
        chatTable.row();
        setupChat(chatTable);

        playersAndChatTable.add(playersTable).height(200).expandX().fillX();
        playersAndChatTable.row();
        playersAndChatTable.add(chatTable).expand().fill();
//        chatTable.pack();

        TextButton ready = new TextButton("Ready", CatanGame.skin);
        ready.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ready.setDisabled(true);
                CatanGame.client.sendTCP(MarkAsReady.newInstance(CatanGame.account.getUsername()));
            }
        });

//        contentTable.top().left();
        contentTable.add(mapAndRules).width(400).height(400).expandX();
        contentTable.add(playersAndChatTable).width(400).height(400).expandX().fillX();
        contentTable.row();

        // In case the player is the admin, allow him/her to decide when to start the game
        if (currentGame != null && CatanGame.account.equals(currentGame.getAdmin())) {
            // Add the ready button on the left
            contentTable.add(ready).height(50).width(400).padTop(20);

            // Add the start game button on the right
            startGameButton = new TextButton("Start Game", CatanGame.skin);
            startGameButton.padLeft(20).padRight(20);
            startGameButton.setDisabled(true);
            startGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // TODO start the game
                    CatanGame.client.sendTCP(StartGame.newInstance(CatanGame.account.getUsername()));
                }
            });
            contentTable.add(startGameButton).height(50).padTop(20).padRight(40).right();
        } else {
            // Add only the ready button, centered on its row
            contentTable.add(ready).height(50).width(400).padTop(20).colspan(2);
        }

//        contentTable.pack();
        window.row();
        window.add(contentTable).expandX().fillX();
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
    public void resume() {
        // Nothing to do
    }

    @Override
    public void pause() {
        // Nothing to do
    }

    @Override
    public void hide() {
        CatanGame.client.removeListener(lobbyListener);
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        stage.dispose();
    }

    /**
     * Add a player to the players table
     *
     * @param username username of the player
     */
    private void addPlayer(String username) {
        // Create a table that holds a label containing the player's name
        // The table seems a bit overkill but it's much easier to change its background
        final Table table = new Table(CatanGame.skin);
        final Label label = new Label(username, CatanGame.skin);
        table.add(label);
        playersTable.row();
        playersTable.add(table).pad(10).expandX().fillX();
    }

    /**
     * Visually indicate that a player has been marked as ready.
     *
     * @param username username of the ready player
     */
    private void markPlayerAsReady(String username) {
        // Find the table row containing the player that has been marked as ready
        final SnapshotArray<Actor> children = playersTable.getChildren();
        // Ignore the first child since it's just the table header
        for (int i = 1; i < children.size; i++) {
            Actor actor = children.get(i);
            if (actor instanceof Table) {
                final Actor label = ((Table) actor).getCells().get(0).getActor();
                if (label instanceof Label) {
                    if (((Label) label).getText().toString().equals(username)) {
                        ((Table) actor).setBackground("readyBackground");
                        break;
                    }
                }
            }
        }
    }

    /**
     * Remove a player from the list of players as a result of him/her leaving
     * the game.
     *
     * @param username username of the player
     */
    private void removePlayer(String username) {
        Table playerTable = null;
        // Find the table row containing the player that left the game
        final SnapshotArray<Actor> children = playersTable.getChildren();
        // Ignore the first child since it's just the table header
        for (int i = 1; i < children.size; i++) {
            Actor actor = children.get(i);
            if (actor instanceof Table) {
                final Actor label = ((Table) actor).getCells().get(0).getActor();
                if (label instanceof Label) {
                    if (((Label) label).getText().toString().equals(username)) {
                        playerTable = ((Table) actor);
                        break;
                    }
                }
            }
        }
        if (playerTable != null)
            playersTable.removeActor(playerTable);
    }

    private void setupChat(Table chatTable) {
        final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        String reallyLongString = "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n"
                + "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n"
                + "This\nIs\nA\nReally\nLong\nString\nThat\nHas\nLots\nOf\nLines\nAnd\nRepeats.\n";
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi feugiat mollis nisl, a egestas enim aliquet et. Fusce rhoncus non erat ultricies fringilla. Aliquam auctor est ut neque congue accumsan. Aliquam sed ligula metus. Phasellus luctus arcu sodales leo dictum, vitae iaculis justo pretium. Sed eget vestibulum tortor. Vivamus euismod et libero at tempus. Phasellus ac dolor in urna efficitur laoreet id eu massa. In sit amet nulla at turpis fermentum porttitor nec nec lectus. Nullam ornare nisl at purus venenatis luctus. Suspendisse potenti. Donec finibus sem ac malesuada luctus. Pellentesque auctor felis at feugiat posuere. Duis sollicitudin, urna ut tristique commodo, dolor sapien consectetur tortor, ac convallis ante odio in sem.";
        final ChatMessage msg = new ChatMessage("Sender", lorem);
        final ChatMessage msg2 = new ChatMessage("Sender", lorem);
//        final Label text = new Label(reallyLongString, skin);
//        text.setAlignment(Align.center);
//        text.setWrap(true);
//        final Label text2 = new Label("This is a short string!", skin);
//        text2.setAlignment(Align.center);
//        text2.setWrap(true);
//        final Label text3 = new Label(reallyLongString, skin);
//        text3.setAlignment(Align.center);
//        text3.setWrap(true);

        final Table scrollTable = new Table();
        scrollTable.left().top();
        scrollTable.add(msg).expandX().fillX();
        scrollTable.row();
        scrollTable.add(msg2).expandX().fillX();
        scrollTable.row();
//        scrollTable.add(text2);
//        scrollTable.row();
//        scrollTable.add(text3);

        final ScrollPane scroller = new ScrollPane(scrollTable);

//        chatTable.setFillParent(true);
        chatTable.add(scroller).fill().expand();
    }
}
