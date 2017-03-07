package com.mygdx.catan.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.catan.Config;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.request.*;
import com.mygdx.catan.response.*;
import com.mygdx.catan.session.Session;
import com.mygdx.catan.session.SessionManager;
import org.apache.commons.lang3.tuple.Pair;

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

    /**
     * Map connecting a game with the starting player, which is
     * the one who rolled the highest number during the initialization
     * phase.
     */
    private static Map<Game, Player> startingPlayer;

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
        Config.registerKryoClasses(kryo);

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
            }

            @Override
            public void disconnected(Connection connection) {
                final String username = getUsername(connection);
                if (username != null)
                    received(connection, LeaveGame.newInstance(username));
            }

            @Override
            public void received(Connection connection, Object object) {
                Response response = null;

                if (object instanceof LoginRequest) {
                    // Attempt login
                    response = attemptLogin(((LoginRequest) object));
                } else if (object instanceof JoinRandomGame) {
                    response = getRandomGame(connection, ((JoinRandomGame) object).account);
                } else if (object instanceof CreateGame) {
                    response = createNewGame(connection, ((CreateGame) object).account);
                }

                if (object instanceof ForwardedRequest) {
                    // Get the forwarded request
                    final ForwardedRequest forwardedRequest = (ForwardedRequest) object;
                    Object forwardedResponse = forwardedRequest;

                    // Get the peers of the client that sent the request
                    Game game = gamesMap.get(forwardedRequest.username);
                    if (game != null) {
                        // Process the request (optional)
                        if (forwardedRequest instanceof MarkAsReady) {
                            forwardedResponse = markAsReady(forwardedRequest.username);
                        } else if (forwardedRequest instanceof JoinRandomGame) {
                            forwardedResponse = PlayerJoined.newInstance(forwardedRequest.username);
                        } else if (forwardedRequest instanceof LeaveGame) {
                            disconnectFromGame(connection);
                            forwardedResponse = PlayerLeft.newInstance(forwardedRequest.username);
                        } else if (forwardedRequest instanceof StartGame) {
                            forwardedResponse = startGame(forwardedRequest.username);
                        } else if (forwardedRequest instanceof RollTwoDice) {
                            final RollTwoDice rollTwoDice = (RollTwoDice) forwardedRequest;
                            forwardedResponse = handleDiceRoll(rollTwoDice.username, rollTwoDice.getRollResult());
                        }

                        for (Account peer : game.peers.keySet()) {
                            // Skip the sender if the request isn't universal
                            if (peer.getUsername().equals(forwardedRequest.username) && !forwardedRequest.universal)
                                continue;
                            // Forward the object (message) to the peer
                            server.sendToTCP(game.peers.get(peer), forwardedResponse);
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
        // Reply to the client with the success status
        final LoginResponse response = new LoginResponse();
        // Check if the account exists
        response.success = accounts.contains(request.account);
        return response;
    }

    private static GameResponse getRandomGame(Connection connection, Account account) {
        Game randomGame = null;
        // Go through the games
        if (gamesMap != null) {
            for (Game game : gamesMap.values()) {
                // If there's a game currently waiting for more players, that's the one to return
                if (!game.isInProgress() && game.getPlayerCount() < Config.MAX_PLAYERS) {
                    // Add the player to the game
                    game.addPlayer(account, connection.getID());
                    gamesMap.put(account.getUsername(), game);
                    randomGame = game;
                }
            }
        }
        return GameResponse.newInstance(randomGame);
    }

    private static GameResponse createNewGame(Connection connection, Account account) {
        // Create the new game
        final Game game = new Game();
        game.addPlayer(account, connection.getID());

        // Associate the player with this game
        gamesMap.put(account.getUsername(), game);

        // Return the game response
        return GameResponse.newInstance(game);
    }

    private static MarkedAsReady markAsReady(String username) {
        final Game game = gamesMap.get(username);
        game.markAsReady(username);

        return MarkedAsReady.newInstance(username, game.isReadyToStart());
    }

    /**
     * Disconnect a player from a game and remove the game once the
     * last player has disconnected.
     *
     * @param connection connection of the player
     */
    private static void disconnectFromGame(Connection connection) {
        // Username of the disconnected player
        String username = null;

        for (Game game : gamesMap.values()) {
            // Disconnect only from games at the lobby stage
            if (!game.isInProgress()) {
                // Look for the player's connection ID
                Account acc = null;
                for (Map.Entry<Account, Integer> accountIntegerEntry : game.peers.entrySet()) {
                    if (accountIntegerEntry.getValue().equals(connection.getID())) {
                        acc = accountIntegerEntry.getKey();
                        username = acc.getUsername();
                        break;
                    }
                }
                if (acc != null) {
                    game.removePlayer(acc);
                    System.out.printf("Removed %s from his/her game\n", username);
                    break;
                }
            }
        }

        if (username != null) {
            gamesMap.remove(username);
            System.out.printf("Removed game from %s\n", username);
        }
    }

    /**
     * Start a game by creating a new session.
     *
     * @param username username of the admin player requesting to start the game
     */
    private static GameResponse startGame(String username) {
        // Get the game
        final Game game = gamesMap.get(username);
        // Create its session
        game.session = Session.newInstance(game.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        // Return the game response containing the game along with its session
        return GameResponse.newInstance(game);
    }

    private static DiceRolled handleDiceRoll(String username, Pair<Integer, Integer> dice) {
        final Game game = gamesMap.get(username);
        if (!game.isInProgress())
            return null;
        switch (game.session.currentPhase) {
            case SETUP_PHASE_ONE:
                final SessionManager sessionManager = SessionManager.getInstance(game.session);
                // Update the session manager with the index of the last player to roll the dice
                final int playerIndex = sessionManager.getPlayerIndex(username);
                sessionManager.updateDiceRollPlayersCount();
                // Update the highest-roll player if necessary
                // Compare the dice rolls
                final int roll = dice.getLeft() + dice.getRight();
                final int highestDiceRoll = sessionManager.getHighestDiceRoll();
                if (roll > highestDiceRoll) {
                    // Update the highest dice roll
                    sessionManager.setHighestDiceRoll(playerIndex, roll);
                }
                if (sessionManager.isRollDiceDone()) {
                    // All players are done rolling the dice
                    // Set the session's current player to the one with the highest dice roll
                    final int index = sessionManager.getHighestDiceRollPlayerIndex();
                    sessionManager.setFirstPlayer(index);
                    return DiceRolled.newInstance(username, dice, sessionManager.getSession());
                } else {
                    return DiceRolled.newInstance(username, dice);
                }
            default:
                return null;
        }
    }

    private static String getUsername(Connection connection) {
        for (Game game : gamesMap.values()) {
            for (Map.Entry<Account, Integer> accountIntegerEntry : game.peers.entrySet()) {
                if (accountIntegerEntry.getValue() == connection.getID()) {
                    return accountIntegerEntry.getKey().getUsername();
                }
            }
        }
        return null;
    }
}
