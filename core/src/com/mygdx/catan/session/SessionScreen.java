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
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.gameboard.GameBoardManager;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SessionScreen implements Screen {

    private final CatanGame aGame;
    private final GameBoardManager aGameBoardManager;

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int SIZE = 7;                                               // number of tiles at longest diagonal
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis

    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
    Texture aWaterTextureSolid;
    Texture aDesertTextureSolid;
    Texture aClayTextureSolid;
    Texture aForrestTextureSolid;
    Texture aStoneTextureSolid;

    private Stage aSessionStage;
    private Texture bg;
    private Random rd = new Random();
    private ArrayList<CoordinatePair<Integer, Integer>> aHexPositions;
    private ArrayList<CoordinatePair<Integer, Integer>> aIntersectionPositions;
    private HashMap<CoordinatePair<Integer, Integer>, ResourceKind> aHexKindSetup;

    /** The list of polygons representing the board hexes */
    private List<PolygonRegion> boardHexes;

    /** The origin of the the hex board */
    private MutablePair<Integer, Integer> boardOrigin;

    public SessionScreen(CatanGame pGame, GameBoardManager pGameBoardManager) {
        aGame = pGame;
        aGameBoardManager = pGameBoardManager;
        boardHexes = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        aSessionStage = new Stage();
        Gdx.input.setInputProcessor(aSessionStage);

        // initialize hex position coordinates, where x=(aHexPositions[i].getLeft()) and y=(aHexPositions[i].getRight())
        // the coordinates describe the offset from the center.
        aHexPositions = new ArrayList<>();
        aHexKindSetup = new HashMap<>();
        aIntersectionPositions = new ArrayList<>();
        int half = SIZE / 2;

        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = -cols + 2 * col + 1;
                int y = (row - half);
                CoordinatePair<Integer, Integer> hexCoord = new CoordinatePair<>(x, y);
                aHexKindSetup.put(hexCoord, ResourceKind.values()[rd.nextInt(ResourceKind.values().length)]);
                aHexPositions.add(hexCoord);

                // Creates the top, and top left points adjacent to current hex
                aIntersectionPositions.add(new CoordinatePair<>(x - 1, y * 3 - 1));
                aIntersectionPositions.add(new CoordinatePair<>(x, y * 3 - 2));

                // If at last row, create bottom and bottom left points
                if (row == SIZE - 1) {
                    aIntersectionPositions.add(new CoordinatePair<>(x - 1, y * 3 + 1));
                    aIntersectionPositions.add(new CoordinatePair<>(x, y * 3 + 2));
                }
            }
            // If the hex is the last column of a row, creates the top right point
            aIntersectionPositions.add(new CoordinatePair<>((cols - 1) + 1, (row - half) * 3 - 1));
        }

        // Create bottom right point of last column and last row
        aIntersectionPositions.add(new CoordinatePair<>(half + 1, (half) * 3 + 1));

        //TODO: UI panels


        // Creating the color filling for hexagons
        aWaterTextureSolid = setupTextureSolid(Color.BLUE);
        aDesertTextureSolid = setupTextureSolid(Color.YELLOW);
        aClayTextureSolid = setupTextureSolid(Color.RED);
        aForrestTextureSolid = setupTextureSolid(Color.GREEN);
        aStoneTextureSolid = setupTextureSolid(Color.GRAY);

        // sets center of board
        int xCenter = 2 * Gdx.graphics.getWidth() / 5;
        int yCenter = 3 * Gdx.graphics.getHeight() / 5;
        int offsetX, offsetY;

        // create hexagons according to coordinates stored in aHexPositions and hex kinds stored in aHexKindSetup
        for (CoordinatePair<Integer, Integer> hexPosition : aHexPositions) {
            offsetX = hexPosition.getLeft();
            offsetY = hexPosition.getRight();
            createHexagon(xCenter + (offsetX * OFFX), yCenter + (offsetY * OFFY), LENGTH, BASE, aHexKindSetup.get(hexPosition));
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
    private void createHexagon(int xPos, int yPos, int length, int base, ResourceKind pResourceKind) {

        Texture aTexture = aWaterTextureSolid;

        // sets aTexture to relevant texture according to ResourceKind
        switch (pResourceKind) {
            case BRICK:
                aTexture = aClayTextureSolid;
                break;
            case GRAIN:
                aTexture = aDesertTextureSolid;
                break;
            case WOOD:
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

}

