package com.mygdx.catan.session;

import java.util.Random;

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
import com.mygdx.catan.enums.ResourceKind;

public class SessionScreen implements Screen {

	private final CatanGame aGame;
	private Stage aSessionStage;
	private Texture bg;
	
	// all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
	private final int SIZE = 7;												// number of tiles at longest diagonal
	private final int LENGTH = 40;											// length of an edge of a tile
	private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH/2, 2)); // length of base of equilateral triangles within a tile
	private final int OFFX = BASE;											// offset on the x axis
	private final int OFFY = LENGTH + LENGTH/2;								// offset on the y axis
	
	private Random rd = new Random();

	private Pair<Integer,Integer>[] aHexPositions;
	private int[] aIntersectionPositions;
	private int[] aHexKindSetup;
	
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

	public int getHexNumber(int size){
		int result = size;
		int level = 1;
		while(size-- > level){
			result += size*2;
			level++;
		}
		return result;
	}

	@Override
	public void show() {
		bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aSessionStage = new Stage();
        Gdx.input.setInputProcessor(aSessionStage);
        
		// initialize hex position coordinates, where x=(aHexPositions[i].getLeft()) and y=(aHexPositions[i].getRight())
        // the coordinates describe the offset from the center.
		aHexPositions = new Pair[getHexNumber(SIZE)]; //TODO: make this number depend on SIZE
		aHexKindSetup = new int[getHexNumber(SIZE)];
		int index = 0;
        int half = SIZE / 2;

        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = -cols + 2 * col + 1;
                int y = (row - half);
                aHexKindSetup[index] = rd.nextInt(ResourceKind.values().length);
                aHexPositions[index++] = new Pair(x,y);
            }
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
        
        // sets center of board
        int xCenter = 2*Gdx.graphics.getWidth() / 5 + Gdx.graphics.getWidth() / 13;
        int yCenter = 3*Gdx.graphics.getHeight() / 5;
        int offsetX, offsetY;
      
        // draws hexagons according to coordinates stored in aHexPositions and hex kinds stored in aHexKindSetup
        for(int i = 0; i < aHexPositions.length; i++) {
        	offsetX = (aHexPositions[i].getLeft());
        	offsetY = (aHexPositions[i].getRight());
        	drawHexagon(xCenter + (offsetX * OFFX), yCenter + (offsetY * OFFY), LENGTH, BASE, ResourceKind.values()[aHexKindSetup[i]]);
        }

        aSessionStage.act(delta);
        aSessionStage.draw();
        
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
	private void drawHexagon(int xPos, int yPos, int length, int base, ResourceKind pResourceKind) {
		
		Texture aTexture = aWaterTextureSolid;
		
		// sets aTexture to relevant texture according to ResourceKind
		switch(pResourceKind) {
		case BRICK:
			aTexture = aClayTextureSolid;
			break;
		case GRAIN:
			aTexture = aDesertTextureSolid;
			break;
		case LUMBER:
			aTexture = aForrestTextureSolid;
			break;
		case ORE:
			aTexture = aStoneTextureSolid;
			break;
		case WOOL:
			aTexture = aClayTextureSolid;
			break;
		default:
			break;
		}
		
		PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
				new float[] {      // Six vertices
						xPos - base, yPos - length/2,        		// Vertex 0                4
						xPos, yPos - length,       		    		// Vertex 1           5         3
						xPos + base, yPos - length/2,		  	    // Vertex 2         
						xPos + base, yPos + length/2,    	   	    // Vertex 3           0         2 
						xPos, yPos + length,        				// Vertex 4                1
						xPos - base, yPos + length/2				// Vertex 5
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


class Pair<L,R> {

	  private final L left;
	  private final R right;

	  public Pair(L left, R right) {
	    this.left = left;
	    this.right = right;
	  }

	  public L getLeft() { return left; }
	  public R getRight() { return right; }

	  @Override
	  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (!(o instanceof Pair)) return false;
	    Pair pairo = (Pair) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }

	}

