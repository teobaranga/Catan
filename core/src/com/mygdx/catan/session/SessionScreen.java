package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.gameboard.Hex;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class SessionScreen implements Screen {

    private final CatanGame aGame;
    private SessionController aSessionController;

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int SIZE = GameRules.getGameRulesInstance().getSize();      // number of tiles at longest diagonal
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis

    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
    Texture aSeaTextureSolid;
    Texture aDesertTextureSolid;
    Texture aHillsTextureSolid;
    Texture aForestTextureSolid;
    Texture aMountainTextureSolid;
    Texture aPastureTextureSolid;
    Texture aFieldsTextureSolid;
    Texture aGoldfieldTextureSolid;

    private Stage aSessionStage;
    private Texture bg;

    /** The list of polygons representing the board hexes */
    private List<PolygonRegion> boardHexes;

    /** The origin of the the hex board */
    private MutablePair<Integer, Integer> boardOrigin;

    public SessionScreen(CatanGame pGame) {
        aGame = pGame;
        boardHexes = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    
    public void setSessionController(SessionController sc) {
    	aSessionController = sc;
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aSessionStage = new Stage();
        Gdx.input.setInputProcessor(aSessionStage);  

        //TODO: UI panels

        // Creating the color filling for hexagons
        aSeaTextureSolid = setupTextureSolid(Color.CYAN);
        aDesertTextureSolid = setupTextureSolid(Color.BLACK);
        aHillsTextureSolid = setupTextureSolid(Color.BROWN);
        aForestTextureSolid = setupTextureSolid(Color.GREEN);
        aMountainTextureSolid = setupTextureSolid(Color.GRAY);
        aPastureTextureSolid = setupTextureSolid(Color.LIME);
        aFieldsTextureSolid = setupTextureSolid(Color.YELLOW);
        aGoldfieldTextureSolid = setupTextureSolid(Color.GOLD);
        

        // sets center of board
        int xCenter = 2 * Gdx.graphics.getWidth() / 5;
        int yCenter = 3 * Gdx.graphics.getHeight() / 5;
        int offsetX, offsetY;
        
        for (Hex hex : aSessionController.getHexes()) {
        	offsetX = hex.getLeftCoordinate();
        	offsetY = hex.getRightCoordinate();
        	createHexagon(xCenter + (offsetX * OFFX), yCenter - (offsetY * OFFY), LENGTH, BASE, hex.getKind());
        }

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

        // Display the background
        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        // Display the board hexes
        polyBatch.begin();
        for (PolygonRegion boardHex : boardHexes) {
            polyBatch.draw(boardHex, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        polyBatch.end();

        aSessionStage.act(delta);
        aSessionStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // FIXME: Does not have the intended effect
        setupBoardOrigin(width, height);

        aSessionStage.getViewport().update(width, height, false);
        aGame.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void pause() {
        // Nothing to do
    }

    @Override
    public void resume() {
        // Nothing to do
    }

    @Override
    public void hide() {
        boardHexes.clear();
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        aSessionStage.dispose();
    }

    /**
     * Creates a hexagon according to given position and length
     *
     * @param xPos   x position of hexagon center
     * @param yPos   y position of hexagon center
     * @param length length of the side of the hexagon
     */
    private void createHexagon(int xPos, int yPos, int length, int base, TerrainKind pTerrainKind) {

        Texture aTexture = aSeaTextureSolid;

        // sets aTexture to relevant texture according to ResourceKind
        switch (pTerrainKind) {
            case HILLS:
                aTexture = aHillsTextureSolid;
                break;
            case FIELDS:
                aTexture = aFieldsTextureSolid;
                break;
            case FOREST:
                aTexture = aForestTextureSolid;
                break;
            case MOUNTAINS:
                aTexture = aMountainTextureSolid;
                break;
            case PASTURE:
                aTexture = aPastureTextureSolid;
                break;
            case SEA:
            	break;
            case DESERT:
            	aTexture = aDesertTextureSolid;
            	break;
            case GOLDFIELD:
                aTexture = aGoldfieldTextureSolid;
            	break;
            default:
                break;
        }

        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        xPos - base, yPos - length / 2,             // Vertex 0                4
                        xPos, yPos - length,                        // Vertex 1           5         3
                        xPos + base, yPos - length / 2,             // Vertex 2
                        xPos + base, yPos + length / 2,             // Vertex 3           0         2
                        xPos, yPos + length,                        // Vertex 4                1
                        xPos - base, yPos + length / 2              // Vertex 5
                }, new short[]{
                0, 1, 4,         // Sets up triangulation according to vertices above
                0, 4, 5,
                1, 2, 3,
                1, 3, 4
        });

        boardHexes.add(polyReg);
    }

    /**
     * Set the coordinates of the board origin.
     * Currently the board origin is such that the board appears centered on screen.
     *
     * @param width  Width of the screen
     * @param height Height of the screen
     */
    private void setupBoardOrigin(int width, int height) {
        // Coordinates that make the board centered on screen
        // The offset calculations are a bit weird and unintuitive but it works
        boardOrigin.setLeft(((int) (width / 2f)) - OFFX * SIZE * 2);
        boardOrigin.setRight(((int) (height / 2f)) - OFFY * SIZE);
    }
    
    /**
     * renders the village with appropriate color and kind at the given position. If position is already occupied, the currently placed village will be removed and replaced
     * @param position of intersection to update
     * */
    public void updateIntersection(CoordinatePair<Integer,Integer> position, PlayerColor color, VillageKind kind, boolean build) {
    	
    }

}

