package com.mygdx.catan.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.catan.CatanGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Catan";
		// TODO : Set the game to match the demo computer resolution & fullscreen to true
		config.width = 1280;
		config.height = 720;
//		config.fullscreen = true;
		new LwjglApplication(new CatanGame(), config);
	}
}
