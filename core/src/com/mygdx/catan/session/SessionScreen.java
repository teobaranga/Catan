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
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.*;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.ui.TradeWindow;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class SessionScreen implements Screen {

    /**
     * The map of resources to colors
     */
    private static Map<ResourceKind, Color> colorMap;

    static {
        // TODO move this to the skin
        colorMap = new HashMap<>();
        colorMap.put(ResourceKind.WOOD, Color.valueOf("679861"));
        colorMap.put(ResourceKind.BRICK, Color.valueOf("CC6633"));
        colorMap.put(ResourceKind.ORE, Color.valueOf("996633"));
        colorMap.put(ResourceKind.GRAIN, Color.valueOf("FFFF66"));
        colorMap.put(ResourceKind.WOOL, Color.valueOf("66FF66"));
        colorMap.put(ResourceKind.COIN, Color.valueOf("FF9A00"));
        colorMap.put(ResourceKind.CLOTH, Color.valueOf("CDCDFF"));
        colorMap.put(ResourceKind.PAPER, Color.valueOf("E6E6B9"));
    }

    private final CatanGame aGame;

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int LENGTH = 40;                                            // length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;                                            // offset on the x axis
    private final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis
    private final int PIECEBASE = (int) (LENGTH * 0.4);

    /**
     * The batch onto which the game piece will be drawn
     */
    private PolygonSpriteBatch polyBatch; // To assign at the beginning
    private PolygonSpriteBatch highlightBatch; // will have blending enabled

    private Texture aRobberTextureSolid;

    private SessionController aSessionController;

    private Stage aSessionStage;

    /**
     * The list of polygons representing the board hexes
     */
    private List<PolygonSprite> boardHexes;

    /**
     * The list of polygons representing the board harbours
     */
    private List<PolygonRegion> boardHarbours;

    /**
     * The List of villages currently on the board
     */
    private List<PolygonRegion> villages;

    /**
     * The List of EdgeUnits currently on the board
     */
    private List<PolygonRegion> edgeUnits;

    /**
     * The List of valid building regions on the board
     */
    private List<PolygonRegion> highlightedPositions;

    /**
     * The Robber
     */
    private PolygonRegion robber;

    /**
     * The origin of the the hex board
     */
    private MutablePair<Integer, Integer> boardOrigin;

    /**
     * The map of resource tables
     */
    private EnumMap<ResourceKind, Label> resourceLabelMap;

    /**
     * determines the current mode of the session screen
     */
    private SessionScreenModes aMode;
    private boolean initializing;

    /**
     * The Lists of valid building intersections. Is empty if aMode != CHOOSEINTERSECTIONMODE || != CHOSEEDGEMODE
     */
    private ArrayList<CoordinatePair> validIntersections = new ArrayList<>();
    private ArrayList<Pair<CoordinatePair, CoordinatePair>> validEdges = new ArrayList<>();

    /**
     * determines which kind of game piece is being built. If aMode is not in choose X mode, the values do not matter
     */
    private VillageKind villagePieceKind;
    private EdgeUnitKind edgePieceKind;
    private CoordinatePair initVillageIntersection;

    /**
     * Menu Buttons
     */
    private TextButton buildSettlementButton;
    private TextButton buildCityButton;
    private TextButton buildRoadButton;
    private TextButton buildShipButton;
    private TextButton rollDiceButton;
    private TextButton maritimeTradeButton;

    private TextButton aInitButton;

    /**
     * A table that keeps track of game messages, mostly used for debugging
     */
    private ScrollPane gameLog;

    /**
     * A table that keeps track of current player
     */
    private Table currentPlayer;
    private Label currentPlayerLabel;

    /**
     * labels that keeps track of available game pieces to build
     */
    private Label availableSettlements;
    private Label availableCities;
    private Label availableRoads;
    private Label availableShips;

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
        highlightedPositions = new ArrayList<>();
        boardOrigin = new MutablePair<>();
        resourceLabelMap = new EnumMap<>(ResourceKind.class);
        polyBatch = new PolygonSpriteBatch();
        highlightBatch = new PolygonSpriteBatch();
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

                            highlightedPositions.clear();

                            if (initializing) {
                                aMode = SessionScreenModes.CHOOSEEDGEMODE;
                                // show a transparent version of settlement on validIntersection 
                                if (villagePieceKind == VillageKind.SETTLEMENT) {
                                    highlightedPositions.add(gamePieces.createSettlement(validIntersection.getLeft(), validIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                                } else {
                                    highlightedPositions.add(gamePieces.createCity(validIntersection.getLeft(), validIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                                }
                                // finds all valid adjacent edges
                                for (CoordinatePair i : aSessionController.getIntersectionsAndEdges()) {
                                    if (aSessionController.isAdjacent(validIntersection, i) && !i.isOccupied()) {
                                        validEdges.add(new MutablePair<>(i, validIntersection));
                                        // show a transparent version of valid adjacent roads
                                        if (aSessionController.isOnLand(i, validIntersection)) {
                                            highlightedPositions.add(gamePieces.createRoad(i.getLeft(), i.getRight(), validIntersection.getLeft(), validIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                                        } else {
                                            highlightedPositions.add(gamePieces.createShip(i.getLeft(), i.getRight(), validIntersection.getLeft(), validIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                                        }

                                    }
                                }
                                initVillageIntersection = validIntersection;
                            } else {
                                aMode = SessionScreenModes.CHOOSEACTIONMODE;
                                aSessionController.buildVillage(validIntersection, villagePieceKind, aSessionController.getPlayerColor(), false, initializing);
                            }

                            buildSettlementButton.setText("Build Settlement");
                            buildCityButton.setText("Build City");
                            validIntersections.clear();

                            return true;
                        }
                    }
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {
                    for (Pair<CoordinatePair, CoordinatePair> validEdge : validEdges) {
                        int xMin = Math.min(validEdge.getLeft().getLeft(), validEdge.getRight().getLeft());
                        int xMax = Math.max(validEdge.getLeft().getLeft(), validEdge.getRight().getLeft());
                        int yMin = Math.min(validEdge.getLeft().getRight(), validEdge.getRight().getRight());
                        int yMax = Math.max(validEdge.getLeft().getRight(), validEdge.getRight().getRight());

                        if (screenX > boardOrigin.getLeft() + (float) (xMin + (xMax - xMin) / 2.0) * BASE - 10 &&
                                screenX < boardOrigin.getLeft() + (float) (xMin + (xMax - xMin) / 2.0) * BASE + 10 &&
                                screenY > boardOrigin.getRight() + (float) (yMin + (yMax - yMin) / 2.0) * (LENGTH / 2) - 10 &&
                                screenY < boardOrigin.getRight() + (yMin + (yMax - yMin) / 2.0) * (LENGTH / 2) + 10) {

                            highlightedPositions.clear();

                            if (initializing) {
                                //TODO set to VIEW mode
                                aMode = SessionScreenModes.CHOOSEACTIONMODE;
                                if (!aSessionController.isOnLand(validEdge.getLeft(), validEdge.getRight())) {
                                    edgePieceKind = EdgeUnitKind.SHIP;
                                }

                                if (villagePieceKind == VillageKind.CITY) {
                                    aSessionController.buildVillage(initVillageIntersection, VillageKind.SETTLEMENT, aSessionController.getPlayerColor(), false, initializing);
                                    aSessionController.buildVillage(initVillageIntersection, villagePieceKind, aSessionController.getPlayerColor(), false, initializing);
                                    aSessionController.distributeInitialResources(initVillageIntersection);
                                } else {
                                    aSessionController.buildVillage(initVillageIntersection, villagePieceKind, aSessionController.getPlayerColor(), false, initializing);
                                }

                                aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), validEdge.getLeft(), validEdge.getRight(), edgePieceKind, false, initializing);

                                initializing = false;
                                aInitButton.setText("Done");
                            } else {
                                aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), validEdge.getLeft(), validEdge.getRight(), edgePieceKind, false, initializing);
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

        for (Map.Entry<ResourceKind, Color> entry : colorMap.entrySet()) {
            Table aTable = createResourceTable(entry.getKey());
            contentTable.add(aTable).pad(5);
        }

        // menu table
        Table menuTable = new Table(CatanGame.skin);
        menuTable.setBackground("resTableBackground");
        menuTable.setSize(200, 300);
        menuTable.setPosition(10, 10);

        // current player table
        currentPlayer = new Table(CatanGame.skin);
        currentPlayerLabel = new Label("", CatanGame.skin);
        currentPlayer.add(currentPlayerLabel);
        currentPlayer.setSize(200, 50);
        currentPlayer.setPosition(10, Gdx.graphics.getHeight() - 60);
        updateCurrentPlayer(aSessionController.getCurrentPlayer());

        // available game pieces table
        Table availableGamePiecesTable = new Table(CatanGame.skin);
        availableGamePiecesTable.setBackground("resTableBackground");
        availableGamePiecesTable.setSize(200, 200);
        availableGamePiecesTable.setPosition(Gdx.graphics.getWidth() - 210, 10);

        Table aAvailSettlementTable = new Table(CatanGame.skin);
        availableSettlements = new Label("", CatanGame.skin);
        aAvailSettlementTable.add(availableSettlements);
        availableGamePiecesTable.add(aAvailSettlementTable).pad(5).row();

        Table aAvailCityTable = new Table(CatanGame.skin);
        availableCities = new Label("", CatanGame.skin);
        aAvailCityTable.add(availableCities);
        availableGamePiecesTable.add(aAvailCityTable).pad(5).row();

        Table aAvailRoadTable = new Table(CatanGame.skin);
        availableRoads = new Label("", CatanGame.skin);
        aAvailRoadTable.add(availableRoads);
        availableGamePiecesTable.add(aAvailRoadTable).pad(5).row();

        Table aAvailShipTable = new Table(CatanGame.skin);
        availableShips = new Label("", CatanGame.skin);
        aAvailShipTable.add(availableShips);
        availableGamePiecesTable.add(aAvailShipTable).pad(5).row();

        updateAvailableGamePieces(5, 4, 15, 15);


        // creates the menu buttons
        buildSettlementButton = new TextButton("Build Settlement", CatanGame.skin);
        setupBuildVillageButton(buildSettlementButton, VillageKind.SETTLEMENT);
        buildSettlementButton.pad(0, 10, 0, 10);
        menuTable.add(buildSettlementButton).padBottom(10).row();

        buildCityButton = new TextButton("Build City", CatanGame.skin);
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

        //TODO: DELETE FOLLOWING TWO TEST BUTTONS
        aInitButton = new TextButton("Init", CatanGame.skin);
        setupInitButton(aInitButton);
        menuTable.add(aInitButton).padBottom(10).row();

        // Add roll dice button
        rollDiceButton = new TextButton("Roll Dice", CatanGame.skin);
        rollDiceButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aSessionController.rollDice();
            }
        });
        menuTable.add(rollDiceButton).padBottom(10).row();


        // Add maritime trade button
        maritimeTradeButton = new TextButton("Maritime Trade", CatanGame.skin);
        maritimeTradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final ResourceMap tradeRatios = new ResourceMap();
                aSessionController.getRatios(tradeRatios);
                tradeRatios.put(ResourceKind.ORE, 4);
                // TODO fill the trade ratios according to the highest harbour levels
                final TradeWindow tradeWindow = new TradeWindow("Maritime Trade", tradeRatios, CatanGame.skin, (offer, request, tradeRatio) -> {
                    final ResourceMap resourceMap = new ResourceMap();
                    resourceMap.put(request, 1);
                    updateResourceBar(resourceMap);
                });
                aSessionStage.addActor(tradeWindow);
            }
        });
        menuTable.add(maritimeTradeButton);

        //Creating the color filling for the robber piece
        aRobberTextureSolid = setupTextureSolid(Color.valueOf("666666"));

        // sets center of board
        int offsetX, offsetY;

        // creates hexes of the board
        for (Hex hex : aSessionController.getHexes()) {
            offsetX = hex.getLeftCoordinate();
            offsetY = hex.getRightCoordinate();
            boardHexes.add(gamePieces.createHexagon((offsetX * OFFX), -(offsetY * OFFY), boardOrigin.getLeft(), boardOrigin.getRight(), hex.getKind()));
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

        // Create the Game Log table
        float pad = 10f;
        float tableWidth = 350f;
        float tableHeight = 300f;
        final Table table = new Table(CatanGame.skin);
        table.left().top();
        gameLog = new ScrollPane(table);
        gameLog.setOverscroll(false, false);
        gameLog.setX(Gdx.graphics.getWidth() - tableWidth - pad);
        gameLog.setY(pad);
        gameLog.setWidth(tableWidth);
        gameLog.setHeight(tableHeight);

        aSessionStage.addActor(contentTable);
        aSessionStage.addActor(availableGamePiecesTable);
        aSessionStage.addActor(menuTable);
        aSessionStage.addActor(currentPlayer);
        aSessionStage.addActor(gameLog);

        // Notify the controller that the session screen is displayed
        aSessionController.onScreenShown();

        // Begin the game
        aSessionController.checkIfMyTurn();
    }

    private void setupInitButton(TextButton initButton) {
        initButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                initButton.setChecked(false);
                initButton.setText("Initializing");
                initialize(false);
            }
        });
    }

    private void setupBuildVillageButton(TextButton buildButton, VillageKind kind) {
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildButton.setChecked(false);
                // TODO: ask SessionController if there are enough resources
                if (!aSessionController.requestBuildVillage(aSessionController.getPlayerColor(), kind)) {
                    addGameMessage("Not enough resources for building the " + kind.name());
                    return;
                }
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    if (kind == VillageKind.SETTLEMENT) {
                        for (CoordinatePair intersections : aSessionController.requestValidBuildIntersections(aSessionController.getPlayerColor())) {
                            validIntersections.add(intersections);
                            highlightedPositions.add(gamePieces.createSettlement(intersections.getLeft(), intersections.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        }
                    } else {
                        for (CoordinatePair intersections : aSessionController.requestValidCityUpgradeIntersections(aSessionController.getPlayerColor())) {
                            validIntersections.add(intersections);
                            highlightedPositions.add(gamePieces.createCity(intersections.getLeft(), intersections.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        }
                    }

                    aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
                    villagePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE && !initializing) {
                    validIntersections.clear();
                    highlightedPositions.clear();
                    buildSettlementButton.setText("Build Settlement");
                    buildCityButton.setText("Build City");
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                }
            }
        });
    }

    private void setupBuildEdgeUnitButton(TextButton buildButton, EdgeUnitKind kind) {
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildButton.setChecked(false);
                // TODO: ask SessionController if there are enough resources
                if(!aSessionController.requestBuildEdgeUnit(aSessionController.getPlayerColor(), kind)) {
                    addGameMessage("Not enough resources for building the " + kind.name());
                    return;
                }
                if (aMode == SessionScreenModes.CHOOSEACTIONMODE) {
                    // the following loop go through requested valid build positions
                    if (kind == EdgeUnitKind.ROAD) {
                        for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
                            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                                if (aSessionController.isAdjacent(i, j) && aSessionController.isOnLand(i, j)) {

                                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<CoordinatePair, CoordinatePair>(i, j);
                                    validEdges.add(edge);

                                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                                            validEdges.remove(edge);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        for (CoordinatePair i : aSessionController.requestValidShipEndpoints(aSessionController.getPlayerColor())) {
                            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                                if (aSessionController.isAdjacent(i, j) && !aSessionController.isOnLand(i, j)) {

                                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<CoordinatePair, CoordinatePair>(i, j);
                                    validEdges.add(edge);

                                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                                            validEdges.remove(edge);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // make a sprite that highlights the areas
                    if (kind == EdgeUnitKind.ROAD) {
                        for (Pair<CoordinatePair, CoordinatePair> edge : validEdges) {
                            highlightedPositions.add(gamePieces.createRoad(edge.getLeft().getLeft(), edge.getLeft().getRight(), edge.getRight().getLeft(), edge.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        }
                    } else {
                        for (Pair<CoordinatePair, CoordinatePair> edge : validEdges) {
                            highlightedPositions.add(gamePieces.createShip(edge.getLeft().getLeft(), edge.getLeft().getRight(), edge.getRight().getLeft(), edge.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        }
                    }


                    aMode = SessionScreenModes.CHOOSEEDGEMODE;
                    edgePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE && !initializing) {
                    validEdges.clear();
                    highlightedPositions.clear();
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

    private Table createResourceTable(ResourceKind type) {
        Table resourceTable = new Table(CatanGame.skin);
        resourceTable.add(new Label(type.toString().toLowerCase(), CatanGame.skin));
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

        for (PolygonSprite boardHex : boardHexes) {
            boardHex.draw(polyBatch);
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
                float xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX - 7));
                float yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY - 5));
                CatanGame.skin.getFont("default").draw(polyBatch, prob.toString(), xPos, yPos);
            }
        }
        polyBatch.draw(robber, boardOrigin.getLeft(), boardOrigin.getRight());
        polyBatch.end();

        // display highlighted positions
        highlightBatch.begin();
        highlightBatch.enableBlending();
        highlightBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
        for (PolygonRegion highlight : highlightedPositions) {
            highlightBatch.draw(highlight, boardOrigin.getLeft(), boardOrigin.getRight());
        }
        highlightBatch.end();

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
        aSessionController.onScreenHidden();
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
     */
    private void placeRobber(int xCor, int yCor) {

        float xPos = xCor * OFFX;
        float yPos = -yCor * OFFY;

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

            float xPos = (xCor * BASE);
            float yPos = -1 * (yCor * LENGTH / 2);

            if ((float) xV0 == xPos - PIECEBASE / 2.0 && (float) yV0 == yPos - PIECEBASE / 2.0) {
                return pr;
            }
        }

        return null;
    }

    /**
     * @param xCorFirst  left coordinate of first intersection
     * @param yCorFirst  right coordinate of first intersection
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
     * @param xCorFirst  left coordinate of first intersection
     * @param yCorFirst  right coordinate of first intersection
     * @param xCorSecond left coordinate of second intersection
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
            case SCIENCE_METROPOLIS:
                village = gamePieces.createMetropolis(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            case SETTLEMENT:
                village = gamePieces.createSettlement(offsetX, offsetY, BASE, LENGTH, PIECEBASE, color);
                break;
            case TRADE_METROPOLIS:
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
     * Enable or disable GUI elements depending on the game phase.
     *
     * @param phase the session's current phase
     */
    void enablePhase(GamePhase phase) {
        switch (phase) {
            case SETUP_PHASE_ONE:
                // Allow the player only to roll the dice
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                maritimeTradeButton.setDisabled(true);

                rollDiceButton.setDisabled(false);
                break;
            case SETUP_PHASE_TWO_CLOCKWISE:
                // Allow the player only to choose a settlement and a road
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                maritimeTradeButton.setDisabled(true);
                rollDiceButton.setDisabled(true);

                initialize(true);
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                // Allow the player only to choose a city and a road
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                maritimeTradeButton.setDisabled(true);
                rollDiceButton.setDisabled(true);

                initialize(false);
                break;
            case TURN_FIRST_PHASE:
                break;
            case TURN_SECOND_PHASE:
                break;
            case Completed:
                break;
        }
    }

    /**
     * Disable all GUI elements.
     */
    void endTurn() {
        buildSettlementButton.setDisabled(true);
        buildCityButton.setDisabled(true);
        buildRoadButton.setDisabled(true);
        buildShipButton.setDisabled(true);
        rollDiceButton.setDisabled(true);
        maritimeTradeButton.setDisabled(true);
    }

    /**
     * Triggers initialization mode. All other actions are blocked until an intersection and an edge has been chosen.
     *
     * @param firstInit Flag indicating whether it's the
     */
    private void initialize(boolean firstInit) {
        aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
        initializing = true;
        // Highlight the valid positions where building can be placed
        for (CoordinatePair i : aSessionController.requestValidInitializationBuildIntersections()) {
            validIntersections.add(i);
            if (firstInit) {
                highlightedPositions.add(gamePieces.createSettlement(i.getLeft(), i.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            } else {
                highlightedPositions.add(gamePieces.createCity(i.getLeft(), i.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            }
        }
        // Set the type of village that the player is allowed to place
        if (firstInit) {
            villagePieceKind = VillageKind.SETTLEMENT;
        } else {
            villagePieceKind = VillageKind.CITY;
        }
        edgePieceKind = EdgeUnitKind.ROAD;
    }

    /**
     * unlocks menu build bar. IMPORTANT: DO NOT GIVE TURN AT INITIALIZATION
     */
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
     * Updates the current player
     */
    public void updateCurrentPlayer(Player newCurrentPlayer) {

        String currentPlayerName = newCurrentPlayer.getAccount().getUsername();
        currentPlayerLabel.setText("Current Player: " + currentPlayerName);

        // sets background color to current player's color
        switch (newCurrentPlayer.getColor()) {
            case BLUE:
                currentPlayer.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
                break;
            case ORANGE:
                currentPlayer.setBackground(CatanGame.skin.newDrawable("white", Color.ORANGE));
                break;
            case RED:
                currentPlayer.setBackground(CatanGame.skin.newDrawable("white", Color.RED));
                break;
            case WHITE:
                currentPlayer.setBackground(CatanGame.skin.newDrawable("white", Color.WHITE));
                break;
            case YELLOW:
                currentPlayer.setBackground(CatanGame.skin.newDrawable("white", Color.YELLOW));
                break;
            default:
                break;
        }
    }

    /**
     * Updates the available game pieces
     *
     * @param newAvailSettlements number of available settlements
     * @param newAvailCities      number of available cities
     * @param newAvailRoads       number of available roads
     * @param newAvailShips       number of available ships
     */
    public void updateAvailableGamePieces(int newAvailSettlements, int newAvailCities, int newAvailRoads, int newAvailShips) {
        availableSettlements.setText("Available Settlements: " + newAvailSettlements);
        availableCities.setText("Available Cities: " + newAvailCities);
        availableRoads.setText("Available Roads: " + newAvailRoads);
        availableShips.setText("Available Ships: " + newAvailShips);
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
            ResourceKind resourceKind = entry.getKey();
            Label l = resourceLabelMap.get(resourceKind);
            int newValue = entry.getValue();
            l.setText(newValue + "");
        }
    }

    /**
     * Add a message to the game log
     *
     * @param message message
     */
    void addGameMessage(String message) {
        final Table table = (Table) gameLog.getChildren().get(0);
        table.add(new Label(message, CatanGame.skin)).left();
        table.row();
        gameLog.layout();
        gameLog.scrollTo(0, 0, 0, 0);
    }
}
