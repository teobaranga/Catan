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
import com.mygdx.catan.response.*;
import com.mygdx.catan.response.game.GameList;
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
        if (!saveGameDatabase.exists()) {
            Output output = new Output(new FileOutputStream(SAVED_GAMES_DB));
            kryo.writeObject(output, createTestSavedGames());
            output.close();
        }

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

        // Load the games TODO complete this
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
                    response = GameList.newInstance(savedGames);
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
        game.gameboard = GameBoard.newInstance();
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
        yearsOfPlenty.gameboard = GameBoard.newInstance();
        yearsOfPlenty.session.currentPhase = GamePhase.TURN_FIRST_PHASE;

        ResourceMap resourceMap = new ResourceMap();
        for (ResourceKind resourceKind : ResourceKind.values())
            resourceMap.add(resourceKind, 12);
        for (Player player : yearsOfPlenty.session.getPlayers())
            player.setResources(resourceMap);

        savedGames.add(yearsOfPlenty);

        // Progress Card game --------------------------------------------------------
        Game progressCardGame = new Game();
        progressCardGame.name = "Progress Card Game";
        for (Account account: accounts) {
            progressCardGame.addPlayer(account, -1);
            progressCardGame.markAsReady(account.getUsername());
        }
        progressCardGame.session = Session.newInstance(progressCardGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        for(Player player: progressCardGame.session.getPlayers()) {
            for (ProgressCardType card : GameRules.getGameRulesInstance().getProgressCardTypes()) {
                player.addProgressCard(card);
            }
        }

        savedGames.add(progressCardGame);

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
        metropolisGame.name = "Metropolis";
        metropolisGame.session = Session.newInstance(metropolisGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());

        for (Account account : accounts) {
            metropolisGame.addPlayer(account, -1);
            metropolisGame.markAsReady(account.getUsername());
        }
        ResourceMap commoditiesMap = new ResourceMap();

        for (ResourceKind resourceKind : ResourceKind.values()) {
            //will need 9 coin, 9 paper, 9 cloth (for 2 upgrades)
            commoditiesMap.add(resourceKind, 13);
        }

        for (Player player : metropolisGame.session.getPlayers()) {
            player.setResources(commoditiesMap);
        }
        // WHICH PHASE? metropolisGame.session.currentPhase = GamePhase.

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
        knightGame.gameboard = GameBoard.newInstance();

        ResourceMap knightResourceMap = new ResourceMap();

        for (ResourceKind resourceKind : ResourceKind.values()) {
            knightResourceMap.add(resourceKind, 12);
        }

        //
        Player[] players = knightGame.session.getPlayers();
        {
            Player p1 = players[0];
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(0, -8, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p1, position, id);
                p1.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(1, -5, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p1, position, id);
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
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-3, -7, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p2, position, id);
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
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(-0, -2, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
                p3.addKnight(knight);
                knightGame.gameboard.addKnight(knight, knight.getId());
            }
            {
                CoordinatePair position = GameBoardManager.getCoordinatePairFromCoordinates(2, -2, knightGame.gameboard);
                int id = knightGame.gameboard.nextKnightId();
                Knight knight = Knight.newInstance(p3, position, id);
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

        for (Player player : players) {
            player.setResources(knightResourceMap);
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

        for (ResourceKind resourceKind : ResourceKind.values()) {
            barbarianResourceMap.add(resourceKind, 20);
        }

        barbarianAttack.session = Session.newInstance(barbarianAttack.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        barbarianAttack.session.currentPhase = GamePhase.TURN_FIRST_PHASE;
        barbarianAttack.session.barbarianPosition = 1;
        barbarianAttack.gameboard = GameBoard.newInstance();

        for (Player player : barbarianAttack.session.getPlayers()) {
            player.setResources(barbarianResourceMap);
        }

        savedGames.add(barbarianAttack);

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
        winningGame.session = Session.newInstance(winningGame.peers.keySet(), GameRules.getGameRulesInstance().getVpToWin());
        //SET SESSION


        for(Account account : accounts) {
            winningGame.addPlayer(account, -1);
            winningGame.markAsReady(account.getUsername());
        }

        ResourceMap winningGameMap = new ResourceMap();
        for (ResourceKind resourceKind : ResourceKind.values()) {
            winningGameMap.add(resourceKind, 15);
        }

        for (Player player : winningGame.session.getPlayers()) {
            player.setResources(winningGameMap);
        }

        return savedGames;
    }

    void stop() {
        server.stop();
    }
}
