package com.mygdx.catan.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esotericsoftware.kryonet.Client;
import com.mygdx.catan.CatanGame;

import java.io.IOException;

public class DesktopLauncher {
    // The IP of the server. All clients need to connect to this IP.
    private static final String MY_IP = "142.157.168.72";

	public static void main (String[] arg) throws IOException {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Catan";
		// TODO : Set the game to match the demo computer resolution & fullscreen to true
		config.width = 1280;
		config.height = 720;
//		config.fullscreen = true;
		new LwjglApplication(new CatanGame(), config);

        Client client = new Client();
        client.start();
        client.connect(5000, MY_IP, 54555, 54777);
	}
}
