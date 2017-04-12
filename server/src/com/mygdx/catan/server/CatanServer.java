package com.mygdx.catan.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.catan.*;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.gameboard.*;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.*;
import com.mygdx.catan.request.game.BrowseGames;
import com.mygdx.catan.request.game.LoadGame;
import com.mygdx.catan.response.*;
import com.mygdx.catan.session.Session;
import com.mygdx.catan.session.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CatanServer {

    /** Name of the file storing the game accounts */
    private static final String ACCOUNTS_DB = "accounts.bin";

    private static final String SAVED_GAMES_DB = "games.bin";

    private static final List<Account> defaultAccounts;

    static {
        // Create the default accounts
        defaultAccounts = new ArrayList<>();
        defaultAccounts.add(new Account("aina", null));
        defaultAccounts.add(new Account("amanda", null));
        defaultAccounts.add(new Account("arnaud", null));
        defaultAccounts.add(new Account("emma", null));
        defaultAccounts.add(new Account("teo", null));
        defaultAccounts.add(new Account("teo test", null));

        defaultAccounts.add(new Account("Player 1", null));
        defaultAccounts.add(new Account("Player 2", null));
        defaultAccounts.add(new Account("Player 3", null));
    }

    private final Server server;

    /**
     * Map connecting any given account (by name) to its game.
     * For the moment, a player is limited to only one game.
     */
    private final Map<String, Game> gamesMap;

    private final List<Game> savedGames;

    private final List<Account> accounts;

    CatanServer() throws IOException {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        KryoSerialization serialization = new KryoSerialization(kryo);
        server = new Server(16384, 9182, serialization);

        // Register request & response classes (needed for networking)
        // Must be registered in the same order in the client
        Config.registerKryoClasses(kryo);

        // Create the default accounts if they don't exist
        File accountsDatabase = new File(ACCOUNTS_DB);
        if (!accountsDatabase.exists()) {
            Output output = new Output(new FileOutputStream(ACCOUNTS_DB));
            kryo.writeObject(output, defaultAccounts);
            output.close();
        }

        // Create the test save games if they don't exist
        File saveGameDatabase = new File(SAVED_GAMES_DB);
//        if (!saveGameDatabase.exists()) {
            Output output = new Output(new FileOutputStream(SAVED_GAMES_DB));
            kryo.writeObject(output, createTestSavedGames());
            output.close();
//        }

        // Load the accounts database
        Input input = new Input(new FileInputStream(ACCOUNTS_DB));
        //noinspection unchecked
        accounts = kryo.readObject(input, ArrayList.class);
        input.close();

        // Load the test games
        input = new Input(new FileInputStream(SAVED_GAMES_DB));
        //noinspection unchecked
        savedGames = kryo.readObject(input, ArrayList.class);
        input.close();

        // Load the games
        gamesMap = new ConcurrentHashMap<>();

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

                System.out.println("message received");

                // Handle the messages sent to only one player
                if (object instanceof LoginRequest) {
                    // Attempt login
                    response = attemptLogin(((LoginRequest) object));
                } else if (object instanceof JoinRandomGame) {
                    response = getRandomGame(connection, ((JoinRandomGame) object).account);
                } else if (object instanceof CreateGame) {
                    response = createNewGame(connection, ((CreateGame) object).account);
                } else if (object instanceof BrowseGames) {
                    for (Game savedGame : savedGames) {
                        GameResponse gameResponse = GameResponse.newInstance(savedGame);
                        connection.sendTCP(gameResponse);
                    }
                    return;
                } else if (object instanceof LoadGame) {
                    // Get the username of the player that loaded the game
                    String username = ((LoadGame) object).getUsername();
                    String gameId = ((LoadGame) object).getGameId();

                    // Associated the player with the save game
                    for (Game savedGame : savedGames) {
                        // Find the game
                        if (savedGame.name.equals(gameId)) {
                            // Find the player
                            Account playerAccount = null;
                            for (Account account : savedGame.peers.keySet()) {
                                if (account.getUsername().equals(username)) {
                                    playerAccount = account;
                                    gamesMap.put(username, savedGame);
                                    break;
                                }
                            }
                            // Set its connection id so it can receive and send msgs
                            savedGame.peers.put(playerAccount, connection.getID());
                            System.out.println(String.format("Player %s %d loaded game %s", playerAccount.getUsername(), connection.getID(), savedGame.name));
                            return;
                        }
                    }
                }

                if (object instanceof TargetedRequest) {
                    final TargetedRequest targetedRequest = (TargetedRequest) object;
                    Object targetedResponse = targetedRequest;

                    // Get the peers of the client that sent the request
                    Game game = gamesMap.get(targetedRequest.sender);
                    if (game != null) {
                        if (targetedRequest instanceof TargetedChooseResourceCardRequest) {
                            targetedResponse = ChooseResourceCardRequest.newInstance(((TargetedChooseResourceCardRequest) targetedRequest).getNumberOfCards(), targetedRequest.sender);
                        }

                        for (Account peer : game.peers.keySet()) {
                            // send message when target is found
                            if (peer.getUsername().equals(targetedRequest.target)) {
                                // Forward the object (message) to the peer
                                server.sendToTCP(game.peers.get(peer), targetedResponse);
                                break;
                            }
                        }
                    }
                }

                // Handle the messages sent to multiple players
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
                        } else if (forwardedRequest instanceof RollDice) {
                            final RollDice rollTwoDice = (RollDice) forwardedRequest;
                            forwardedResponse = handleDiceRoll(rollTwoDice.username, rollTwoDice.getRollResult(), rollTwoDice.getEventDieResult());
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

    private LoginResponse attemptLogin(LoginRequest request) {
        // Reply to the client with the success status
        final LoginResponse response = new LoginResponse();
        // Check if the account exists
        response.success = accounts.contains(request.account);
        return response;
    }

    private GameResponse getRandomGame(Connection connection, Account account) {
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

    private GameResponse createNewGame(Connection connection, Account account) {
        // Create the new game
        final Game game = new Game();
        game.addPlayer(account, connection.getID());

        // Associate the player with this game
        gamesMap.put(account.getUsername(), game);

        // Return the game response
        return GameResponse.newInstance(game);
    }

    private MarkedAsReady markAsReady(String username) {
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
    private void disconnectFromGame(Connection connection) {
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
     * Start a game by creating a new session and a new gameboard.
     *
     * @param username username of the admin player requesting to start the game
     */
    private GameResponse startGame(String username) {
        // Get the game
        final Game game = gamesMap.get(username);
        // Create its session
        game.session = Session.newInstance(game.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        // Create its gameboard
        game.gameboard = GameBoard.newInstance(BoardVariants.BOARD_VARIANT1); //TODO make default again
        System.out.println("gameboard created");
        // Return the game response containing the game along with its session and its gameboard
        return GameResponse.newInstance(game);
    }

    private DiceRolled handleDiceRoll(String username, DiceRollPair dice, EventKind event) {
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
                final int roll = dice.getRed() + dice.getYellow();
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
                    return DiceRolled.newInstance(username, dice, sessionManager.getSession(), event);
                } else {
                    return DiceRolled.newInstance(username, dice, event);
                }
            case TURN_FIRST_PHASE:
                return DiceRolled.newInstance(username, dice, event);
            default:
                return null;
        }
    }

    private String getUsername(Connection connection) {
        for (Game game : gamesMap.values()) {
            for (Map.Entry<Account, Integer> accountIntegerEntry : game.peers.entrySet()) {
                if (accountIntegerEntry.getValue() == connection.getID()) {
                    return accountIntegerEntry.getKey().getUsername();
                }
            }
        }
        return null;
    }

    private List<Game> createTestSavedGames() {
        List<Game> savedGames = new ArrayList<>();

        Account player1 = defaultAccounts.get(6);
        Account player2 = defaultAccounts.get(7);
        Account player3 = defaultAccounts.get(8);

        Account[] accounts = {player1, player2, player3};

        // Years of plenty saved game ----------------------------------------------
        Game yearsOfPlenty = new Game();
        yearsOfPlenty.name = "Years of Plenty";
        for (Account account : accounts) {
            yearsOfPlenty.addPlayer(account, -1);
            yearsOfPlenty.markAsReady(account.getUsername());
        }
        yearsOfPlenty.session = Session.newInstance(yearsOfPlenty.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        yearsOfPlenty.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);
        yearsOfPlenty.session.currentPhase = GamePhase.TURN_FIRST_PHASE;


        Player[] yearsOfPlentyPlayers = yearsOfPlenty.session.getPlayers();
        {
            Player p1 = yearsOfPlentyPlayers[0];
            {
                {
                    ResourceMap resourceMapP1 = new ResourceMap();

                    resourceMapP1.put(ResourceKind.WOOD, 5);
                    resourceMapP1.put(ResourceKind.CLOTH, 4);
                    resourceMapP1.put(ResourceKind.WOOL, 5);
                    resourceMapP1.put(ResourceKind.COIN, 3);
                    resourceMapP1.put(ResourceKind.ORE, 4);
                    resourceMapP1.put(ResourceKind.BRICK, 4);
                    resourceMapP1.put(ResourceKind.GRAIN, 5);
                    resourceMapP1.put(ResourceKind.PAPER, 2);
                    p1.setResources(resourceMapP1);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p1, position);
                    village.setVillageKind(VillageKind.CITY);
                    p1.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(4, -4, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(4, -2, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(5, -1, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(5, 1, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(4, -2, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(5, -1, yearsOfPlenty.gameboard);
                    EdgeUnit ship = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.SHIP, p1);
                    p1.addEdgeUnit(ship);
                    yearsOfPlenty.gameboard.addRoadOrShip(ship);
                }
                {
                    //ADD SETTLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p1, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p1.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(5, 1, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p1, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p1.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    //ADD ROAD
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                Player p2 = yearsOfPlentyPlayers[1];
                {
                    ResourceMap resourceMapP2 = new ResourceMap();

                    resourceMapP2.put(ResourceKind.WOOD, 4);
                    resourceMapP2.put(ResourceKind.CLOTH, 5);
                    resourceMapP2.put(ResourceKind.WOOL, 4);
                    resourceMapP2.put(ResourceKind.COIN, 4);
                    resourceMapP2.put(ResourceKind.ORE, 3);
                    resourceMapP2.put(ResourceKind.BRICK, 5);
                    resourceMapP2.put(ResourceKind.GRAIN, 3);
                    resourceMapP2.put(ResourceKind.PAPER, 2);
                    p1.setResources(resourceMapP2);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p2, position);
                    village.setVillageKind(VillageKind.CITY);
                    p2.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);

                }
                {
                    //ADD SETELLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p2, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p2.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    //ADD ROAD!
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                Player p3 = yearsOfPlentyPlayers[2];
                {
                    ResourceMap resourceMapP3 = new ResourceMap();

                    resourceMapP3.put(ResourceKind.WOOD, 3);
                    resourceMapP3.put(ResourceKind.CLOTH, 4);
                    resourceMapP3.put(ResourceKind.WOOL, 4);
                    resourceMapP3.put(ResourceKind.COIN, 3);
                    resourceMapP3.put(ResourceKind.ORE, 3);
                    resourceMapP3.put(ResourceKind.BRICK, 5);
                    resourceMapP3.put(ResourceKind.GRAIN, 4);
                    resourceMapP3.put(ResourceKind.PAPER, 2);
                    p1.setResources(resourceMapP3);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-0, -2, yearsOfPlenty.gameboard);
                    int id = yearsOfPlenty.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(p3, position, id);
                    p3.addKnight(knight);
                    yearsOfPlenty.gameboard.addKnight(knight, knight.getId());
                }
                {
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, yearsOfPlenty.gameboard);
                    int id = yearsOfPlenty.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(p3, position, id);
                    p3.addKnight(knight);
                    yearsOfPlenty.gameboard.addKnight(knight, knight.getId());
                }
                {
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p3, position);
                    village.setVillageKind(VillageKind.CITY);
                    p3.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    //ADD SETETLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, yearsOfPlenty.gameboard);
                    Village village = Village.newInstance(p3, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p3.addVillage(village);
                    yearsOfPlenty.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    //ADD ROAD
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, yearsOfPlenty.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, yearsOfPlenty.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    yearsOfPlenty.gameboard.addRoadOrShip(road);
                }

            }
        }

        // savedGames.add(yearsOfPlenty);

        // ---------------------------------------------------------------------------
        // Progress Card game : 1 ----------------------------------------------------
        // ---------------------------------------------------------------------------

        Game progressCardGame = new Game();
        progressCardGame.name = "Progress Card Game";
        for (Account account : accounts) {
            progressCardGame.addPlayer(account, -1);
            progressCardGame.markAsReady(account.getUsername());
        }
        progressCardGame.session = Session.newInstance(progressCardGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        progressCardGame.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);
        progressCardGame.session.currentPhase = GamePhase.TURN_FIRST_PHASE;

        // barbarians have attacked at least once
        progressCardGame.session.firstBarbarianAttack = true;

        Player[] progressCardplayers = progressCardGame.session.getPlayers();
        Player progressCardPlayer1 = progressCardplayers[0];
        Player progressCardPlayer2 = progressCardplayers[1];
        Player progressCardPlayer3 = progressCardplayers[2];

        {
            /**
             * player 1 ---------- trade
             * - Alchemist
             * - commercial harbor
             * - spy
             * - saboteur
             */
            progressCardPlayer1.addProgressCard(ProgressCardType.ALCHEMIST);
            progressCardPlayer1.addProgressCard(ProgressCardType.COMMERCIALHARBOUR);
            progressCardPlayer1.addProgressCard(ProgressCardType.SPY);
            progressCardPlayer1.addProgressCard(ProgressCardType.SABOTEUR);

            // set flipchart levels: science : 3 - politics : 2 - trade : 5
            progressCardPlayer1.getCityImprovements().setScienceLevel(3);
            progressCardPlayer1.getCityImprovements().setPoliticsLevel(2);
            progressCardPlayer1.getCityImprovements().setTradeLevel(5);

            // place villages and edges on appropriate intersections
            {
                CoordinatePair Mfour_Meight = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, progressCardGame.gameboard);
                CoordinatePair Mthree_Mseven = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, progressCardGame.gameboard);
                CoordinatePair Mone_Mone = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, progressCardGame.gameboard);
                CoordinatePair Mone_one = GameBoardManager.getCoordinatePairFromCoordinates(-1, 1, progressCardGame.gameboard);

                Village vil1 = Village.newInstance(progressCardPlayer1, Mfour_Meight);
                Mfour_Meight.putVillage(vil1);
                Village vil2 = Village.newInstance(progressCardPlayer1, Mone_Mone);
                vil2.setVillageKind(VillageKind.TRADE_METROPOLIS);
                progressCardGame.session.tradeMetropolisOwner = progressCardPlayer1;
                Mone_Mone.putVillage(vil2);
                EdgeUnit edg1 = EdgeUnit.newEdgeUnit(Mfour_Meight, Mthree_Mseven, EdgeUnitKind.ROAD, progressCardPlayer1);
                EdgeUnit edg2 = EdgeUnit.newEdgeUnit(Mone_one, Mone_Mone, EdgeUnitKind.ROAD, progressCardPlayer1);

                progressCardPlayer1.addVillage(vil1);
                progressCardPlayer1.addVillage(vil2);
                progressCardPlayer1.addEdgeUnit(edg1);
                progressCardPlayer1.addEdgeUnit(edg2);

                progressCardGame.gameboard.addRoadOrShip(edg1);
                progressCardGame.gameboard.addRoadOrShip(edg2);
                progressCardGame.gameboard.addVillage(vil1);
                progressCardGame.gameboard.addVillage(vil2);
            }

            // give resources
            ResourceMap progressCardPlayer1Map = new ResourceMap();
            progressCardPlayer1Map.put(ResourceKind.WOOD, 5);
            progressCardPlayer1Map.put(ResourceKind.CLOTH, 3);
            progressCardPlayer1Map.put(ResourceKind.WOOL, 2);
            progressCardPlayer1.setResources(progressCardPlayer1Map);

            /**
             * player 2 ---------- science
             * - roadbuilding
             * - medicine
             * - engineer
             * - resource monopoly
             */
            progressCardPlayer2.addProgressCard(ProgressCardType.ROADBUILDING);
            progressCardPlayer2.addProgressCard(ProgressCardType.MEDICINE);
            progressCardPlayer2.addProgressCard(ProgressCardType.ENGINEER);
            progressCardPlayer2.addProgressCard(ProgressCardType.RESOURCEMONOPOLY);

            // set flipchart levels: science : 5 - politics : 0 - trade : 1
            progressCardPlayer2.getCityImprovements().setScienceLevel(5);
            progressCardPlayer2.getCityImprovements().setPoliticsLevel(0);
            progressCardPlayer2.getCityImprovements().setTradeLevel(1);

            // place villages and edges on appropriate intersections
            {
                CoordinatePair one_seven = GameBoardManager.getCoordinatePairFromCoordinates(1, 7, progressCardGame.gameboard);
                CoordinatePair one_five = GameBoardManager.getCoordinatePairFromCoordinates(1, 5, progressCardGame.gameboard);
                CoordinatePair Mthree_Mone = GameBoardManager.getCoordinatePairFromCoordinates(-3, -1, progressCardGame.gameboard);
                CoordinatePair Mthree_one = GameBoardManager.getCoordinatePairFromCoordinates(-3, 1, progressCardGame.gameboard);
                CoordinatePair Mtwo_two = GameBoardManager.getCoordinatePairFromCoordinates(-2, 2, progressCardGame.gameboard);
                CoordinatePair Mtwo_four = GameBoardManager.getCoordinatePairFromCoordinates(-2, 4, progressCardGame.gameboard);
                CoordinatePair Mone_five = GameBoardManager.getCoordinatePairFromCoordinates(-1, 5, progressCardGame.gameboard);
                CoordinatePair zero_four = GameBoardManager.getCoordinatePairFromCoordinates(0, 4, progressCardGame.gameboard);
                CoordinatePair Mthree_Mfive = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, progressCardGame.gameboard);
                CoordinatePair Mtwo_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, progressCardGame.gameboard);

                Village vil1 = Village.newInstance(progressCardPlayer2, one_seven);
                one_seven.putVillage(vil1);
                vil1.setVillageKind(VillageKind.CITY);
                Village vil2 = Village.newInstance(progressCardPlayer2, Mthree_Mone);
                vil2.setVillageKind(VillageKind.SCIENCE_METROPOLIS);
                progressCardGame.session.scienceMetropolisOwner = progressCardPlayer2;
                Mthree_Mone.putVillage(vil2);
                Village vil3 = Village.newInstance(progressCardPlayer2, Mthree_Mfive);
                Mthree_Mfive.putVillage(vil3);

                EdgeUnit edg1 = EdgeUnit.newEdgeUnit(one_five, one_seven, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg2 = EdgeUnit.newEdgeUnit(Mthree_one, Mthree_Mone, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg3 = EdgeUnit.newEdgeUnit(Mthree_one, Mtwo_two, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg4 = EdgeUnit.newEdgeUnit(Mtwo_two, Mtwo_four, EdgeUnitKind.SHIP, progressCardPlayer2);
                EdgeUnit edg5 = EdgeUnit.newEdgeUnit(Mtwo_four, Mone_five, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg6 = EdgeUnit.newEdgeUnit(Mone_five, zero_four, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg7 = EdgeUnit.newEdgeUnit(one_five, zero_four, EdgeUnitKind.ROAD, progressCardPlayer2);
                EdgeUnit edg8 = EdgeUnit.newEdgeUnit(Mtwo_Mfour, Mthree_Mfive, EdgeUnitKind.ROAD, progressCardPlayer2);

                progressCardPlayer2.addVillage(vil1);
                progressCardPlayer2.addVillage(vil2);
                progressCardPlayer2.addVillage(vil3);
                progressCardPlayer2.addEdgeUnit(edg1);
                progressCardPlayer2.addEdgeUnit(edg2);
                progressCardPlayer2.addEdgeUnit(edg3);
                progressCardPlayer2.addEdgeUnit(edg4);
                progressCardPlayer2.addEdgeUnit(edg5);
                progressCardPlayer2.addEdgeUnit(edg6);
                progressCardPlayer2.addEdgeUnit(edg7);
                progressCardPlayer2.addEdgeUnit(edg8);
                progressCardGame.gameboard.addRoadOrShip(edg1);
                progressCardGame.gameboard.addRoadOrShip(edg2);
                progressCardGame.gameboard.addRoadOrShip(edg3);
                progressCardGame.gameboard.addRoadOrShip(edg4);
                progressCardGame.gameboard.addRoadOrShip(edg5);
                progressCardGame.gameboard.addRoadOrShip(edg6);
                progressCardGame.gameboard.addRoadOrShip(edg7);
                progressCardGame.gameboard.addRoadOrShip(edg8);
                progressCardGame.gameboard.addVillage(vil1);
                progressCardGame.gameboard.addVillage(vil2);
                progressCardGame.gameboard.addVillage(vil3);

                // set longest road owner
                progressCardGame.session.longestRoadOwner = progressCardPlayer2;

                // add knights
                {
                    int id = progressCardGame.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(progressCardPlayer2, Mtwo_two, id);
                    progressCardPlayer2.addKnight(knight);
                    progressCardGame.gameboard.addKnight(knight, knight.getId());
                }
            }

            // give resources
            ResourceMap progressCardPlayer2Map = new ResourceMap();
            progressCardPlayer2Map.put(ResourceKind.BRICK, 1);
            progressCardPlayer2Map.put(ResourceKind.WOOD, 2);
            progressCardPlayer2Map.put(ResourceKind.GRAIN, 1);
            progressCardPlayer2Map.put(ResourceKind.PAPER, 1);
            progressCardPlayer2.setResources(progressCardPlayer2Map);

            /**
             * player 3 ---------- politics
             * - warlord
             * - inventor
             * - irrigation
             * - wedding
             * */
            progressCardPlayer3.addProgressCard(ProgressCardType.WARLORD);
            progressCardPlayer3.addProgressCard(ProgressCardType.INVENTOR);
            progressCardPlayer3.addProgressCard(ProgressCardType.IRRIGATION);
            progressCardPlayer3.addProgressCard(ProgressCardType.WEDDING);

            // set flipchart levels: science : 2 - politics : 3 - trade : 0
            progressCardPlayer3.getCityImprovements().setScienceLevel(2);
            progressCardPlayer3.getCityImprovements().setPoliticsLevel(3);
            progressCardPlayer3.getCityImprovements().setTradeLevel(0);

            // place villages and edges on appropriate intersections
            {
                CoordinatePair zero_Mten = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, progressCardGame.gameboard);
                CoordinatePair two_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(2, -4, progressCardGame.gameboard);
                CoordinatePair zero_Meight = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, progressCardGame.gameboard);
                CoordinatePair two_Mtwo = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, progressCardGame.gameboard);
                CoordinatePair one_Mone = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, progressCardGame.gameboard);

                Village vil1 = Village.newInstance(progressCardPlayer3, zero_Mten);
                zero_Mten.putVillage(vil1);
                Village vil2 = Village.newInstance(progressCardPlayer3, two_Mfour);
                vil2.setVillageKind(VillageKind.CITY);
                two_Mfour.putVillage(vil2);
                EdgeUnit edg1 = EdgeUnit.newEdgeUnit(zero_Meight, zero_Mten, EdgeUnitKind.ROAD, progressCardPlayer3);
                EdgeUnit edg2 = EdgeUnit.newEdgeUnit(two_Mtwo, two_Mfour, EdgeUnitKind.ROAD, progressCardPlayer3);
                EdgeUnit edg3 = EdgeUnit.newEdgeUnit(one_Mone, two_Mtwo, EdgeUnitKind.ROAD, progressCardPlayer3);

                // add knights
                {
                    int id = progressCardGame.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(progressCardPlayer3, one_Mone, id);
                    progressCardPlayer3.addKnight(knight);
                    progressCardGame.gameboard.addKnight(knight, knight.getId());
                }
                {
                    int id = progressCardGame.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(progressCardPlayer3, two_Mtwo, id);
                    progressCardPlayer3.addKnight(knight);
                    progressCardGame.gameboard.addKnight(knight, knight.getId());
                }

                progressCardPlayer3.addVillage(vil1);
                progressCardPlayer3.addVillage(vil2);
                progressCardPlayer3.addEdgeUnit(edg1);
                progressCardPlayer3.addEdgeUnit(edg2);
                progressCardPlayer3.addEdgeUnit(edg3);
                progressCardGame.gameboard.addRoadOrShip(edg1);
                progressCardGame.gameboard.addRoadOrShip(edg2);
                progressCardGame.gameboard.addRoadOrShip(edg3);
                progressCardGame.gameboard.addVillage(vil1);
                progressCardGame.gameboard.addVillage(vil2);
            }

            // give resources
            ResourceMap progressCardPlayer3Map = new ResourceMap();
            progressCardPlayer3Map.put(ResourceKind.ORE, 1);
            progressCardPlayer3Map.put(ResourceKind.COIN, 2);
            progressCardPlayer3.setResources(progressCardPlayer3Map);
        }


        /** remove cards from gameboard stack */
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.ALCHEMIST, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ALCHEMIST));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.COMMERCIALHARBOUR, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.COMMERCIALHARBOUR));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.SPY, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.SPY));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.SABOTEUR, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.SABOTEUR));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.ROADBUILDING, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ROADBUILDING));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.MEDICINE, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.MEDICINE));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.ENGINEER, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ENGINEER));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.RESOURCEMONOPOLY, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.RESOURCEMONOPOLY));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.WARLORD, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.WARLORD));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.INVENTOR, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.INVENTOR));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.IRRIGATION, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.IRRIGATION));
        progressCardGame.gameboard.removeProgressCard(ProgressCardType.WEDDING, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.WEDDING));

        //savedGames.add(progressCardGame);


        // ---------------------------------------------------------------------------
        // Progress Card game : 2 ----------------------------------------------------
        // ---------------------------------------------------------------------------

        Game progressCardGame2 = new Game();
        progressCardGame2.name = "Progress Card Game 2";
        for (Account account: accounts) {
            progressCardGame2.addPlayer(account, -1);
            progressCardGame2.markAsReady(account.getUsername());
        }
        progressCardGame2.session = Session.newInstance(progressCardGame2.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        progressCardGame2.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);
        progressCardGame2.session.currentPhase = GamePhase.TURN_FIRST_PHASE;

        // barbarians have attacked at least once
        progressCardGame2.session.firstBarbarianAttack = true;

        Player[] pc2players = progressCardGame2.session.getPlayers();
        Player pc2p1 = pc2players[0];
        Player pc2p2 = pc2players[1];
        Player pc2p3 = pc2players[2];

        // place merchant and robber, set merchant owner
        progressCardGame2.gameboard.setMerchantOwner(pc2p2);
        progressCardGame2.gameboard.setMerchantPosition(GameBoardManager.getHexFromCoordinates(0, 2, progressCardGame2.gameboard));
        progressCardGame2.gameboard.setRobberPosition(GameBoardManager.getHexFromCoordinates(0, 0, progressCardGame2.gameboard));


        /**
         * player 1 ---------- trade
         * - bishop
         * - deserter
         * - diplomat
         * - intrigue
         */
        pc2p1.addProgressCard(ProgressCardType.BISHOP);
        pc2p1.addProgressCard(ProgressCardType.DESERTER);
        pc2p1.addProgressCard(ProgressCardType.DIPLOMAT);
        pc2p1.addProgressCard(ProgressCardType.INTRIGUE);


        // set flipchart levels: science : 0 - politics : 5 - trade : 0
        pc2p1.getCityImprovements().setScienceLevel(0);
        pc2p1.getCityImprovements().setPoliticsLevel(5);
        pc2p1.getCityImprovements().setTradeLevel(0);

        // place villages and edges on appropriate intersections
        {
            CoordinatePair Mfour_Meight = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, progressCardGame2.gameboard);
            CoordinatePair Mthree_Mseven = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, progressCardGame2.gameboard);
            CoordinatePair Mone_Mone = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, progressCardGame2.gameboard);
            CoordinatePair Mone_one = GameBoardManager.getCoordinatePairFromCoordinates(-1, 1, progressCardGame2.gameboard);
            CoordinatePair zero_two = GameBoardManager.getCoordinatePairFromCoordinates(0, 2, progressCardGame2.gameboard);
            CoordinatePair Mtwo_two = GameBoardManager.getCoordinatePairFromCoordinates(-2, 2, progressCardGame2.gameboard);

            Village vil1 = Village.newInstance(pc2p1, Mfour_Meight);
            Mfour_Meight.putVillage(vil1);
            Village vil2 = Village.newInstance(pc2p1, Mone_Mone);
            vil2.setVillageKind(VillageKind.POLITICS_METROPOLIS);
            progressCardGame2.session.politicsMetropolisOwner = pc2p1;
            Mone_Mone.putVillage(vil2);
            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(Mfour_Meight, Mthree_Mseven, EdgeUnitKind.ROAD, pc2p1);
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(Mone_one, Mone_Mone, EdgeUnitKind.ROAD, pc2p1);
            EdgeUnit edg3 = EdgeUnit.newEdgeUnit(Mone_one, Mtwo_two, EdgeUnitKind.ROAD, pc2p1);
            EdgeUnit edg4 = EdgeUnit.newEdgeUnit(Mone_one, zero_two, EdgeUnitKind.ROAD, pc2p1);

            pc2p1.addVillage(vil1);
            pc2p1.addVillage(vil2);
            pc2p1.addEdgeUnit(edg1);
            pc2p1.addEdgeUnit(edg2);
            pc2p1.addEdgeUnit(edg3);
            pc2p1.addEdgeUnit(edg4);

            progressCardGame2.gameboard.addRoadOrShip(edg1);
            progressCardGame2.gameboard.addRoadOrShip(edg2);
            progressCardGame2.gameboard.addRoadOrShip(edg3);
            progressCardGame2.gameboard.addRoadOrShip(edg4);
            progressCardGame2.gameboard.addVillage(vil1);
            progressCardGame2.gameboard.addVillage(vil2);

            // add knights
            {
                int id = progressCardGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(pc2p1, Mthree_Mseven, id);
                pc2p1.addKnight(knight);
                progressCardGame.gameboard.addKnight(knight, knight.getId());
            } {
                int id = progressCardGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(pc2p1, zero_two, id);
                pc2p1.addKnight(knight);
                progressCardGame.gameboard.addKnight(knight, knight.getId());
            }
        }

        // give resources
        ResourceMap pc2p1Map = new ResourceMap();
        pc2p1Map.put(ResourceKind.WOOD, 5);
        pc2p1Map.put(ResourceKind.CLOTH, 3);
        pc2p1Map.put(ResourceKind.WOOL, 2);
        pc2p1.setResources(pc2p1Map);


        /**
         * player 2 ---------- science
         * - crane
         * - mining
         * - smith
         */
        pc2p2.addProgressCard(ProgressCardType.CRANE);
        pc2p2.addProgressCard(ProgressCardType.MINING);
        pc2p2.addProgressCard(ProgressCardType.SMITH);

        // set flipchart levels: science : 5 - politics : 0 - trade : 0
        pc2p2.getCityImprovements().setScienceLevel(5);
        pc2p2.getCityImprovements().setPoliticsLevel(0);
        pc2p2.getCityImprovements().setTradeLevel(0);

        // place villages and edges on appropriate intersections
        {
            CoordinatePair one_seven = GameBoardManager.getCoordinatePairFromCoordinates(1, 7, progressCardGame.gameboard);
            CoordinatePair one_five = GameBoardManager.getCoordinatePairFromCoordinates(1, 5, progressCardGame.gameboard);
            CoordinatePair Mthree_Mone = GameBoardManager.getCoordinatePairFromCoordinates(-3, -1, progressCardGame.gameboard);
            CoordinatePair Mthree_one = GameBoardManager.getCoordinatePairFromCoordinates(-3, 1, progressCardGame.gameboard);
            CoordinatePair Mtwo_two = GameBoardManager.getCoordinatePairFromCoordinates(-2, 2, progressCardGame.gameboard);
            CoordinatePair Mtwo_four = GameBoardManager.getCoordinatePairFromCoordinates(-2, 4, progressCardGame.gameboard);
            CoordinatePair Mone_five = GameBoardManager.getCoordinatePairFromCoordinates(-1, 5, progressCardGame.gameboard);
            CoordinatePair zero_four = GameBoardManager.getCoordinatePairFromCoordinates(0, 4, progressCardGame.gameboard);
            CoordinatePair Mthree_Mfive = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, progressCardGame.gameboard);
            CoordinatePair Mtwo_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, progressCardGame.gameboard);

            Village vil1 = Village.newInstance(pc2p2, one_seven);
            one_seven.putVillage(vil1);
            vil1.setVillageKind(VillageKind.CITY);
            Village vil2 = Village.newInstance(pc2p2, Mthree_Mone);
            vil2.setVillageKind(VillageKind.SCIENCE_METROPOLIS);
            progressCardGame.session.scienceMetropolisOwner = pc2p2;
            Mthree_Mone.putVillage(vil2);
            Village vil3 = Village.newInstance(pc2p2, Mthree_Mfive);
            Mthree_Mfive.putVillage(vil3);

            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(one_five, one_seven, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(Mthree_one, Mthree_Mone, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg3 = EdgeUnit.newEdgeUnit(Mthree_one, Mtwo_two, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg4 = EdgeUnit.newEdgeUnit(Mtwo_two, Mtwo_four, EdgeUnitKind.SHIP, pc2p2);
            EdgeUnit edg5 = EdgeUnit.newEdgeUnit(Mtwo_four, Mone_five, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg6 = EdgeUnit.newEdgeUnit(Mone_five, zero_four, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg7 = EdgeUnit.newEdgeUnit(one_five, zero_four, EdgeUnitKind.ROAD, pc2p2);
            EdgeUnit edg8 = EdgeUnit.newEdgeUnit(Mtwo_Mfour, Mthree_Mfive, EdgeUnitKind.ROAD, pc2p2);

            pc2p2.addVillage(vil1);
            pc2p2.addVillage(vil2);
            pc2p2.addVillage(vil3);
            pc2p2.addEdgeUnit(edg1);
            pc2p2.addEdgeUnit(edg2);
            pc2p2.addEdgeUnit(edg3);
            pc2p2.addEdgeUnit(edg4);
            pc2p2.addEdgeUnit(edg5);
            pc2p2.addEdgeUnit(edg6);
            pc2p2.addEdgeUnit(edg7);
            pc2p2.addEdgeUnit(edg8);
            progressCardGame2.gameboard.addRoadOrShip(edg1);
            progressCardGame2.gameboard.addRoadOrShip(edg2);
            progressCardGame2.gameboard.addRoadOrShip(edg3);
            progressCardGame2.gameboard.addRoadOrShip(edg4);
            progressCardGame2.gameboard.addRoadOrShip(edg5);
            progressCardGame2.gameboard.addRoadOrShip(edg6);
            progressCardGame2.gameboard.addRoadOrShip(edg7);
            progressCardGame2.gameboard.addRoadOrShip(edg8);
            progressCardGame2.gameboard.addVillage(vil1);
            progressCardGame2.gameboard.addVillage(vil2);
            progressCardGame2.gameboard.addVillage(vil3);

            // set longest road owner
            progressCardGame2.session.longestRoadOwner = pc2p2;

            // add knights
            {
                int id = progressCardGame2.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(pc2p2, Mtwo_two, id);
                pc2p2.addKnight(knight);
                progressCardGame2.gameboard.addKnight(knight, knight.getId());
            }
        }

        // give resources
        ResourceMap pc2p2Map = new ResourceMap();
        pc2p2Map.put(ResourceKind.BRICK, 1);
        pc2p2Map.put(ResourceKind.WOOD, 2);
        pc2p2Map.put(ResourceKind.GRAIN, 1);
        pc2p2Map.put(ResourceKind.PAPER, 1);
        pc2p2.setResources(pc2p2Map);


        /**
         * player 3 ---------- politics
         * - master merchant
         * - merchant
         * - merchant fleet
         * - trade monopoly
         *
         * */
        pc2p3.addProgressCard(ProgressCardType.MASTERMERCHANT);
        pc2p3.addProgressCard(ProgressCardType.MERCHANT);
        pc2p3.addProgressCard(ProgressCardType.MERCHANTFLEET);
        pc2p3.addProgressCard(ProgressCardType.TRADEMONOPOLY);

        // set flipchart levels: science : 0 - politics : 0 - trade : 5
        pc2p3.getCityImprovements().setScienceLevel(0);
        pc2p3.getCityImprovements().setPoliticsLevel(0);
        pc2p3.getCityImprovements().setTradeLevel(5);

        // place villages and edges on appropriate intersections
        {
            CoordinatePair zero_Mten = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, progressCardGame2.gameboard);
            CoordinatePair two_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(2, -4, progressCardGame2.gameboard);
            CoordinatePair zero_Meight = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, progressCardGame2.gameboard);
            CoordinatePair two_Mtwo = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, progressCardGame2.gameboard);
            CoordinatePair one_Mone = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, progressCardGame2.gameboard);

            Village vil1 = Village.newInstance(pc2p3, zero_Mten);
            zero_Mten.putVillage(vil1);
            vil1.setVillageKind(VillageKind.CITY);
            Village vil2 = Village.newInstance(pc2p3, two_Mfour);
            vil2.setVillageKind(VillageKind.TRADE_METROPOLIS);
            progressCardGame2.session.tradeMetropolisOwner = pc2p3;
            two_Mfour.putVillage(vil2);
            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(zero_Meight, zero_Mten, EdgeUnitKind.ROAD, pc2p3);
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(two_Mtwo, two_Mfour, EdgeUnitKind.ROAD, pc2p3);
            EdgeUnit edg3 = EdgeUnit.newEdgeUnit(one_Mone, two_Mtwo, EdgeUnitKind.ROAD, pc2p3);

            pc2p3.addVillage(vil1);
            pc2p3.addVillage(vil2);
            pc2p3.addEdgeUnit(edg1);
            pc2p3.addEdgeUnit(edg2);
            pc2p3.addEdgeUnit(edg3);
            progressCardGame2.gameboard.addRoadOrShip(edg1);
            progressCardGame2.gameboard.addRoadOrShip(edg2);
            progressCardGame2.gameboard.addRoadOrShip(edg3);
            progressCardGame2.gameboard.addVillage(vil1);
            progressCardGame2.gameboard.addVillage(vil2);
        }

        // give resources
        ResourceMap pc2p3Map = new ResourceMap();
        pc2p3Map.put(ResourceKind.PAPER, 2);
        pc2p3.setResources(pc2p3Map);

        /** remove cards from gameboard stack */
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.CRANE, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ALCHEMIST));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.MINING, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.COMMERCIALHARBOUR));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.SMITH, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.SPY));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.BISHOP, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.SABOTEUR));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.DESERTER, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ROADBUILDING));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.DIPLOMAT, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.MEDICINE));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.INTRIGUE, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.ENGINEER));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.MASTERMERCHANT, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.RESOURCEMONOPOLY));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.MERCHANT, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.WARLORD));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.MERCHANTFLEET, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.INVENTOR));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.TRADEMONOPOLY, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.IRRIGATION));
        progressCardGame2.gameboard.removeProgressCard(ProgressCardType.MERCHANT, GameRules.getGameRulesInstance().getProgressCardKind(ProgressCardType.WEDDING));

        // savedGames.add(progressCardGame2);


        // Metropolis saved game ---------------------------------------------------
        //TO DO:
        /*
            player 1 : owns Aqueduct
            player 2 : owns MarketPlace
            player 3 : owns Fortress

            they must have cities that have not been pillaged.
            SIDE NOTE: do we add 4 VP for metropolis??
         */
        Game metropolisGame = new Game();
        metropolisGame.name = "Metropolis game";
        for (Account account: accounts) {
            metropolisGame.addPlayer(account, -1);
            metropolisGame.markAsReady(account.getUsername());
        }
        metropolisGame.session = Session.newInstance(metropolisGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        metropolisGame.gameboard = GameBoard.newInstance(BoardVariants.BOARD_VARIANT1);
        metropolisGame.session.currentPhase = GamePhase.TURN_FIRST_PHASE;

        // barbarians have attacked at least once
        metropolisGame.session.firstBarbarianAttack = true;
        
        Player[] metropolisplayers = metropolisGame.session.getPlayers();
        Player metp1 = metropolisplayers[0];
        Player metp2 = metropolisplayers[1];
        Player metp3 = metropolisplayers[2];
        
        
        // set flipchart levels: science : 0 - politics : 3 - trade : 0
        metp1.getCityImprovements().setScienceLevel(0);
        metp1.getCityImprovements().setPoliticsLevel(3);
        metp1.getCityImprovements().setTradeLevel(0);
        
        {
            CoordinatePair Mthree_Mfive = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, metropolisGame.gameboard);
            CoordinatePair Mtwo_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, metropolisGame.gameboard);
            CoordinatePair Mone_Mfive = GameBoardManager.getCoordinatePairFromCoordinates(-1, -5, metropolisGame.gameboard);
            CoordinatePair Mtwo_Mtwo = GameBoardManager.getCoordinatePairFromCoordinates(-2, -2, metropolisGame.gameboard);
            
            Village vil1 = Village.newInstance(metp1, Mthree_Mfive);
            Mthree_Mfive.putVillage(vil1);
            metropolisGame.gameboard.addVillage(vil1);
            metp1.addVillage(vil1);
            
            Village vil2 = Village.newInstance(metp1, Mone_Mfive);
            Mone_Mfive.putVillage(vil2);
            vil2.setVillageKind(VillageKind.CITY);
            metropolisGame.gameboard.addVillage(vil2);
            metp1.addVillage(vil2);
            
            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(Mtwo_Mfour, Mthree_Mfive, EdgeUnitKind.ROAD, metp1);
            metropolisGame.gameboard.addRoadOrShip(edg1);
            metp1.addEdgeUnit(edg1);
            
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(Mtwo_Mfour, Mone_Mfive, EdgeUnitKind.ROAD, metp1);
            metropolisGame.gameboard.addRoadOrShip(edg2);
            metp1.addEdgeUnit(edg2);
            
            EdgeUnit edg3 = EdgeUnit.newEdgeUnit(Mtwo_Mfour, Mtwo_Mtwo, EdgeUnitKind.ROAD, metp1);
            metropolisGame.gameboard.addRoadOrShip(edg3);
            metp1.addEdgeUnit(edg3);
            
            {
                int id = metropolisGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(metp1, Mtwo_Mtwo, id);
                knight.promote();
                knight.promote();
                knight.activate();
                Mtwo_Mtwo.putKnight(knight);
                metp1.addKnight(knight);
                metropolisGame.gameboard.addKnight(knight, knight.getId());
            }
            
            metp1.incrementDefenderOfCatanPoints();
        }
        
        ResourceMap metp1Map = new ResourceMap();
        metp1Map.put(ResourceKind.PAPER, 6);
        metp1Map.put(ResourceKind.CLOTH, 5);
        metp1Map.put(ResourceKind.COIN, 9);
        metp1.setResources(metp1Map);
        
        
        // set flipchart levels: science : 3 - politics : 0 - trade : 0
        metp1.getCityImprovements().setScienceLevel(3);
        metp1.getCityImprovements().setPoliticsLevel(0);
        metp1.getCityImprovements().setTradeLevel(0);
        
        {
            CoordinatePair two_Mfour = GameBoardManager.getCoordinatePairFromCoordinates(2, -4, metropolisGame.gameboard);
            CoordinatePair two_Mtwo = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, metropolisGame.gameboard);
            CoordinatePair zero_Mtwo = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, metropolisGame.gameboard);
            CoordinatePair one_Mone = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, metropolisGame.gameboard);
            
            Village vil1 = Village.newInstance(metp2, two_Mfour);
            two_Mfour.putVillage(vil1);
            metropolisGame.gameboard.addVillage(vil1);
            metp2.addVillage(vil1);
            
            Village vil2 = Village.newInstance(metp2, zero_Mtwo);
            vil2.setVillageKind(VillageKind.CITY);
            two_Mfour.putVillage(vil2);
            metropolisGame.gameboard.addVillage(vil2);
            metp2.addVillage(vil2);
            
            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(two_Mfour, two_Mtwo, EdgeUnitKind.ROAD, metp2);
            metropolisGame.gameboard.addRoadOrShip(edg1);
            metp2.addEdgeUnit(edg1);
            
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(zero_Mtwo, one_Mone, EdgeUnitKind.ROAD, metp2);
            metropolisGame.gameboard.addRoadOrShip(edg2);
            metp2.addEdgeUnit(edg2);
        }
        
        ResourceMap metp2Map = new ResourceMap();
        metp2Map.put(ResourceKind.PAPER, 10);
        metp2Map.put(ResourceKind.CLOTH, 5);
        metp2Map.put(ResourceKind.COIN, 6);
        metp2.setResources(metp2Map);
        
        // set flipchart levels: science : 0 - politics : 0 - trade : 1
        metp1.getCityImprovements().setScienceLevel(0);
        metp1.getCityImprovements().setPoliticsLevel(0);
        metp1.getCityImprovements().setTradeLevel(1);
        {
            CoordinatePair Mtwo_two = GameBoardManager.getCoordinatePairFromCoordinates(-2, 2, metropolisGame.gameboard);
            CoordinatePair Mtwo_four = GameBoardManager.getCoordinatePairFromCoordinates(-2, 4, metropolisGame.gameboard);
            CoordinatePair zero_two = GameBoardManager.getCoordinatePairFromCoordinates(0, 2, metropolisGame.gameboard);
            CoordinatePair one_one = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, metropolisGame.gameboard);
            
            Village vil1 = Village.newInstance(metp3, Mtwo_two);
            Mtwo_two.putVillage(vil1);
            metropolisGame.gameboard.addVillage(vil1);
            metp3.addVillage(vil1);
            
            Village vil2 = Village.newInstance(metp3, one_one);
            vil2.setVillageKind(VillageKind.CITY);
            one_one.putVillage(vil2);
            metropolisGame.gameboard.addVillage(vil2);
            metp3.addVillage(vil2);
            
            EdgeUnit edg1 = EdgeUnit.newEdgeUnit(Mtwo_four, Mtwo_two, EdgeUnitKind.SHIP, metp3);
            metropolisGame.gameboard.addRoadOrShip(edg1);
            metp3.addEdgeUnit(edg1);
            
            EdgeUnit edg2 = EdgeUnit.newEdgeUnit(zero_two, one_one, EdgeUnitKind.ROAD, metp3);
            metropolisGame.gameboard.addRoadOrShip(edg2);
            metp3.addEdgeUnit(edg2);
        }
        ResourceMap metp3Map = new ResourceMap();
        metp3Map.put(ResourceKind.PAPER, 5);
        metp3Map.put(ResourceKind.CLOTH, 9);
        metp3Map.put(ResourceKind.COIN, 6);
        metp3.setResources(metp3Map);

        savedGames.add(metropolisGame);

        //Knight saved game --------------------------------------------------------
        /*
            Check list:
            each player has at least 2 knights with a represented knight level
            each player should have several roads that lead to intersections that are free of settlements or cities
            each player should have 3 brick and 3 lumber


         */
        Game knightGame = new Game();
        knightGame.name = "Knight";

        for (Account account : accounts) {
            knightGame.addPlayer(account, -1);
            knightGame.markAsReady(account.getUsername());
        }

        knightGame.session = Session.newInstance(knightGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        knightGame.session.currentPhase = GamePhase.TURN_FIRST_PHASE;
        knightGame.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);


        //
        Player[] players = knightGame.session.getPlayers();
        {
            Player p1 = players[0];
            {
                ResourceMap knightResourceMapP1 = new ResourceMap();

                knightResourceMapP1.put(ResourceKind.WOOD, 5);
                knightResourceMapP1.put(ResourceKind.CLOTH, 4);
                knightResourceMapP1.put(ResourceKind.WOOL, 5);
                knightResourceMapP1.put(ResourceKind.COIN, 3);
                knightResourceMapP1.put(ResourceKind.ORE, 4);
                knightResourceMapP1.put(ResourceKind.BRICK, 4);
                knightResourceMapP1.put(ResourceKind.GRAIN, 5);
                knightResourceMapP1.put(ResourceKind.PAPER, 2);
                p1.setResources(knightResourceMapP1);

                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p1, position, id);
                knight.promote();
                p1.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p1, position, id);
                knight.promote();
                knight.promote();
                p1.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, knightGame.gameboard);
                Village village = Village.newInstance(p1, position);
                village.setVillageKind(VillageKind.CITY);
                p1.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }
            {
                //ADD SETTLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, knightGame.gameboard);
                Village village = Village.newInstance(p1, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p1.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }
            {
                //ADD ROAD
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }

            Player p2 = players[1];
            {
                ResourceMap knightResourceMapP2 = new ResourceMap();

                knightResourceMapP2.put(ResourceKind.WOOD, 6);
                knightResourceMapP2.put(ResourceKind.CLOTH, 4);
                knightResourceMapP2.put(ResourceKind.WOOL, 4);
                knightResourceMapP2.put(ResourceKind.COIN, 3);
                knightResourceMapP2.put(ResourceKind.ORE, 4);
                knightResourceMapP2.put(ResourceKind.BRICK, 3);
                knightResourceMapP2.put(ResourceKind.GRAIN, 4);
                knightResourceMapP2.put(ResourceKind.PAPER, 2);
                p1.setResources(knightResourceMapP2);


                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p2, position, id);
                knight.promote();
                p2.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p2, position, id);
                p2.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, knightGame.gameboard);
                Village village = Village.newInstance(p2, position);
                village.setVillageKind(VillageKind.CITY);
                p2.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }
            {
                //ADD SETELLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, knightGame.gameboard);
                Village village = Village.newInstance(p2, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p2.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }

            {
                //ADD ROAD!
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }

            Player p3 = players[2];
            {
                ResourceMap knightResourceMapP3 = new ResourceMap();

                knightResourceMapP3.put(ResourceKind.WOOD, 4);
                knightResourceMapP3.put(ResourceKind.CLOTH, 5);
                knightResourceMapP3.put(ResourceKind.WOOL, 4);
                knightResourceMapP3.put(ResourceKind.COIN, 4);
                knightResourceMapP3.put(ResourceKind.ORE, 3);
                knightResourceMapP3.put(ResourceKind.BRICK, 4);
                knightResourceMapP3.put(ResourceKind.GRAIN, 5);
                knightResourceMapP3.put(ResourceKind.PAPER, 3);
                p1.setResources(knightResourceMapP3);


                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-0, -2, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
                knight.promote();
                knight.promote();
                p3.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
                knight.promote();
                p3.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, knightGame.gameboard);
                Village village = Village.newInstance(p3, position);
                village.setVillageKind(VillageKind.CITY);
                p3.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }
            {
                //ADD SETETLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, knightGame.gameboard);
                Village village = Village.newInstance(p3, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p3.addVillage(village);
                knightGame.gameboard.addVillage(village);
            }

            {
                //ADD ROAD
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, knightGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, knightGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                knightGame.gameboard.addRoadOrShip(road);
            }

        }

        savedGames.add(knightGame);

        //barbarian saved game -----------------------------------------------------
        // 3 player where barbarian is soon going to attack island


        Game barbarianAttack = new Game();
        barbarianAttack.name = "Barbarian Attack Island";

        ResourceMap barbarianResourceMap = new ResourceMap();
        for (Account account : accounts) {
            barbarianAttack.addPlayer(account, -1);
            barbarianAttack.markAsReady(account.getUsername());
        }


        barbarianAttack.session = Session.newInstance(barbarianAttack.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        barbarianAttack.session.currentPhase = GamePhase.TURN_FIRST_PHASE;
        barbarianAttack.session.barbarianPosition = 1;
        barbarianAttack.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);


        Player[] barbarianAttackPlayers = barbarianAttack.session.getPlayers();
        {
            Player p1 = barbarianAttackPlayers[0];
            {
                {
                    ResourceMap barbResourceMapP1 = new ResourceMap();

                    barbResourceMapP1.put(ResourceKind.WOOD, 5);
                    barbResourceMapP1.put(ResourceKind.CLOTH, 4);
                    barbResourceMapP1.put(ResourceKind.WOOL, 5);
                    barbResourceMapP1.put(ResourceKind.COIN, 3);
                    barbResourceMapP1.put(ResourceKind.ORE, 4);
                    barbResourceMapP1.put(ResourceKind.BRICK, 4);
                    barbResourceMapP1.put(ResourceKind.GRAIN, 5);
                    barbResourceMapP1.put(ResourceKind.PAPER, 2);
                    p1.setResources(barbResourceMapP1);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p1, position);
                    village.setVillageKind(VillageKind.CITY);
                    p1.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    //ADD SETTLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p1, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p1.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);
                }
                {
                    //ADD ROAD
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                    p1.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                Player p2 = barbarianAttackPlayers[1];
                {
                    ResourceMap barbResourceMapP2 = new ResourceMap();

                    barbResourceMapP2.put(ResourceKind.WOOD, 5);
                    barbResourceMapP2.put(ResourceKind.CLOTH, 4);
                    barbResourceMapP2.put(ResourceKind.WOOL, 3);
                    barbResourceMapP2.put(ResourceKind.COIN, 3);
                    barbResourceMapP2.put(ResourceKind.ORE, 4);
                    barbResourceMapP2.put(ResourceKind.BRICK, 4);
                    barbResourceMapP2.put(ResourceKind.GRAIN, 6);
                    barbResourceMapP2.put(ResourceKind.PAPER, 4);
                    p1.setResources(barbResourceMapP2);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p2, position);
                    village.setVillageKind(VillageKind.CITY);
                    p2.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);

                }
                {
                    //ADD SETELLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p2, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p2.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    //ADD ROAD!
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                    p2.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                Player p3 = barbarianAttackPlayers[2];
                {
                    ResourceMap barbResourceMapP3 = new ResourceMap();

                    barbResourceMapP3.put(ResourceKind.WOOD, 6);
                    barbResourceMapP3.put(ResourceKind.CLOTH, 4);
                    barbResourceMapP3.put(ResourceKind.WOOL, 4);
                    barbResourceMapP3.put(ResourceKind.COIN, 4);
                    barbResourceMapP3.put(ResourceKind.ORE, 4);
                    barbResourceMapP3.put(ResourceKind.BRICK, 4);
                    barbResourceMapP3.put(ResourceKind.GRAIN, 5);
                    barbResourceMapP3.put(ResourceKind.PAPER, 4);
                    p1.setResources(barbResourceMapP3);

                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-0, -2, barbarianAttack.gameboard);
                    int id = barbarianAttack.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(p3, position, id);
                    knight.promote();
                    p3.addKnight(knight);
                    barbarianAttack.gameboard.addKnight(knight, knight.getId());
                }
                {
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, barbarianAttack.gameboard);
                    int id = barbarianAttack.gameboard.nextKnightId();
                    Knight knight = Knight.newInstance(p3, position, id);
                    p3.addKnight(knight);
                    barbarianAttack.gameboard.addKnight(knight, knight.getId());
                }
                {
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p3, position);
                    village.setVillageKind(VillageKind.CITY);
                    p3.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);
                }
                {
                    //ADD SETETLEMENT
                    CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, barbarianAttack.gameboard);
                    Village village = Village.newInstance(p3, position);
                    village.setVillageKind(VillageKind.SETTLEMENT);
                    p3.addVillage(village);
                    barbarianAttack.gameboard.addVillage(village);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    //ADD ROAD
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }
                {
                    CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, barbarianAttack.gameboard);
                    CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, barbarianAttack.gameboard);
                    EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                    p3.addEdgeUnit(road);
                    barbarianAttack.gameboard.addRoadOrShip(road);
                }

            }
        }

        //savedGames.add(barbarianAttack);

        //**** prepare catan strength ****
        //give player1 2 knights, player2 3 knights, player3 4 knights (change up the levels i.e basic, strong, mighty)
        //add knights to board
        //make sure these knights are active

        // *** prepare barbarian strength ****
        //add cities (and metropolis) to players
        // could be fancy and add city wall.

        // winning saved game ----------------------------------------------------


        Game winningGame = new Game();
        winningGame.name = "Winning Game";

        for (Account account : accounts) {
            winningGame.addPlayer(account, -1);
            winningGame.markAsReady(account.getUsername());
        }
        ResourceMap winningGameMap = new ResourceMap();
        for (ResourceKind resourceKind : ResourceKind.values()) {
            winningGameMap.add(resourceKind, 6);
        }
        winningGame.session = Session.newInstance(winningGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        winningGame.session.currentPhase = GamePhase.TURN_FIRST_PHASE;
        winningGame.gameboard = GameBoard.newInstance(BoardVariants.DEFAULT);


        winningGame.session = Session.newInstance(winningGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        //SET SESSION

        Player[] winningGamePlayers = winningGame.session.getPlayers();
        {
            Player p1 = winningGamePlayers[0];
            {

                ResourceMap winResourceMapP1 = new ResourceMap();

                winResourceMapP1.put(ResourceKind.WOOD, 5);
                winResourceMapP1.put(ResourceKind.CLOTH, 4);
                winResourceMapP1.put(ResourceKind.WOOL, 5);
                winResourceMapP1.put(ResourceKind.COIN, 3);
                winResourceMapP1.put(ResourceKind.ORE, 4);
                winResourceMapP1.put(ResourceKind.BRICK, 4);
                winResourceMapP1.put(ResourceKind.GRAIN, 5);
                winResourceMapP1.put(ResourceKind.PAPER, 2);
                p1.setResources(winResourceMapP1);

                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, winningGame.gameboard);
                Village village = Village.newInstance(p1, position);
                village.setVillageKind(VillageKind.CITY);
                p1.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                //ADD SETTLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, winningGame.gameboard);
                Village village = Village.newInstance(p1, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p1.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            {
                //ADD ROAD
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(0, -10, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -8, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -7, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p1);
                p1.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }

            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, winningGame.gameboard);
                Village village = Village.newInstance(p1, position);
                village.setVillageKind(VillageKind.CITY);
                p1.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            Player p2 = winningGamePlayers[1];
            {
                p2.setTokenVictoryPoints(9);
                CatanGame.client.sendTCP(UpdateVP.newInstance(p2.getUsername()));

                ResourceMap winResourceMapP2 = new ResourceMap();

                winResourceMapP2.put(ResourceKind.WOOD, 4);
                winResourceMapP2.put(ResourceKind.CLOTH, 4);
                winResourceMapP2.put(ResourceKind.WOOL, 4);
                winResourceMapP2.put(ResourceKind.COIN, 4);
                winResourceMapP2.put(ResourceKind.ORE, 5);
                winResourceMapP2.put(ResourceKind.BRICK, 5);
                winResourceMapP2.put(ResourceKind.GRAIN, 6);
                winResourceMapP2.put(ResourceKind.PAPER, 4);
                p2.setResources(winResourceMapP2);

                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, winningGame.gameboard);
                Village village = Village.newInstance(p2, position);
                village.setVillageKind(VillageKind.CITY);
                p2.addVillage(village);
                winningGame.gameboard.addVillage(village);

            }
            {
                //ADD SETELLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, winningGame.gameboard);
                Village village = Village.newInstance(p2, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p2.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -8, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                //ADD ROAD!
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-4, -4, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(-3, -5, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-2, -4, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p2);
                p2.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }

            Player p3 = winningGamePlayers[2];
            {
                winningGame.gameboard.setaBootOwner(p3);

                ResourceMap winResourceMapP3 = new ResourceMap();

                winResourceMapP3.put(ResourceKind.WOOD, 6);
                winResourceMapP3.put(ResourceKind.CLOTH, 4);
                winResourceMapP3.put(ResourceKind.WOOL, 4);
                winResourceMapP3.put(ResourceKind.COIN, 4);
                winResourceMapP3.put(ResourceKind.ORE, 4);
                winResourceMapP3.put(ResourceKind.BRICK, 4);
                winResourceMapP3.put(ResourceKind.GRAIN, 5);
                winResourceMapP3.put(ResourceKind.PAPER, 4);
                p3.setResources(winResourceMapP3);

                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-0, -2, winningGame.gameboard);
                int id = winningGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
                knight.promote();
                p3.addKnight(knight);
                winningGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, winningGame.gameboard);
                int id = winningGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
                p3.addKnight(knight);
                winningGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, winningGame.gameboard);
                Village village = Village.newInstance(p3, position);
                village.setVillageKind(VillageKind.CITY);
                p3.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            {
                //ADD SETETLEMENT
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, winningGame.gameboard);
                Village village = Village.newInstance(p3, position);
                village.setVillageKind(VillageKind.SETTLEMENT);
                p3.addVillage(village);
                winningGame.gameboard.addVillage(village);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                //ADD ROAD
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(0, -2, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(-1, -1, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
            {
                CoordinatePair pos1 = GameBoardManager.getCoordinatePairFromCoordinates(1, -1, winningGame.gameboard);
                CoordinatePair pos2 = GameBoardManager.getCoordinatePairFromCoordinates(1, 1, winningGame.gameboard);
                EdgeUnit road = EdgeUnit.newEdgeUnit(pos1, pos2, EdgeUnitKind.ROAD, p3);
                p3.addEdgeUnit(road);
                winningGame.gameboard.addRoadOrShip(road);
            }
        }
        savedGames.add(winningGame);

       /** testing different boards */
       Game testBoard = new Game();
       testBoard.name = "Winning Game";

       for(Account account : accounts) {
           testBoard.addPlayer(account, -1);
           testBoard.markAsReady(account.getUsername());
       }
       testBoard.session = Session.newInstance(testBoard.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
       testBoard.session.currentPhase = GamePhase.TURN_FIRST_PHASE;
       testBoard.gameboard = GameBoard.newInstance(BoardVariants.BOARD_VARIANT1);

       savedGames.add(testBoard);

        return savedGames;
    }

    void stop() {
        server.stop();
    }
}
