package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.SessionScreenModes;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
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
        colorMap.put("wood", Color.valueOf("679861"));
        colorMap.put("brick", Color.valueOf("CC6633"));
        colorMap.put("ore", Color.valueOf("996633"));
        colorMap.put("grain", Color.valueOf("FFFF66"));
        colorMap.put("wool", Color.valueOf("66FF66"));
        colorMap.put("coin", Color.valueOf("FF9A00"));
        colorMap.put("cloth", Color.valueOf("CDCDFF"));
        colorMap.put("paper", Color.valueOf("E6E6B9"));
    }

    private final CatanGame aGame;

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int SIZE = GameRules.getGameRulesInstance().getSize();      // number of tiles at longest diagonal
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis
    private final int PIECEBASE = (int) (LENGTH * 0.4);

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
    
    Texture aRobberTextureSolid;

    private SessionController aSessionController;

    private Stage aSessionStage;

    private Texture bg;

    /** The list of polygons representing the board hexes */
    private List<PolygonRegion> boardHexes;

    /** The List of villages currently on the board */
    private List<PolygonRegion> villages;

    /** The List of EdgeUnits currently on the board */
    private List<PolygonRegion> edgeUnits;
    
    /** The Robber */
    private PolygonRegion robber;

    /** The origin of the the hex board */
    private MutablePair<Integer, Integer> boardOrigin;

    /** The map of resource tables */
    private Map<String, Label> resourceLabelMap;
    
    /** determines the current mode of the session screen */
    private SessionScreenModes aMode;
    
    /** The Lists of valid building intersections. Is empty if aMode != CHOOSEINTERSECTIONMODE || != CHOSEEDGEMODE*/
    private ArrayList<CoordinatePair<Integer,Integer>> validIntersections = new ArrayList<CoordinatePair<Integer,Integer>>();
    private ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> validEdges = new ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>>();
    
    /** determines which kind of game piece is being built. If aMode is not in choose X mode, the values do not matter */
    private VillageKind villagePieceKind;
    private EdgeUnitKind edgePieceKind;
    
    /** Menu Buttons */
    private TextButton buildSettlementButton;
    private TextButton buildCityButton;
    
    
    public SessionScreen(CatanGame pGame) {
        aGame = pGame;
        boardHexes = new ArrayList<>();
        villages = new ArrayList<>();
        edgeUnits = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        resourceLabelMap = new HashMap<>();
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // sets the initializing mode of the session screen (VIEWMODE). set to CHOOSEACTIONMODE for testing purposes 
        aMode = SessionScreenModes.CHOOSEACTIONMODE;
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

        // resource table
        Table contentTable = new Table(CatanGame.skin);
        contentTable.setBackground("resTableBackground");
        contentTable.setSize(550, 120);
        contentTable.setPosition(400, 20);

        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            Table aTable = createResourceTable(entry.getKey());
            contentTable.add(aTable).pad(5);
        }

        
        // menu table
        Table menuTable = new Table(CatanGame.skin);
        menuTable.setBackground("resTableBackground");
        menuTable.setSize(200, 300);
        menuTable.setPosition(20, 20);
        
        // creates the menu buttons TODO:remainder of buttons
        buildSettlementButton = new TextButton("Build Settlement",CatanGame.skin);
        setupBuildVillageButton(buildSettlementButton, VillageKind.SETTLEMENT, "Build Settlement");
        buildSettlementButton.pad(0, 10, 0, 10);
        menuTable.add(buildSettlementButton).padBottom(10).row();
        
        buildCityButton = new TextButton("Build City",CatanGame.skin);
        setupBuildVillageButton(buildCityButton, VillageKind.CITY, "Build City");
        buildCityButton.pad(0, 10, 0, 10);
        menuTable.add(buildCityButton).padBottom(10).row();
        

        // Creating the color filling for hexagons
        aSeaTextureSolid = setupTextureSolid(Color.TEAL);
        aDesertTextureSolid = setupTextureSolid(Color.DARK_GRAY);
        aHillsTextureSolid = setupTextureSolid(colorMap.get("brick"));
        aForestTextureSolid = setupTextureSolid(colorMap.get("wood"));
        aMountainTextureSolid = setupTextureSolid(colorMap.get("ore"));
        aPastureTextureSolid = setupTextureSolid(colorMap.get("wool"));
        aFieldsTextureSolid = setupTextureSolid(colorMap.get("grain"));
        aGoldfieldTextureSolid = setupTextureSolid(colorMap.get("coin"));

        //Creating the color filling for player pieces
        aOrangeTextureSolid = setupTextureSolid(Color.ORANGE);
        aRedTextureSolid = setupTextureSolid(Color.RED);
        aWhiteTextureSolid = setupTextureSolid(Color.WHITE);
        aBlueTextureSolid = setupTextureSolid(Color.BLUE);
        aYellowTextureSolid = setupTextureSolid(Color.YELLOW);
        
        //Creating the color filling for the robber piece
        aRobberTextureSolid = setupTextureSolid(Color.valueOf("666666"));

        // sets center of board
        int offsetX, offsetY;

        // creates hexes of the board
        for (Hex hex : aSessionController.getHexes()) {
            offsetX = hex.getLeftCoordinate();
            offsetY = hex.getRightCoordinate();
            createHexagon((offsetX * OFFX),-(offsetY * OFFY), LENGTH, BASE, hex.getKind());
        }
        
        // places robber at initial robber position
        Hex robberPos = aSessionController.getRobberPosition();
        if (robberPos != null) {
            updateRobberPosition(robberPos);
        }

        // for testing purposes, puts a settlement on every intersection of the board //TODO remove when done
        //int i = 0;
        //for (CoordinatePair<Integer, Integer> coor : aSessionController.getIntersectionsAndEdges()) {
        //    updateIntersection(coor, PlayerColor.values()[i++ % 5], VillageKind.values()[i%3]);
        //}

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
        createShip(0, 2, 0, 4, PlayerColor.RED);

        aSessionStage.addActor(contentTable);
        aSessionStage.addActor(menuTable);
    }

    private void setupBuildVillageButton(TextButton buildButton, VillageKind kind, String buttonText) {
        buildButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buildButton.setChecked(false);
             // TODO: ask SessionController if there are enough resources
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    // make the following loop go through requested valid build positions
                    for (CoordinatePair<Integer,Integer> intersections : aSessionController.getIntersectionsAndEdges()) {
                        validIntersections.add(intersections);
                        //TODO make a sprite that highlights the area
                    }
                    aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
                    villagePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE) {
                    validIntersections.clear();
                    buildButton.setText(buttonText);
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                }
            }
        });
    }
    
    private void setupBuildEdgeUnitButton(TextButton buildButton, EdgeUnitKind kind, String buttonText) {
        buildButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buildButton.setChecked(false);
             // TODO: ask SessionController if there are enough resources
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    // make the following loop go through requested valid build positions
                    for (CoordinatePair<Integer,Integer> i : aSessionController.getIntersectionsAndEdges()) {
                        
                        Pair<Integer,Integer> e0 = new MutablePair<Integer,Integer>(i.getLeft(),i.getRight());
                        Pair<Integer,Integer> e1 = new MutablePair<Integer,Integer>(i.getLeft(),i.getRight() - 2);
                        Pair<Integer,Integer> e2 = new MutablePair<Integer,Integer>(i.getLeft() - 1,i.getRight() + 1);
                        Pair<Integer,Integer> e3 = new MutablePair<Integer,Integer>(i.getLeft() + 1,i.getRight() + 1);
                        
                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> r1 = new MutablePair<Pair<Integer, Integer>, Pair<Integer, Integer>>(e0,e1);
                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> r2 = new MutablePair<Pair<Integer, Integer>, Pair<Integer, Integer>>(e0,e2);
                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> r3 = new MutablePair<Pair<Integer, Integer>, Pair<Integer, Integer>>(e0,e3);
                        
                        validEdges.add(r1);
                        validEdges.add(r2);
                        validEdges.add(r3);
                        
                        for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                            if (eu.hasEndpoint(i) && eu.hasEndpoint(e1.getLeft(), e1.getRight())) { validEdges.remove(r1); }
                            if (eu.hasEndpoint(i) && eu.hasEndpoint(e2.getLeft(), e2.getRight())) { validEdges.remove(r2); }
                            if (eu.hasEndpoint(i) && eu.hasEndpoint(e3.getLeft(), e3.getRight())) { validEdges.remove(r3); }
                        }
                        
                        //TODO remove edges that are in water
                        
                        //TODO make a sprite that highlights the areas
                    }
                    aMode = SessionScreenModes.CHOOSEEDGEMODE;
                    edgePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {
                    
                    buildButton.setText(buttonText);
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                }
            }
        });
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
        l.setText("0");
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

        // 
        if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE) {
            for (CoordinatePair<Integer,Integer> validIntersection : validIntersections) {
                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                   
                    if (Gdx.input.getX() > boardOrigin.getLeft() + validIntersection.getLeft() * BASE - 10 &&
                            Gdx.input.getX() < boardOrigin.getLeft() + validIntersection.getLeft() * BASE + 10 &&
                            Gdx.input.getY() > boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) - 10 &&
                            Gdx.input.getY() < boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) + 10) {
                        
                        //TODO: call buildVillage method in SessionController and delete following
                        updateIntersection(validIntersection, PlayerColor.BLUE, villagePieceKind);
                        
                        aMode = SessionScreenModes.CHOOSEACTIONMODE;
                    }
                }
            }
            if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                buildSettlementButton.setText("Build Settlement");
                buildCityButton.setText("Build City");
                validIntersections.clear();
            }
        }
        
        
        // Display the board hexes and game pieces and dice number tokens
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
        for (Hex hex : aSessionController.getHexes()) {
            Integer prob = GameRules.getGameRulesInstance().getDiceNumber(hex);
            if (prob != 0 && prob != null) {
                float xPos =  (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX - 7));
                float yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY - 5));
                CatanGame.skin.getFont("default").draw(polyBatch, prob.toString(), xPos, yPos);
            }
        }
        polyBatch.draw(robber, boardOrigin.getLeft(), boardOrigin.getRight());
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
                0, 1, 4,         // Sets up triangulatio          n according to vertices above
                0, 4, 5,
                1, 2, 3,
                1, 3, 4
        });

        boardHexes.add(polyReg);
    }

    /**
     * Places Robber on given coordinates
     * 
     * @param xCor left coordinate
     * @param yCor right coordinate
     * */
    private void placeRobber(int xCor, int yCor) {
        
        float xPos = xCor * OFFX;
        float yPos = - yCor * OFFY;
        
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aRobberTextureSolid),
                new float[]{     
                        (float) (xPos - BASE * 0.6), (float) (yPos - BASE * 0.5),     // Vertex 0            2
                        (float) (xPos + BASE * 0.6), yPos - BASE / 2,                 // Vertex 1                   
                        xPos, (float) (yPos + BASE * 0.8),                            // Vertex 2         0     1

                }, new short[]{
                0, 1, 2         // Sets up triangulation according to vertices above
        });
        
        robber = polyReg;
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
        
        float xPos = (xCor * BASE);
        float yPos = - (yCor * LENGTH / 2);


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

        float xPos = + (xCor * BASE);
        float yPos = - (yCor * LENGTH / 2);


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

        float xPos = (xCor * BASE);
        float yPos = - (yCor * LENGTH / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      
                        (float) (xPos - PIECEBASE / 2.0), (float) (yPos - PIECEBASE / 2.0),        // Vertex 0
                        (float) (xPos/* + PIECEBASE / 2.0*/), (float) (yPos - PIECEBASE / 2.0),    // Vertex 1
                        (float) (xPos), (float) (yPos + PIECEBASE / 10.0),                         // Vertex 2
                        (float) (xPos + PIECEBASE / 2.0), (float) (yPos + PIECEBASE / 10.0),       // Vertex 3        12      7
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
     * Creates a road according to given positions. Assumes the coordinates correspond to adjacent intersections
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

            v0[0] = (float) (((xCorFirst * BASE)) - PIECEBASE / 4.0);
            v0[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v1[0] = (float) (((xCorFirst * BASE)) + PIECEBASE / 4.0);
            v1[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v2[0] = (float) (((xCorFirst * BASE)) + PIECEBASE / 4.0);
            v2[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

            v3[0] = (float) (( + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v3[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

        } else {
            if ((Math.min(xCorFirst, xCorSecond) == xCorFirst && Math.max(yCorFirst, yCorSecond) == yCorFirst) ||
                    (Math.min(xCorFirst, xCorSecond) == xCorSecond && Math.max(yCorFirst, yCorSecond) == yCorSecond)) {

                v0[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 2.0);
                v0[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);

                v1[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 4.0);
                v1[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v2[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 2.0);
                v2[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v3[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 4.0);
                v3[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            } else {

                v0[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 4.0);
                v0[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

                v1[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 2.0);
                v1[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v2[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 4.0);
                v2[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v3[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 2.0);
                v3[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);

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
     * Creates a ship according to given positions. Assumes the coordinates correspond to adjacent intersections
     *
     * @param xCorFirst  x coordinate of game piece first endpoint
     * @param yCorFirst  y coordinate of game piece first endpoint
     * @param xCorSecond x coordinate of game piece second endpoint
     * @param yCorSecond y coordinate of game piece second endpoint
     * @param color      of game piece
     */
    private void createShip(int xCorFirst, int yCorFirst, int xCorSecond, int yCorSecond, PlayerColor color) {
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

        float[] v0 = new float[2], v1 = new float[2], v2 = new float[2],
                v3 = new float[2], v4 = new float[2], vm = new float[2],
                v5 = new float[2], v6 = new float[2];
        
        int xMin = Math.min(xCorFirst, xCorSecond);
        int xMax = Math.max(xCorFirst, xCorSecond);
        int yMin = Math.min(yCorFirst, yCorSecond);
        int yMax = Math.max(yCorFirst, yCorSecond);
        
        // Sets the identifying coordinates as middle between the two end points
        vm[0] = (float) (xMin + (xMax - xMin) / 2.0);
        vm[1] = (float) (yMin + (yMax - yMin) / 2.0);
        
        // Determines which direction the EdgeUnit will be facing, and gives appropriate vertex values
        if (xCorFirst == xCorSecond) {

            v0[0] = (float) (( + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v0[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v1[0] = (float) (( + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v1[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

            v2[0] = (float) (( + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v2[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

            v3[0] = (float) (( + (xCorFirst * BASE)) - PIECEBASE / 4.0);
            v3[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);
            
            v4[0] = (float) ( + (vm[0] * BASE + PIECEBASE * 1.1)); 
            v4[1] = (float) ( - (vm[1] * (LENGTH / 2)));
            
            v5[0] = (float) (( + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v5[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE);
            
            v6[0] = (float) (( + (xCorFirst * BASE)) + PIECEBASE / 4.0);
            v6[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE * 0.8);

        } else {
            if ((Math.min(xCorFirst, xCorSecond) == xCorFirst && Math.max(yCorFirst, yCorSecond) == yCorFirst) ||
                    (Math.min(xCorFirst, xCorSecond) == xCorSecond && Math.max(yCorFirst, yCorSecond) == yCorSecond)) {

                v0[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 2.0);
                v0[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);

                v1[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 4.0);
                v1[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v2[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 2.0);
                v2[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v3[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 4.0);
                v3[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);
                
                v4[0] = (float) ( + (vm[0] * BASE - PIECEBASE / 3.0)); 
                v4[1] = (float) ( - (vm[1] * (LENGTH / 2) - PIECEBASE));
                
                v5[0] = (float) (( + (xMin * BASE)) + PIECEBASE * 0.5);
                v5[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE * 0.4);
                
                v6[0] = (float) (( + (xMax * BASE)) - PIECEBASE * 0.8);
                v6[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE * 0.5);

            } else {

                v0[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 4.0);
                v0[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 2.0);

                v1[0] = (float) (( + (xMin * BASE)) + PIECEBASE / 2.0);
                v1[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE / 15.0);

                v2[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 4.0);
                v2[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 2.0);

                v3[0] = (float) (( + (xMax * BASE)) - PIECEBASE / 2.0);
                v3[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE / 15.0);
                
                v4[0] = (float) ( + (vm[0] * BASE + PIECEBASE / 3.0)); 
                v4[1] = (float) ( - (vm[1] * (LENGTH / 2) - PIECEBASE));
                
                v5[0] = (float) (( + (xMin * BASE)) + PIECEBASE * 0.8);
                v5[1] = (float) (( - (yMin * LENGTH / 2)) - PIECEBASE * 0.5);
                
                v6[0] = (float) (( + (xMax * BASE)) - PIECEBASE * 0.5);
                v6[1] = (float) (( - (yMax * LENGTH / 2)) + PIECEBASE * 0.4);
            }
        }

        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[]{  
                        vm[0], vm[1],         // Vertex 0, for identification purposes 
                                             // describes the x y coordinates of the middle of an EdgeUnit
                        v0[0], v0[1],        // Vertex 1              5 
                        v1[0], v1[1],        // Vertex 2            /   \
                        v2[0], v2[1],        // Vertex 3         2 6     7 3     (with rotation)  
                        v3[0], v3[1],        // Vertex 4         1   vm    4
                        v4[0], v4[1],        // Vertex 5
                        v5[0], v5[1],        // Vertex 6
                        v6[0], v6[1]         // Vertex 7
                        

                }, new short[]{
                1, 4, 3,         // Sets up triangulation according to vertices above
                1, 3, 2,
                6, 7, 5
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
        //boardOrigin.setLeft(((int) (width / 2f)) - OFFX * SIZE * 2);
        //boardOrigin.setRight(((int) (height / 2f)) - OFFY * SIZE);
        
        boardOrigin.setLeft(((int) (width / 2f)));
        boardOrigin.setRight(((int) (height / 2f)));
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

            float xPos =(xCor * BASE);
            float yPos = -1 * (yCor * LENGTH / 2);

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
            createShip(xCorFirst, yCorFirst, xCorSecond, yCorSecond, color);
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
        placeRobber(position.getLeftCoordinate(), position.getRightCoordinate());
    }

    /**
     *
     */
    public void showDice(int yellowDice, int redDice) {

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

    public void updateResourceBar(ResourceMap updates) {
        for (Map.Entry<ResourceKind, Integer> entry : updates.entrySet()) {
            String resourceName = entry.getKey().toString().toLowerCase();
            Label l = resourceLabelMap.get(resourceName);
            int prev = Integer.valueOf(l.getText().toString());
            int newValue = prev + entry.getValue();
            l.setText(newValue + "");
        }
    }
}

