package com.mygdx.catan.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;

public class MenuScreen implements Screen {
	
	private final CatanGame aGame;
	private Stage aLobbyStage;
	private Texture bg;
	private TextButton aJoinRandomButton;
	private TextButton aCreateGameButton;
	private TextButton aBrowseGamesButton;
	private TextButton aResumeGameButton;
	private Table aLobbyTable;
	
	public MenuScreen(CatanGame pGame) {
		aGame = pGame;
	}
	
	@Override
	public void show() {
		
		bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		aLobbyStage = new Stage();
        Gdx.input.setInputProcessor(aLobbyStage);
        
        // Setup table for buttons
        aLobbyTable = new Table();
        aLobbyTable.setFillParent(true);
		aLobbyStage.addActor(aLobbyTable);
		aLobbyTable.left().bottom();
		
		// Setup buttons
    	aJoinRandomButton = new TextButton("Join Random Game", aGame.skin);
    	setupButton(aJoinRandomButton, ScreenKind.LOBBY);
    	aCreateGameButton = new TextButton("Create Game", aGame.skin);
    	setupButton(aCreateGameButton, ScreenKind.CREATE_GAME);
    	aBrowseGamesButton = new TextButton("Browse Games", aGame.skin);
    	setupButton(aBrowseGamesButton, ScreenKind.BROWSE_GAMES);
    	aResumeGameButton = new TextButton("Resume Game", aGame.skin);
    	setupButton(aResumeGameButton, ScreenKind.RESUME_GAME);
		
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
    
    public void setupButton(TextButton pTextButton, ScreenKind pScreenkind) {
    	// add listener to button
    	pTextButton.addListener(new ChangeListener() {
        	@Override
			public void changed(ChangeEvent event, Actor actor) {
        		aGame.switchScreen(pScreenkind);
        	}
				
			});
        
        aLobbyTable.add(pTextButton).pad(50);
    }
}
