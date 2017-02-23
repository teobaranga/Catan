package com.mygdx.catan.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class CatanServer {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
        server.bind(54555, 54777);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                // Print the client's ID
                System.out.println(connection.getID());
            }
        });
    }
}
