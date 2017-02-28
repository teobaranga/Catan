package com.mygdx.catan.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.Config;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.request.*;
import com.mygdx.catan.response.GameResponse;
import com.mygdx.catan.response.LoginResponse;
import com.mygdx.catan.response.MarkedAsReady;
import com.mygdx.catan.response.Response;
import com.mygdx.catan.session.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatanServer {

    /** Name of the file storing the game accounts */
    private static final String ACCOUNTS_DB = "accounts.bin";

    /**
     * Map connecting any given account (by name) to its game.
     * For the moment, a player is limited to only one game.
     */
    private static Map<String, Game> gamesMap;

    private static List<Account> defaultAccounts;
    private static List<Account> accounts;

    static {
        // Create the default accounts
        defaultAccounts = new ArrayList<>();
        defaultAccounts.add(new Account("aina", null));
        defaultAccounts.add(new Account("amanda", null));
        defaultAccounts.add(new Account("arnaud", null));
        defaultAccounts.add(new Account("emma", null));
        defaultAccounts.add(new Account("teo", null));
        defaultAccounts.add(new Account("teo test", null));
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();

        // Register request & response classes (needed for networking)
        // Must be registered in the same order in the client
        Kryo kryo = server.getKryo();
        kryo.register(Account.class);
        kryo.register(Game.class);
        kryo.register(Session.class);
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(JoinRandomGame.class);
        kryo.register(CreateGame.class);
        kryo.register(GameResponse.class);
        kryo.register(MarkAsReady.class);
        kryo.register(MarkedAsReady.class);
        kryo.register(ForwardedRequest.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);

        // Create the default accounts if they don't exist
        File accountsDatabase = new File(ACCOUNTS_DB);
        if (!accountsDatabase.exists()) {
            Output output = new Output(new FileOutputStream(ACCOUNTS_DB));
            kryo.writeObject(output, defaultAccounts);
            output.close();
        }

        // Load the accounts database
        Input input = new Input(new FileInputStream(ACCOUNTS_DB));
        //noinspection unchecked
        accounts = kryo.readObject(input, ArrayList.class);
        input.close();

        // Load the games TODO complete this
        gamesMap = new HashMap<>();

        // Start the server
        server.start();
        server.bind(Config.TCP, Config.UDP);

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                // Print the client's ID
                System.out.println(connection.getID());
            }

            @Override
            public void received(Connection connection, Object object) {
                Response response = null;

                if (object instanceof LoginRequest) {
                    // Attempt login
                    response = attemptLogin(((LoginRequest) object));
                } else if (object instanceof JoinRandomGame) {
                    response = getRandomGame();
                } else if (object instanceof CreateGame) {
                    response = createNewGame(connection, ((CreateGame) object).account);
                } else if (object instanceof MarkAsReady) {
                    // TODO: mark the player as ready
                    System.out.println(connection.getID() + " marked as ready");
                    response = new MarkedAsReady();
                } else if (object instanceof ForwardedRequest) {
                    final ForwardedRequest forwardedRequest = (ForwardedRequest) object;
                    // Get the peers of the client that sent the request
                    Game game = gamesMap.get(forwardedRequest.username);
                    for (Account peer : game.peers.keySet()) {
                        // Forward the object (message) to the other peers
                        if (!peer.getUsername().equals(forwardedRequest.username)) {
                            server.sendToTCP(game.peers.get(peer), object);
                        }
                    }
                }

                // Send the response
                if (response != null)
                    connection.sendTCP(response);
            }
        });
    }

    private static LoginResponse attemptLogin(LoginRequest request) {
        // Check if the account exists
        boolean success = accounts.contains(request.account);
        // Reply to the client with the success status
        final LoginResponse response = new LoginResponse();
        response.success = success;
        return response;
    }

    private static GameResponse getRandomGame() {
        final GameResponse response = new GameResponse();
        // Go through the games
        if (gamesMap != null) {
            for (Game game : gamesMap.values()) {
                // If there's a game currently waiting for more players, that's the one to return
                if (!game.inProgress() && game.getPlayerCount() < Config.MAX_PLAYERS) {
                    response.game = game;
                }
            }
        }
        return response;
    }

    private static GameResponse createNewGame(Connection connection, Account account) {
        final GameResponse response = new GameResponse();

        final Game game = new Game();
        game.peers.put(account, connection.getID());

        gamesMap.put(account.getUsername(), game);

        response.game = game;

        return response;
    }
}
