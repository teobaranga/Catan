package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.catan.CatanGame;

public class SessionScreen implements Screen {

	private final CatanGame aGame;
	private Stage aSessionStage;
	private Texture bg;
	private final int SIZE = 7;
	private int[] aHexPositions;
	private int[] aIntersectionPositions;
	
	PolygonSprite poly;
	PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
	
	Texture aWaterTextureSolid;
	Texture aDesertTextureSolid;
	Texture aClayTextureSolid;
	Texture aForrestTextureSolid;
	Texture aStoneTextureSolid;
	
	public SessionScreen(CatanGame pGame) {
		aGame = pGame;
	}
	
	@Override
	public void show() {
		bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aSessionStage = new Stage();
        Gdx.input.setInputProcessor(aSessionStage);
        
		// initialize hex position coordinates, where x=((aHexPositions[i]/SIZE) - (SIZE/2)) and y=((aHexPositions[i]%SIZE) - (SIZE/2))
        // the coordinates describe the offset from the center.
        // Note that the x coordinate offset works diagonally, and therefore depends on the y offset
		aHexPositions = new int[37];
		int index = 0;
        int half = SIZE / 2;

        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = col - (cols - half) + 1;
                int y = (row - half);
                aHexPositions[index++] = SIZE*(x + half) + (y + half);
                System.out.print((SIZE*(x + half) + (y + half))+"("+x+","+y+") ");
            }
            System.out.println();
        }
        
        //TODO: initialize point position coordinates
        //TODO: UI panels
        
        
     // Creating the color filling for hexagons
        aWaterTextureSolid = setupTextureSolid(Color.BLUE);
        aDesertTextureSolid = setupTextureSolid(Color.YELLOW);
        aClayTextureSolid = setupTextureSolid(Color.RED);
    	aForrestTextureSolid = setupTextureSolid(Color.GREEN);
    	aStoneTextureSolid = setupTextureSolid(Color.GRAY);
	}
	
	private Texture setupTextureSolid(Color color) {
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color); // DE is red, AD is green and BE is blue.
        pix.fill();
		return new Texture(pix);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        aSessionStage.act(delta);
        aSessionStage.draw();
        
      //TODO: draw hexagons
        drawHexagon(Gdx.graphics.getWidth() / 3, 5*Gdx.graphics.getHeight() / 6, 70, aWaterTextureSolid);
        drawHexagon(Gdx.graphics.getWidth() / 3 + 125, 5*Gdx.graphics.getHeight() / 6, 70, aDesertTextureSolid);
        drawHexagon(Gdx.graphics.getWidth() / 3 + 250, 5*Gdx.graphics.getHeight() / 6, 70, aForrestTextureSolid);
        drawHexagon(Gdx.graphics.getWidth() / 3 + 60, 5*Gdx.graphics.getHeight() / 6 - 110, 70, aClayTextureSolid);
	}

	@Override
	public void resize(int width, int height) {
        aSessionStage.getViewport().update(width, height, false);
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
        aSessionStage.dispose();
	}
	
	/**
	 * Draws a hexagon according to given position and length
	 * 
	 * @param xPos x position of hexagon center
	 * @param yPos y position of hexagon center
	 * @param length length of the side of the hexagon
	 * */
	private void drawHexagon(int xPos, int yPos, int length, Texture pTexture) {
		
		int b = (int) Math.sqrt(Math.pow(length, 2) - Math.pow(length/2, 2));
	        
		PolygonRegion polyReg = new PolygonRegion(new TextureRegion(pTexture),
				new float[] {      // Six vertices
						xPos - b, yPos - length/2,        	// Vertex 0                4
						xPos, yPos - length,       		    // Vertex 1           5         3
						xPos + b, yPos - length/2,		    // Vertex 2         
						xPos + b, yPos + length/2,    	    // Vertex 3           0         2 
						xPos, yPos + length,        		// Vertex 4                1
						xPos - b, yPos + length/2
		}, new short[] {
				0, 1, 4,         // Sets up triangulation according to vertices above
				0, 4, 5,        
				1, 2, 3,
				1, 3, 4
		});
		poly = new PolygonSprite(polyReg);
		poly.setOrigin(100, 100);
		polyBatch = new PolygonSpriteBatch();
	  
		polyBatch.begin();
	    poly.draw(polyBatch);
	    polyBatch.end();
	}
}
