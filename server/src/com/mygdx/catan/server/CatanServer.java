package com.mygdx.catan.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.catan.Config;
import com.mygdx.catan.request.ForwardedRequest;
import com.mygdx.catan.request.LoginRequest;
import com.mygdx.catan.request.MarkAsReady;
import com.mygdx.catan.response.LoginResponse;
import com.mygdx.catan.response.MarkedAsReady;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatanServer {

    /** Name of the file storing the game accounts */
    private static final String ACCOUNTS_DB = "accounts.bin";

    /** Map connecting any given peer (by name) to its other peers */
    private static Map<String, List<Connection>> peersMap;

    private static List<String> defaultAccounts;
    private static List<String> accounts;

    static {
        defaultAccounts = new ArrayList<>();
        defaultAccounts.add("aina");
        defaultAccounts.add("amanda");
        defaultAccounts.add("arnaud");
        defaultAccounts.add("emma");
        defaultAccounts.add("teo");
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();

        // Register request & response classes (needed for networking)
        // Must be registered in the same order in the client
        Kryo kryo = server.getKryo();
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(MarkAsReady.class);
        kryo.register(MarkedAsReady.class);
        kryo.register(ForwardedRequest.class);
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
                if (object instanceof LoginRequest) {
                    // Check if the account exists
                    boolean success = accounts.contains(((LoginRequest) object).username);
                    // Reply to the client with the success status
                    final LoginResponse response = new LoginResponse();
                    response.success = success;
                    connection.sendTCP(response);
                } else if (object instanceof MarkAsReady) {
                    // TODO: mark the player as ready
                    System.out.println(connection.getID() + " marked as ready");
                    connection.sendTCP(new MarkedAsReady());
                } else if (object instanceof ForwardedRequest) {
                    // Get the peers of the client that sent the request
                    List<Connection> peers = peersMap.get(connection.toString());
                    for (Connection peer : peers) {
                        // Forward the object (message) to the other peers
                        if (!peer.toString().equals(connection.toString())) {
                            peer.sendTCP(object);
                        }
                    }
                }
            }
        });
    }
}
