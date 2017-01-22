package com.mygdx.catan.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;

public class MenuScreen implements Screen {
	
	private final CatanGame aGame;
	private static final String TITLE = "Menu";
	private Stage aLobbyStage;
	private Texture bg;
	private TextButton aJoinRandomButton;
	private TextButtonStyle aButtonStyle;
	private Table aLobbyTable;
	
	private Skin skin = new Skin();
	
	public MenuScreen(CatanGame pGame) {
		aGame = pGame;
	}
	
	@Override
	public void show() {
		
		bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		aLobbyStage = new Stage();
        Gdx.input.setInputProcessor(aLobbyStage);
        
        aLobbyTable = new Table();
        aLobbyTable.setFillParent(true);
		aLobbyStage.addActor(aLobbyTable);
		aLobbyTable.left().bottom();
	
		// Generate a 1x1 white texture and store it in skin named white
		Pixmap pixmap = new Pixmap(1,1, Format.RGBA8888);
        pixmap.setColor(Color.GRAY);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));
		
		// store default font into skin under name default
		skin.add("default", new BitmapFont());
		
        aButtonStyle = new TextButtonStyle();
        aButtonStyle.font = skin.getFont("default");
        aButtonStyle.up = skin.getDrawable("white");
     
        
        aJoinRandomButton = new TextButton("Join Random", aButtonStyle);
        
        
        aJoinRandomButton.addListener(new ChangeListener() {
        	@Override
			public void changed(ChangeEvent event, Actor actor) {
 				aJoinRandomButton.setText("Good job!");
        	}
				
			});
        
        aLobbyTable.add(aJoinRandomButton).pad(50);
        }

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        aLobbyStage.act(delta);
        aLobbyStage.draw();
		
	}

	@Override
	public void resize(int width, int height) {
        aLobbyStage.getViewport().update(width, height, false);
        aGame.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        aLobbyStage.dispose();
    }

}
