package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.Hex;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SessionScreen implements Screen {

    /**
     * The map of resources to colors
     */
    private static Map<String, Color> colorMap;

    static {
        // TODO move this to the skin
        colorMap = new HashMap<>();
        colorMap.put("wood", Color.LIME);
        colorMap.put("brick", Color.BROWN);
        colorMap.put("ore", Color.GRAY);
        colorMap.put("grain", Color.YELLOW);
        colorMap.put("wool", Color.GREEN);
        colorMap.put("coin", Color.GOLD);
    }

    private final CatanGame aGame;

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int SIZE = GameRules.getGameRulesInstance().getSize();      // number of tiles at longest diagonal
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis
    private final int PIECEBASE = (int) (LENGTH * 0.4);

    //Temporary
    private final int XCENTER;
    private final int YCENTER;

    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
    SpriteBatch fontBatch = new SpriteBatch(); //Q: can/should we use polyBatch to draw fonts, or do we need a new batch for it?

    Texture aSeaTextureSolid;
    Texture aDesertTextureSolid;
    Texture aHillsTextureSolid;
    Texture aForestTextureSolid;
    Texture aMountainTextureSolid;
    Texture aPastureTextureSolid;
    Texture aFieldsTextureSolid;
    Texture aGoldfieldTextureSolid;

    Texture aOrangeTextureSolid;
    Texture aRedTextureSolid;
    Texture aWhiteTextureSolid;
    Texture aBlueTextureSolid;
    Texture aYellowTextureSolid;

    private SessionController aSessionController;

    private Stage aSessionStage;

    private Texture bg;

    /**
     * The list of polygons representing the board hexes
     */
    private List<PolygonRegion> boardHexes;

    /**
     * The List of villages currently on the board
     */
    private List<PolygonRegion> villages;

    /**
     * The List of EdgeUnits currently on the board
     */
    private List<PolygonRegion> edgeUnits;

    /**
     * The origin of the the hex board
     */
    private MutablePair<Integer, Integer> boardOrigin;


    /**
     * The map of resource tables
     */
    private Map<String, Label> resourceLabelMap;

    public SessionScreen(CatanGame pGame) {
        aGame = pGame;
        boardHexes = new ArrayList<>();
        villages = new ArrayList<>();
        edgeUnits = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        resourceLabelMap = new HashMap<>();
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        XCENTER = 2 * Gdx.graphics.getWidth() / 5;
        YCENTER = 3 * Gdx.graphics.getHeight() / 5;
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

        Table contentTable = new Table(CatanGame.skin);
        contentTable.setBackground("resTableBackground");
        contentTable.setSize(550, 120);
        contentTable.setPosition(400, 20);

        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            Table aTable = createResourceTable(entry.getKey());
            contentTable.add(aTable).pad(5);
        }

        // Creating the color filling for hexagons
        aSeaTextureSolid = setupTextureSolid(Color.TEAL);
        aDesertTextureSolid = setupTextureSolid(Color.GRAY);
        aHillsTextureSolid = setupTextureSolid(Color.valueOf("FFB386"));
        aForestTextureSolid = setupTextureSolid(Color.valueOf("679861"));
        aMountainTextureSolid = setupTextureSolid(Color.valueOf("996633"));
        aPastureTextureSolid = setupTextureSolid(Color.valueOf("66FF66"));
        aFieldsTextureSolid = setupTextureSolid(Color.valueOf("FFFF66"));
        aGoldfieldTextureSolid = setupTextureSolid(Color.valueOf("FF9A00"));

        //Creating the color filling for player pieces
        aOrangeTextureSolid = setupTextureSolid(Color.ORANGE);
        aRedTextureSolid = setupTextureSolid(Color.RED);
        aWhiteTextureSolid = setupTextureSolid(Color.WHITE);
        aBlueTextureSolid = setupTextureSolid(Color.BLUE);
        aYellowTextureSolid = setupTextureSolid(Color.YELLOW);

        // sets center of board
        int xCenter = XCENTER;
        int yCenter = YCENTER;
        int offsetX, offsetY;

        for (Hex hex : aSessionController.getHexes()) {
            offsetX = hex.getLeftCoordinate();
            offsetY = hex.getRightCoordinate();
            createHexagon(xCenter + (offsetX * OFFX), yCenter - (offsetY * OFFY), LENGTH, BASE, hex.getKind());
        }

        // for testing purposes, puts a settlement on every intersection of the board //TODO remove when done
        int i = 0;
        for (CoordinatePair<Integer, Integer> coor : aSessionController.getIntersectionsAndEdges()) {
            updateIntersection(coor, PlayerColor.values()[i++ % 5], VillageKind.values()[i%3]);
        }

        // for testing purposes, removes some arbitrary village //TODO remove when done
        removeVillage(1, -1);
        removeVillage(2, -2);
        removeVillage(0, -2);
        removeVillage(1, 1);
        removeVillage(2, 2);
        createRoad(1, -1, 2, -2, PlayerColor.WHITE);
        createRoad(0, -2, 1, -1, PlayerColor.WHITE);
        createRoad(1, 1, 2, 2, PlayerColor.RED);
        createRoad(0, 2, 1, 1, PlayerColor.ORANGE);
        createRoad(1, -1, 1, 1, PlayerColor.WHITE);
        removeEdgeUnit(1, -1, 2, -2);

        // FOR TEST
        //showDice();
        aSessionStage.addActor(contentTable);
    }

    private Texture setupTextureSolid(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color); // DE is red, AD is green and BE is blue.
        pix.fill();
        return new Texture(pix);
    }

    private Table createResourceTable(String type) {
        Table resourceTable = new Table(CatanGame.skin);
        resourceTable.add(new Label(type, CatanGame.skin));
        resourceTable.row();

        Label l = new Label("0", CatanGame.skin);
        resourceTable.add(l);
        resourceLabelMap.put(type, l);

        resourceTable.setBackground(CatanGame.skin.newDrawable("white", colorMap.get(type)));
        resourceTable.setSize(60, 60);
        resourceTable.pad(10);
        return resourceTable;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Display the background
        aGame.batch.begin();
        aGame.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aGame.batch.end();

        // Display the board hexes and game pieces
        polyBatch.begin();
        for (PolygonRegion boardHex : boardHexes) {
            polyBatch.draw(boardHex, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        for (PolygonRegion edgeUnit : edgeUnits) {
            polyBatch.draw(edgeUnit, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        for (PolygonRegion village : villages) {
            polyBatch.draw(village, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        polyBatch.end();
        
        fontBatch.begin();
        for (Hex hex : aSessionController.getHexes()) {
            Integer prob = GameRules.getGameRulesInstance().getDiceNumber(hex);
            if (prob != 0 && prob != null) {
                //FIXME: screen center coordinates do not work as expected? (fix centering of boardgame)
                float xPos =  (float) (Gdx.graphics.getWidth() * 0.52 + (hex.getLeftCoordinate() * OFFX));
                float yPos = (float) (Gdx.graphics.getWidth() * 0.3 - (hex.getRightCoordinate() * OFFY));
                CatanGame.skin.getFont("default").draw(fontBatch, prob.toString(), xPos, yPos);
            }
        }
        fontBatch.end();

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
                0, 1, 4,         // Sets up triangulatio          n according to vertices above
                0, 4, 5,
                1, 2, 3,
                1, 3, 4
        });

        boardHexes.add(polyReg);
    }

    /**
     * Creates a settlement according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    private void createSettlement(int xCor, int yCor, PlayerColor color) {
        Texture aTexture = aSeaTextureSolid;

        switch (color) {
            case BLUE:
                aTexture = aBlueTextureSolid;
                break;
            case ORANGE:
                aTexture = aOrangeTextureSolid;
                break;
            case RED:
                aTexture = aRedTextureSolid;
                break;
            case WHITE:
                aTexture = aWhiteTextureSolid;
                break;
            case YELLOW:
                aTexture = aYellowTextureSolid;
                break;
            default:
                break;
        }

        //int xCenter = 2 * Gdx.graphics.getWidth() / 5;
        //int yCenter = 3 * Gdx.graphics.getHeight() / 5;
        float xPos = XCENTER + (xCor * BASE);
        float yPos = YCENTER - (yCor * LENGTH / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 0                
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 1           4        
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 4.0),        // Vertex 2		   3    2
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 4.0),        // Vertex 3         0    1     
                        xPos, (float) (yPos + PIECEBASE * 0.7),                                    // Vertex 4
                }, new short[]{
                0, 1, 2,         // Sets up triangulation according to vertices above
                0, 2, 3,
                3, 2, 4
        });
        villages.add(polyReg);
    }
    
    /**
     * Creates a city according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    private void createCity(int xCor, int yCor, PlayerColor color) {
        Texture aTexture = aSeaTextureSolid;

        switch (color) {
            case BLUE:
                aTexture = aBlueTextureSolid;
                break;
            case ORANGE:
                aTexture = aOrangeTextureSolid;
                break;
            case RED:
                aTexture = aRedTextureSolid;
                break;
            case WHITE:
                aTexture = aWhiteTextureSolid;
                break;
            case YELLOW:
                aTexture = aYellowTextureSolid;
                break;
            default:
                break;
        }

        float xPos = XCENTER + (xCor * BASE);
        float yPos = YCENTER - (yCor * LENGTH / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 0           3
                        (float) (xPos), (float) (yPos + PIECEBASE / 3.0),                          // Vertex 1         2    1  
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 3.0),        // Vertex 2              6  4 
                        (float) (xPos - PIECEBASE / 4.0), (float) (yPos + PIECEBASE * 0.7),        // Vertex 3         0       5 
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos),                          // Vertex 4
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 5
                        (float) (xPos), (float) (yPos),                                            // Vertex 6
                        
                }, new short[]{
                0, 5, 4,         // Sets up triangulation according to vertices above
                0, 4, 6,
                0, 6, 2,
                6, 1, 2,
                2, 1, 3
        });
        villages.add(polyReg);
    }
    
    /**
     * Creates a metropolis according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    private void createMetropolis(int xCor, int yCor, PlayerColor color) {
        Texture aTexture = aSeaTextureSolid;

        switch (color) {
            case BLUE:
                aTexture = aBlueTextureSolid;
                break;
            case ORANGE:
                aTexture = aOrangeTextureSolid;
                break;
            case RED:
                aTexture = aRedTextureSolid;
                break;
            case WHITE:
                aTexture = aWhiteTextureSolid;
                break;
            case YELLOW:
                aTexture = aYellowTextureSolid;
                break;
            default:
                break;
        }

        float xPos = XCENTER + (xCor * BASE);
        float yPos = YCENTER - (yCor * LENGTH / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 0
                        (float) (xPos/* + PIECEBASE / 2.0*/), (float) (yPos - PIECEBASE / 2.0),    // Vertex 1
                        (float) (xPos), (float) (yPos + PIECEBASE / 10.0),                          // Vertex 2
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 10.0),        // Vertex 3        12      7
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 4      13  11   8  6
                        (float) (xPos + PIECEBASE), (float) (yPos - PIECEBASE / 2.0),              // Vertex 5
                        (float) (xPos + PIECEBASE), (float) (yPos + PIECEBASE / 2.0),              // Vertex 6          10   9  
                        (float) (xPos + 3 * PIECEBASE / 4.0), (float) (yPos + PIECEBASE * 0.8),    // Vertex 7          2    3
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 2.0),        // Vertex 8       
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 3.0),        // Vertex 9       0  1    4  5
                        (float) (xPos), (float) (yPos + PIECEBASE / 3.0),                          // Vertex 10
                        (float) (xPos /*+ PIECEBASE / 2.0*/), (float) (yPos + PIECEBASE / 2.0),    // Vertex 11
                        (float) (xPos - PIECEBASE / 4.0), (float) (yPos + (PIECEBASE * 0.8)),      // Vertex 12
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 2.0),        // Vertex 13
                        
                }, new short[]{
                0, 1, 11,         // Sets up triangulation according to vertices above
                0, 11, 13,
                13, 11, 12,
                2, 3, 9, 
                2, 9, 10,
                4, 5, 6,
                4, 6, 8,
                8, 6, 7
                
        });
        villages.add(polyReg);
    }

    /**
     * Creates a settlement according to given position. Assumes the coordinates correspond to adjacent intersections
     *
     * @param xCorFirst  x coordinate of game piece first endpoint
     * @param yCorFirst  y coordinate of game piece first endpoint
     * @param xCorSecond x coordinate of game piece second endpoint
     * @param yCorSecond y coordinate of game piece second endpoint
     * @param color      of game piece
     */
    private void createRoad(int xCorFirst, int yCorFirst, int xCorSecond, int yCorSecond, PlayerColor color) {
        Texture aTexture = aSeaTextureSolid;

        switch (color) {
            case BLUE:
                aTexture = aBlueTextureSolid;
                break;
            case ORANGE:
                aTexture = aOrangeTextureSolid;
                break;
            case RED:
                aTexture = aRedTextureSolid;
                break;
            case WHITE:
                aTexture = aWhiteTextureSolid;
                break;
            case YELLOW:
                aTexture = aYellowTextureSolid;
                break;
            default:
                break;
        }

        //int xCenter = 2 * Gdx.graphics.getWidth() / 5;
        //int yCenter = 3 * Gdx.graphics.getHeight() / 5;
        int xCenter = XCENTER;
        int yCenter = YCENTER;
        float[] v0 = new float[2], v1 = new float[2], v2 = new float[2], v3 = new float[2], vm = new float[2];
        
        int xMin = Math.min(xCorFirst, xCorSecond);
        int xMax = Math.max(xCorFirst, xCorSecond);
        int yMin = Math.min(yCorFirst, yCorSecond);
        int yMax = Math.max(yCorFirst, yCorSecond);
        
        // Sets the identifying coordinates as middle between the two end points
        vm[0] = (float) (xMin + (xMax - xMin) / 2.0);
        vm[1] = (float) (yMin + (yMax - yMin) / 2.0);
        
        // Determines which direction the EdgeUnit will be facing, and gives appropriate vertex values
        if (xCorFirst == xCorSecond) {

            v0[0] = (float) ((xCenter + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v0[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v1[0] = (float) ((xCenter + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v1[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v2[0] = (float) ((xCenter + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v2[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

            v3[0] = (float) ((xCenter + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v3[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

        } else {
            if ((Math.min(xCorFirst, xCorSecond) == xCorFirst && Math.max(yCorFirst, yCorSecond) == yCorFirst) ||
                    (Math.min(xCorFirst, xCorSecond) == xCorSecond && Math.max(yCorFirst, yCorSecond) == yCorSecond)) {

                v0[0] = (float) ((xCenter + (xMin * BASE)) + PIECEBASE / 2.0);
                v0[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);

                v1[0] = (float) ((xCenter + (xMin * BASE)) + PIECEBASE / 4.0);
                v1[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v2[0] = (float) ((xCenter + (xMax * BASE)) - PIECEBASE / 2.0);
                v2[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v3[0] = (float) ((xCenter + (xMax * BASE)) - PIECEBASE / 4.0);
                v3[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            } else {

                v0[0] = (float) ((xCenter + (xMin * BASE)) + PIECEBASE / 4.0);
                v0[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

                v1[0] = (float) ((xCenter + (xMin * BASE)) + PIECEBASE / 2.0);
                v1[1] = (float) ((yCenter - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v2[0] = (float) ((xCenter + (xMax * BASE)) - PIECEBASE / 4.0);
                v2[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v3[0] = (float) ((xCenter + (xMax * BASE)) - PIECEBASE / 2.0);
                v3[1] = (float) ((yCenter - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);

            }
        }

        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{  
                        vm[0], vm[1],         // Vertex 0, for identification purposes 
                                             // describes the x y coordinates of the middle of an EdgeUnit
                        v0[0], v0[1],        // Vertex 1                
                        v1[0], v1[1],        // Vertex 2         2        3     (with rotation)  
                        v2[0], v2[1],        // Vertex 3		 1        4   
                        v3[0], v3[1]         // Vertex 4
                        

                }, new short[]{
                1, 4, 3,         // Sets up triangulation according to vertices above
                1, 3, 2
        });
        edgeUnits.add(polyReg);

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
     * @param xCor left coordinate of intersection
     * @param yCor right coordinate of intersection
     * @return PolygonRegion of intersection piece which lies on intersection with coordinates xCor and yCor, null if no game piece lies on that space
     */
    private PolygonRegion getIntersectionPiece(int xCor, int yCor) {

        for (PolygonRegion pr : villages) {
            float xV0 = pr.getVertices()[0];
            float yV0 = pr.getVertices()[1];
  
            int xCenter = XCENTER;
            int yCenter = YCENTER;
            float xPos = xCenter + (xCor * BASE);
            float yPos = yCenter - (yCor * LENGTH / 2);

            if ((float) xV0 == xPos - PIECEBASE / 2.0 && (float) yV0 == yPos - PIECEBASE / 2.0) {
                return pr;
            }
        }

        return null;
    }
    
    /**
     * @param xCorFirst left coordinate of first intersection
     * @param yCorFirst right coordinate of first intersection
     * @param xCorSecond left coordinate of second intersection
     * @param yCorSecond right coordinate of second intersection
     * @return PolygonRegion of edge piece which lies between (xCorFirst,yCorFirst) and (xCorSecond,yCorSecon), null if no game piece lies on that space
     */
    private PolygonRegion getEdgePiece(int xCorFirst, int yCorFirst, int xCorSecond, int yCorSecond) {
        
        for (PolygonRegion pr : edgeUnits) {
            float xVM = pr.getVertices()[0];
            float yVM = pr.getVertices()[1];
            
            float xCorM = (float) (Math.min(xCorFirst, xCorSecond) + (Math.max(xCorFirst, xCorSecond) - Math.min(xCorFirst, xCorSecond)) / 2.0);
            float yCorM = (float) (Math.min(yCorFirst, yCorSecond) + (Math.max(yCorFirst, yCorSecond) - Math.min(yCorFirst, yCorSecond)) / 2.0);
            
            if (xVM == xCorM && yVM == yCorM) {
                return pr;
            }
        }
        
        return null;
    }

    /**
     * removes polygon of village at given coordinates from the board
     *
     * @param xCor left coordinate of intersection
     * @param yCor right coordinate of intersection
     * @return true if a village was removed from the board
     */
    private boolean removeVillage(int xCor, int yCor) {
        PolygonRegion village = getIntersectionPiece(xCor, yCor);
        if (village != null) {
            villages.remove(village);
            return true;
        }
        return false;
    }
    
    /**
     * removes polygon of edge unit at given coordinates from the board
     *
     * @param xCorFirst left coordinate of first intersection
     * @param yCorFirst right coordinate of first intersection
     * * @param xCorSecond left coordinate of second intersection
     * @param yCorSecond right coordinate of second intersection
     * @return true if an edge unit was removed from the board
     */
    public boolean removeEdgeUnit(int xCorFirst, int yCorFirst, int xCorSecond, int yCorSecond) {
        PolygonRegion edgePiece = getEdgePiece(xCorFirst, yCorFirst, xCorSecond, yCorSecond);
        if (edgePiece != null) {
            edgeUnits.remove(edgePiece);
            return true;
        }
        return false;
    }


    /**
     * renders the village with appropriate color and kind at the given position. If position is already occupied, the currently placed village will be removed and replaced
     *
     * @param position of intersection to update
     * @param color    of player who owns the new Village
     * @param kind     of new Village
     */
    public void updateIntersection(CoordinatePair<Integer, Integer> position, PlayerColor color, VillageKind kind) {

        int offsetX = position.getLeft();
        int offsetY = position.getRight();

        // Removes village on given coordinate
        removeVillage(offsetX, offsetY);

        switch (kind) {
            case CITY:
                createCity(offsetX, offsetY, color);
                break;
            case SCIENCEMETROPOLE:
                createMetropolis(offsetX, offsetY, color);
                break;
            case SETTLEMENT:
                createSettlement(offsetX, offsetY, color);
                break;
            case TRADEMETROPLE:
                createMetropolis(offsetX, offsetY, color);
                break;
            default:
                break;

        }
    }

    /**
     * renders the road with appropriate color and position. If edge is already occupied, it removes current edge piece and replaces it with this one
     *
     * @param firstCoordinate  end point of edge
     * @param secondCoordinate other end point of edge
     * @param kind             of new edge unit (SHIP or ROAD)
     * @param color            of player who owns the new edge unit
     */
    public void updateEdge(CoordinatePair<Integer, Integer> firstCoordinate, CoordinatePair<Integer, Integer> secondCoordinate, EdgeUnitKind kind, PlayerColor color) {
        int xCorFirst = firstCoordinate.getLeft();
        int yCorFirst = firstCoordinate.getRight();
        int xCorSecond = secondCoordinate.getLeft();
        int yCorSecond = secondCoordinate.getRight();
        
        // removes edge on given coordinate
        removeEdgeUnit(xCorFirst, yCorFirst, xCorSecond, yCorSecond);
        
        switch(kind) {
        case ROAD:
            createRoad(xCorFirst, yCorFirst, xCorSecond, yCorSecond, color);
            break;
        case SHIP:
            break;
        default:
            break;
        }
    }

    /**
     * moves the robber to given position
     *
     * @param position new hex that the robber will be moved to
     */
    public void updateRobberPosition(Hex position) {
        //TODO
    }

    // TODO TEST 
    public void showDice() {
        int yellowDice = aSessionController.getYellowDice();
        int redDice = aSessionController.getRedDice();

        // FOR TEST
        //int yellowDice = 5;
        //int redDice = 1;

        Table yellow = new Table();
        Table red = new Table();

        yellow.setBackground(CatanGame.skin.newDrawable("white", Color.YELLOW));
        red.setBackground(CatanGame.skin.newDrawable("white", Color.RED));

        yellow.setSize(60, 60);
        red.setSize(60, 60);

        yellow.setPosition(1050, 600);
        red.setPosition(1120, 600);

        yellow.add(new Label("" + yellowDice, CatanGame.skin));
        red.add(new Label("" + redDice, CatanGame.skin));

        aSessionStage.addActor(yellow);
        aSessionStage.addActor(red);
    }

    public void updateResourceBar(ArrayList<Integer> updates) {
        Iterator<Integer> it = updates.iterator();
        for (Map.Entry<String, Label> entry : resourceLabelMap.entrySet()) {
            Label l = entry.getValue();
            int newValue = it.next() + Integer.valueOf(l.getText().toString());
            l.setText(newValue + "");
        }
    }
}

