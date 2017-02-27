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
    private final int PIECEBASE = LENGTH / 3;

    //Temporary
    private final int XCENTER;
    private final int YCENTER;

    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning

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
        aSeaTextureSolid = setupTextureSolid(Color.CYAN);
        aDesertTextureSolid = setupTextureSolid(Color.BLACK);
        aHillsTextureSolid = setupTextureSolid(Color.BROWN);
        aForestTextureSolid = setupTextureSolid(Color.GREEN);
        aMountainTextureSolid = setupTextureSolid(Color.GRAY);
        aPastureTextureSolid = setupTextureSolid(Color.LIME);
        aFieldsTextureSolid = setupTextureSolid(Color.YELLOW);
        aGoldfieldTextureSolid = setupTextureSolid(Color.GOLD);

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
            // System.out.println(coor.getLeft() +" "+ coor.getRight());
            updateIntersection(coor, PlayerColor.values()[i++ % 5], VillageKind.SETTLEMENT);
        }

        // for testing purposes, removes some arbitrary village //TODO remove when done
        removeVillage(1, -1);
        removeVillage(2, -2);
        removeVillage(0, -2);
        removeVillage(1, 1);
        removeVillage(2, 2);
        createRoad(1, -1, 2, -2, PlayerColor.WHITE);
        createRoad(0, -2, 1, -1, PlayerColor.WHITE);
        createRoad(1, 1, 2, 2, PlayerColor.WHITE);
        createRoad(0, 2, 1, 1, PlayerColor.WHITE);
        createRoad(1, -1, 1, 1, PlayerColor.WHITE);

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

        // Display the board hexes
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
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 2.0),        // Vertex 2		   3    2
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 2.0),        // Vertex 3         0    1     
                        xPos, yPos + PIECEBASE,                            // Vertex 4
                }, new short[]{
                0, 1, 2,         // Sets up triangulation according to vertices above
                0, 2, 3,
                3, 2, 4
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

        //TODO set middle vertex (for identification purposes)
        // Determines which direction the EdgeUnit will be facing, and gives appropriate vertex values
        if (xCorFirst == xCorSecond) {

            v0[0] = (float) ((xCenter + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v0[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 2.0);

            v1[0] = (float) ((xCenter + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v1[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 2.0);

            v2[0] = (float) ((xCenter + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v2[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 2.0);

            v3[0] = (float) ((xCenter + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v3[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 2.0);

        } else {
            if ((Math.min(xCorFirst, xCorSecond) == xCorFirst && Math.max(yCorFirst, yCorSecond) == yCorFirst) ||
                    (Math.min(xCorFirst, xCorSecond) == xCorSecond && Math.max(yCorFirst, yCorSecond) == yCorSecond)) {

                v0[0] = (float) ((xCenter + (Math.min(xCorFirst, xCorSecond) * BASE)) + PIECEBASE / 2.0);
                v0[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 15.0);

                v1[0] = (float) ((xCenter + (Math.min(xCorFirst, xCorSecond) * BASE)) + PIECEBASE / 4.0);
                v1[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 2.0);

                v2[0] = (float) ((xCenter + (Math.max(xCorFirst, xCorSecond) * BASE)) - PIECEBASE / 2.0);
                v2[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 15.0);

                v3[0] = (float) ((xCenter + (Math.max(xCorFirst, xCorSecond) * BASE)) - PIECEBASE / 4.0);
                v3[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 2.0);

            } else {

                v0[0] = (float) ((xCenter + (Math.min(xCorFirst, xCorSecond) * BASE)) + PIECEBASE / 4.0);
                v0[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 2.0);

                v1[0] = (float) ((xCenter + (Math.min(xCorFirst, xCorSecond) * BASE)) + PIECEBASE / 2.0);
                v1[1] = (float) ((yCenter - (Math.min(yCorFirst, yCorSecond) * LENGTH / 2)) - PIECEBASE / 15.0);

                v2[0] = (float) ((xCenter + (Math.max(xCorFirst, xCorSecond) * BASE)) - PIECEBASE / 4.0);
                v2[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 2.0);

                v3[0] = (float) ((xCenter + (Math.max(xCorFirst, xCorSecond) * BASE)) - PIECEBASE / 2.0);
                v3[1] = (float) ((yCenter - (Math.max(yCorFirst, yCorSecond) * LENGTH / 2)) + PIECEBASE / 15.0);

            }
        }

        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        v0[0], v0[1],        // Vertex 0                
                        v1[0], v1[1],        // Vertex 1         1        2     (with rotation)  
                        v2[0], v2[1],        // Vertex 2		 0        3   
                        v3[0], v3[1],        // Vertex 3

                }, new short[]{
                0, 3, 2,         // Sets up triangulation according to vertices above
                0, 2, 1
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
     * @return PolygonRegion which lies on intersection with coordinates xCor and yCor, null if no game piece lies on that space
     */
    private PolygonRegion getBoardGamePiece(int xCor, int yCor) {

        for (PolygonRegion pr : villages) {
            float xV0 = pr.getVertices()[0];
            float yV0 = pr.getVertices()[1];

            //int xCenter = 2 * Gdx.graphics.getWidth() / 5;							// will break if screen is resized 
            //int yCenter = 3 * Gdx.graphics.getHeight() / 5;
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
     * removes polygon of given coordinates from the board
     *
     * @param xCor left coordinate of intersection
     * @param yCor right coordinate of intersection
     * @return true if a village was removed from the board
     */
    private boolean removeVillage(int xCor, int yCor) {
        PolygonRegion village = getBoardGamePiece(xCor, yCor);
        if (village != null) {
            villages.remove(village);
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

        if (removeVillage(offsetX, offsetY)) {
            System.out.println("remove: " + offsetX + " " + offsetY);
        }

        switch (kind) {
            case CITY:
                break;
            case SCIENCEMETROPOLE:
                break;
            case SETTLEMENT:
                createSettlement(offsetX, offsetY, color);
                break;
            case TRADEMETROPLE:
                break;
            default:
                break;

        }
    }

    /**
     * renders the road with appropriate color and position.
     *
     * @param firstCoordinate  end point of edge
     * @param secondCoordinate other end point of edge
     * @param kind             of new edge unit (SHIP or ROAD)
     * @param color            of player who owns the new edge unit
     */
    public void updateEdge(CoordinatePair<Integer, Integer> firstCoordinate, CoordinatePair<Integer, Integer> secondCoordinate, EdgeUnitKind kind, PlayerColor color) {
        //TODO
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

