package com.mygdx.catan.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.catan.Config;
import com.mygdx.catan.request.MarkAsReady;
import com.mygdx.catan.response.MarkedAsReady;

import java.io.IOException;

public class CatanServer {

    public static void main(String[] args) throws IOException {
        Server server = new Server();

        // Register request & response classes (needed for networking)
        Kryo kryo = server.getKryo();
        kryo.register(MarkAsReady.class);
        kryo.register(MarkedAsReady.class);

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
                if (object instanceof MarkAsReady) {
                    // TODO: mark the player as ready
                    System.out.println(connection.getID() + " marked as ready");
                    final MarkedAsReady markedAsReady = new MarkedAsReady();
                    markedAsReady.hello = true;
                    connection.sendTCP(markedAsReady);
                }
            }
        });
    }
}
