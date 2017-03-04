package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
import com.mygdx.catan.enums.*;
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
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis
    private final int PIECEBASE = (int) (LENGTH * 0.4);

    /** The batch onto which the game piece will be drawn */
    private PolygonSpriteBatch polyBatch; // To assign at the beginning
    
    private Texture aRobberTextureSolid;

    private SessionController aSessionController;

    private Stage aSessionStage;

    /** The list of polygons representing the board hexes */
    private List<PolygonRegion> boardHexes;
    
    /** The list of polygons representing the board harbours */
    private List<PolygonRegion> boardHarbours;

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
    private boolean initializing;
    
    /** The Lists of valid building intersections. Is empty if aMode != CHOOSEINTERSECTIONMODE || != CHOSEEDGEMODE*/
    private ArrayList<CoordinatePair> validIntersections = new ArrayList<>();
    private ArrayList<Pair<CoordinatePair,CoordinatePair>> validEdges = new ArrayList<>();
    
    /** determines which kind of game piece is being built. If aMode is not in choose X mode, the values do not matter */
    private VillageKind villagePieceKind;
    private EdgeUnitKind edgePieceKind;

    private CoordinatePair initSettlementIntersection;
    private CoordinatePair initEdgePos1;
    private CoordinatePair initEdgePos2;


    /** Menu Buttons */
    private TextButton buildSettlementButton;
    private TextButton buildCityButton;
    private TextButton buildRoadButton;
    private TextButton buildShipButton;
    
    private TextButton aInitButton;

    /**
     * Input adapter that handles general clicks to the screen that are not on buttons or
     * any other of the stage actors. Used for choosing village positions, road positions, etc.
     */
    private InputAdapter inputAdapter;

    private GamePieces gamePieces;
    
    public SessionScreen(CatanGame pGame) {
        aGame = pGame;
        aSessionController = new SessionController(this);
        boardHexes = new ArrayList<>();
        boardHarbours = new ArrayList<>();
        villages = new ArrayList<>();
        edgeUnits = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        resourceLabelMap = new HashMap<>();
        polyBatch = new PolygonSpriteBatch();
        gamePieces = new GamePieces();
        initializing = false;
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // sets the initializing mode of the session screen (VIEWMODE). set to CHOOSEACTIONMODE for testing purposes 
        aMode = SessionScreenModes.CHOOSEACTIONMODE;

        inputAdapter = new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE) {
                    for (CoordinatePair validIntersection : validIntersections) {
                        if (screenX > boardOrigin.getLeft() + validIntersection.getLeft() * BASE - 10 &&
                                screenX < boardOrigin.getLeft() + validIntersection.getLeft() * BASE + 10 &&
                                screenY > boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) - 10 &&
                                screenY < boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) + 10) {

                            if (initializing) {
                                aMode = SessionScreenModes.CHOOSEEDGEMODE;
                                //TODO: show a transparent version of settlement on validIntersection 
                                for (CoordinatePair i : aSessionController.getIntersectionsAndEdges()) {
                                    if (aSessionController.isAdjacent(validIntersection, i) && !i.isOccupied()) {
                                        validEdges.add(new MutablePair<>(i,validIntersection));
                                    }
                                }
                                initSettlementIntersection = validIntersection;
                            } else {
                                aMode = SessionScreenModes.CHOOSEACTIONMODE;
                                
                                //TODO: call buildVillage method in SessionController and delete following
                                updateIntersection(validIntersection, PlayerColor.BLUE, villagePieceKind);
                            }
                            
                            buildSettlementButton.setText("Build Settlement");
                            buildCityButton.setText("Build City");
                            validIntersections.clear();

                            return true;
                        }
                    }
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {
                    for (Pair<CoordinatePair,CoordinatePair> validEdge : validEdges) {
                        int xMin = Math.min(validEdge.getLeft().getLeft(), validEdge.getRight().getLeft());
                        int xMax = Math.max(validEdge.getLeft().getLeft(), validEdge.getRight().getLeft());
                        int yMin = Math.min(validEdge.getLeft().getRight(), validEdge.getRight().getRight());
                        int yMax = Math.max(validEdge.getLeft().getRight(), validEdge.getRight().getRight());
                        
                        if (screenX > boardOrigin.getLeft() + (float) (xMin + (xMax - xMin) / 2.0) * BASE - 10 &&
                                screenX < boardOrigin.getLeft() + (float) (xMin + (xMax - xMin) / 2.0) * BASE + 10 &&
                                screenY > boardOrigin.getRight() + (float) (yMin + (yMax - yMin) / 2.0) * (LENGTH / 2) - 10 &&
                                screenY < boardOrigin.getRight() + (yMin + (yMax - yMin) / 2.0) * (LENGTH / 2) + 10) {
                            
                           
                            
                            if (initializing) {
                                aMode = SessionScreenModes.VIEWMODE;
                                if (!aSessionController.isOnLand(validEdge.getLeft(), validEdge.getRight())) {
                                    edgePieceKind = EdgeUnitKind.SHIP;
                                }
                                initEdgePos1 = validEdge.getLeft();
                                initEdgePos2 = validEdge.getRight();
                                //TODO: call placeCityAndRoad method in SessionController and delete following
                                updateIntersection(initSettlementIntersection, PlayerColor.ORANGE, villagePieceKind);
                                updateEdge(initEdgePos1, initEdgePos2, edgePieceKind, PlayerColor.ORANGE);
                                
                                initializing = false;
                                aInitButton.setText("Done");
                            } else {
                                //TODO: call buildEdgePiece method in SessionController and delete following
                                updateEdge(validEdge.getLeft(), validEdge.getRight(), edgePieceKind, PlayerColor.WHITE);
                                
                                aMode = SessionScreenModes.CHOOSEACTIONMODE;
                            }
                            
                            buildRoadButton.setText("Build Road");
                            buildShipButton.setText("Build Ship");
                            
                            validEdges.clear();
                            
                            return true;
                        }        
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void show() {
        aSessionStage = new Stage();

        // Combine input (click handling) from the InputAdapter and the Stage
        final InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(aSessionStage);
        inputMultiplexer.addProcessor(inputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // resource table
        Table contentTable = new Table(CatanGame.skin);
        contentTable.setBackground("resTableBackground");
        contentTable.setSize(550, 120);
        contentTable.setPosition(350, 10);

        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            Table aTable = createResourceTable(entry.getKey());
            contentTable.add(aTable).pad(5);
        }
        
        // menu table
        Table menuTable = new Table(CatanGame.skin);
        menuTable.setBackground("resTableBackground");
        menuTable.setSize(200, 300);
        menuTable.setPosition(10, 10);
        
        // creates the menu buttons TODO:remainder of buttons
        buildSettlementButton = new TextButton("Build Settlement",CatanGame.skin);
        setupBuildVillageButton(buildSettlementButton, VillageKind.SETTLEMENT);
        buildSettlementButton.pad(0, 10, 0, 10);
        menuTable.add(buildSettlementButton).padBottom(10).row();
        
        buildCityButton = new TextButton("Build City",CatanGame.skin);
        setupBuildVillageButton(buildCityButton, VillageKind.CITY);
        buildCityButton.pad(0, 10, 0, 10);
        menuTable.add(buildCityButton).padBottom(10).row();
        
        buildRoadButton = new TextButton("Build Road", CatanGame.skin);
        setupBuildEdgeUnitButton(buildRoadButton, EdgeUnitKind.ROAD);
        buildRoadButton.pad(0, 10, 0, 10);
        menuTable.add(buildRoadButton).padBottom(10).row();
        
        buildShipButton = new TextButton("Build Ship", CatanGame.skin);
        setupBuildEdgeUnitButton(buildShipButton, EdgeUnitKind.SHIP);
        buildShipButton.pad(0, 10, 0, 10);
        menuTable.add(buildShipButton).padBottom(10).row();
        
        //TODO: DELETE FOLLOWING TEST BUTTON
        aInitButton = new TextButton("Init",CatanGame.skin);
        setupInitButton(aInitButton);
        menuTable.add(aInitButton);
        
        //Creating the color filling for the robber piece
        aRobberTextureSolid = setupTextureSolid(Color.valueOf("666666"));

        // sets center of board
        int offsetX, offsetY;

        // creates hexes of the board
        for (Hex hex : aSessionController.getHexes()) {
            offsetX = hex.getLeftCoordinate();
            offsetY = hex.getRightCoordinate();
            boardHexes.add(gamePieces.createHexagon((offsetX * OFFX), -(offsetY * OFFY), LENGTH, BASE, hex.getKind()));
        }
        
        // creates harbours of the board
        for (CoordinatePair intersection : aSessionController.getIntersectionsAndEdges()) {
            if (intersection.getHarbourKind() != HarbourKind.NONE) {
                offsetX = intersection.getLeft();
                offsetY = intersection.getRight();
                boardHarbours.add(gamePieces.createHarbour(offsetX, offsetY, BASE, LENGTH, intersection.getHarbourKind()));
            }
        }
        
        // places robber at initial robber position
        Hex robberPos = aSessionController.getRobberPosition();
        if (robberPos != null) {
            updateRobberPosition(robberPos);
        }

        aSessionStage.addActor(contentTable);
        aSessionStage.addActor(menuTable);
    }

    
    private void setupInitButton(TextButton initButton) {
        initButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                initButton.setChecked(false);
                initButton.setText("Initializing");
                initialize(false);
            }
        });
    }
    
    private void setupBuildVillageButton(TextButton buildButton, VillageKind kind) {
        buildButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buildButton.setChecked(false);
             // TODO: ask SessionController if there are enough resources
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    // make the following loop go through requested valid build positions
                    for (CoordinatePair intersections : aSessionController.getIntersectionsAndEdges()) {
                        validIntersections.add(intersections);
                        //TODO make a sprite that highlights the area
                    }
                    aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
                    villagePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE && !initializing) {
                    validIntersections.clear();
                    buildSettlementButton.setText("Build Settlement");
                    buildCityButton.setText("Build City");
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                }
            }
        });
    }
    
    private void setupBuildEdgeUnitButton(TextButton buildButton, EdgeUnitKind kind) {
        buildButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buildButton.setChecked(false);
             // TODO: ask SessionController if there are enough resources
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    // make the following loop go through requested valid build positions
                    for (CoordinatePair i : aSessionController.getIntersectionsAndEdges()) {
                        
                        for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                            if (kind == EdgeUnitKind.ROAD){
                                if (aSessionController.isAdjacent(i, j) && aSessionController.isOnLand(i,j)) {
                                    
                                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<CoordinatePair, CoordinatePair>(i,j);
                                    validEdges.add(edge);
                                    
                                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                                            validEdges.remove(edge);
                                        }
                                    }
                                }
                            } else {
                                if (aSessionController.isAdjacent(i, j) && !aSessionController.isOnLand(i,j)) {
                                    
                                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<CoordinatePair, CoordinatePair>(i,j);
                                    validEdges.add(edge);
                                    
                                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                                            validEdges.remove(edge);
                                        }
                                    }
                                }
                            }
                        }
                        
                        //TODO make a sprite that highlights the areas
                    }
                    aMode = SessionScreenModes.CHOOSEEDGEMODE;
                    edgePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE && !initializing) {
                    validEdges.clear();
                    buildRoadButton.setText("Build Road");
                    buildShipButton.setText("Build Ship");
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
        // Set the background color
        Gdx.gl.glClearColor(0.24706f, 0.24706f, 0.24706f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Display the board hexes + harbours and game pieces and dice number tokens
        polyBatch.begin();
        for (PolygonRegion boardHex : boardHexes) {
            polyBatch.draw(boardHex, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        for (PolygonRegion harbour : boardHarbours) {
            polyBatch.draw(harbour, boardOrigin.getLeft(), boardOrigin.getRight());
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
        aSessionStage.dispose();
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
    public void updateIntersection(CoordinatePair position, PlayerColor color, VillageKind kind) {

        int offsetX = position.getLeft();
        int offsetY = position.getRight();

        // Removes village on given coordinate
        removeVillage(offsetX, offsetY);

        PolygonRegion village = null;
        switch (kind) {
            case CITY:
                village = gamePieces.createCity(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            case SCIENCEMETROPOLE:
                village = gamePieces.createMetropolis(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            case SETTLEMENT:
                village = gamePieces.createSettlement(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            case TRADEMETROPLE:
                village = gamePieces.createMetropolis(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            default:
                break;
        }
        if (village != null)
            villages.add(village);
    }

    /**
     * renders the road with appropriate color and position. If edge is already occupied, it removes current edge piece and replaces it with this one
     *
     * @param firstCoordinate  end point of edge
     * @param secondCoordinate other end point of edge
     * @param kind             of new edge unit (SHIP or ROAD)
     * @param color            of player who owns the new edge unit
     */
    public void updateEdge(CoordinatePair firstCoordinate, CoordinatePair secondCoordinate, EdgeUnitKind kind, PlayerColor color) {
        int xCorFirst = firstCoordinate.getLeft();
        int yCorFirst = firstCoordinate.getRight();
        int xCorSecond = secondCoordinate.getLeft();
        int yCorSecond = secondCoordinate.getRight();
        
        // removes edge on given coordinate
        removeEdgeUnit(xCorFirst, yCorFirst, xCorSecond, yCorSecond);

        PolygonRegion edgeUnit = null;
        switch (kind) {
            case ROAD:
                edgeUnit = gamePieces.createRoad(xCorFirst, yCorFirst, xCorSecond, yCorSecond, BASE, LENGTH, PIECEBASE, color);
                break;
            case SHIP:
                edgeUnit = gamePieces.createShip(xCorFirst, yCorFirst, xCorSecond, yCorSecond, BASE, LENGTH, PIECEBASE, color);
                break;
            default:
                break;
        }
        if (edgeUnit != null)
            edgeUnits.add(edgeUnit);
    }

    /**
     * triggers initialization mode. All other actions are blocked until an intersection and an edge has been chosen
     * */
    public void initialize(boolean firstInit) {
        aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
        initializing = true;
        for (CoordinatePair i : aSessionController.requestValidInitializationBuildIntersections()) {
            validIntersections.add(i);
        }
        if (firstInit) {villagePieceKind = VillageKind.SETTLEMENT;}
        else {villagePieceKind = VillageKind.CITY;}
        edgePieceKind = EdgeUnitKind.ROAD;
    }
    
    /**
     * unlocks menu build bar. IMPORTANT: DO NOT GIVE TURN AT INITIALIZATION
     * */
    public void giveTurn() {
        aMode = SessionScreenModes.CHOOSEACTIONMODE;
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

    public CoordinatePair getInitSettlementIntersection() {
        return initSettlementIntersection;
    }

    public CoordinatePair getInitEdgePos1() {
        return initEdgePos1;
    }

    public CoordinatePair getInitEdgePos2() {
        return  initEdgePos2;
    }


}
