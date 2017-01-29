package com.mygdx.catan.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.ui.CatanWindow;

public class MenuScreen implements Screen {
	
	private final CatanGame aGame;
	private Stage aMenuStage;
	private Texture bg;
	private TextButton aJoinRandomButton;
	private TextButton aCreateGameButton;
	private TextButton aBrowseGamesButton;
	private TextButton aResumeGameButton;
	private Table aMenuTable;
	
	public MenuScreen(CatanGame pGame) {
		aGame = pGame;
	}
	
	@Override
	public void show() {
		
		bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		aMenuStage = new Stage();
        Gdx.input.setInputProcessor(aMenuStage);
        
        // Setup table for buttons
        aMenuTable = new Table();
        aMenuTable.setFillParent(true);
		aMenuStage.addActor(aMenuTable);
		aMenuTable.left().bottom();
		
		// Setup buttons
    	aJoinRandomButton = new TextButton("Join Random Game", aGame.skin);
    	setupRandomButton();
    	
    	aCreateGameButton = new TextButton("Create Game", aGame.skin);
    	setupButton(aCreateGameButton, ScreenKind.CREATE_GAME);
    	
    	aBrowseGamesButton = new TextButton("Browse Games", aGame.skin);
    	setupButton(aBrowseGamesButton, ScreenKind.BROWSE_GAMES);
    	
    	aResumeGameButton = new TextButton("Resume Game", aGame.skin);
    	setupButton(aResumeGameButton, ScreenKind.LOBBY);
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        aMenuStage.act(delta);
        aMenuStage.draw();
		
	}

	@Override
	public void resize(int width, int height) {
        aMenuStage.getViewport().update(width, height, false);
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
        aMenuStage.dispose();
    }
    
    public void setupButton(TextButton pTextButton, ScreenKind pScreenkind) {
    	// add listener to button
    	pTextButton.addListener(new ClickListener() {
        	@Override
			public void clicked(InputEvent event, float x, float y) {
        		pTextButton.setChecked(false);
        		aGame.switchScreen(pScreenkind);
        	}
				
			});
        
        aMenuTable.add(pTextButton).pad(50);
    }
    
    public void setupRandomButton() {
    	// add listener to button
    	aJoinRandomButton.addListener(new ClickListener() {
        	@Override
			public void clicked(InputEvent event, float x, float y) {
        		aJoinRandomButton.setChecked(false);
        		//TODO: request catanGame to join random game
        		
        		// setup window
        		CatanWindow window = new CatanWindow("Joining Game", aGame.skin);
        		window.setWidth(1f / 4f * Gdx.graphics.getWidth());
                window.setHeight(1f / 4f * Gdx.graphics.getHeight());
                window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2, Gdx.graphics.getHeight() / 2 - window.getHeight() / 2);
                window.setWindowListener(() -> window.remove());
      
                aMenuStage.addActor(window);
        	}
				
			});
        
        aMenuTable.add(aJoinRandomButton).pad(50);
    }
}
