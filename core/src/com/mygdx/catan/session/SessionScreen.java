package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.FishTokenMap;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.ScreenKind;
import com.mygdx.catan.enums.SessionScreenModes;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.EdgeUnit;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.moves.MultiStepInitMove;
import com.mygdx.catan.moves.MultiStepMove;
import com.mygdx.catan.moves.MultiStepMovingshipMove;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.UpdateOldBoot;
import com.mygdx.catan.ui.ChooseDiceResultWindow;
import com.mygdx.catan.ui.ChooseDraw;
import com.mygdx.catan.ui.ChooseFromEnumCollectionWindow;
import com.mygdx.catan.ui.ChooseMultipleResourcesWindow;
import com.mygdx.catan.ui.ChoosePlayerWindow;
import com.mygdx.catan.ui.ChooseProgressCardKindWindow;
import com.mygdx.catan.ui.DomesticTradeWindow;
import com.mygdx.catan.ui.FishTradeWindow;
import com.mygdx.catan.ui.KnightActor;
import com.mygdx.catan.ui.PlayProgressCardWindow;
import com.mygdx.catan.ui.TradeWindow;
import com.mygdx.catan.ui.WinnerWindow;
import com.mygdx.catan.ui.window.KnightActionsWindow;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SessionScreen implements Screen {

    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    public static final int LENGTH = 40;                                            // length of an edge of a tile

    private final CatanGame aGame;
    public static final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH / 2, 2)); // length of base of equilateral triangles within a tile
    public static final int OFFX = BASE;                                            // offset on the x axis
    public static final int OFFY = LENGTH + LENGTH / 2;                             // offset on the y axis
    public static final int PIECEBASE = (int) (LENGTH * 0.4);

    /** The batch onto which the game piece will be drawn */
    private PolygonSpriteBatch polyBatch; // To assign at the beginning
    private PolygonSpriteBatch highlightBatch; // will have blending enabled

    private SessionController aSessionController;

    private Stage aSessionStage, gamePiecesStage;

    /** The list of polygons representing the board hexes */
    private List<PolygonSprite> boardHexes;

    /** The list of polygons representing the board harbours */
    private List<PolygonRegion> boardHarbours;

    /** The List of villages currently on the board */
    private List<PolygonRegion> villages;

    /** The List of EdgeUnits currently on the board */
    private List<PolygonRegion> edgeUnits;

    /** The list of knights currently on the board */
    private List<KnightActor> knights;

    /** The list of city walls currently on the board */
    private List<Image> cityWalls;

    /** The List of valid building regions on the board */
    private List<PolygonRegion> highlightedPositions;

    /** The Robber */
    private PolygonSprite robberSprite;

    /** The Merchant */
    private PolygonRegion merchantRegion;

    /** The origin of the the hex board */
    private MutablePair<Integer, Integer> boardOrigin;

    /** The map of resource tables */
    private EnumMap<ResourceKind, Label> resourceLabelMap;

    /** Determines the current mode of the session screen */
    private SessionScreenModes aMode;

    /** Contains the currently performing multi step move */
    private MultiStepMove currentlyPerformingMove;

    /**
     * The Lists of valid building intersections. Is empty if aMode != CHOOSEINTERSECTIONMODE || != CHOSEEDGEMODE
     */
    private ArrayList<CoordinatePair> validIntersections;
    private ArrayList<Pair<CoordinatePair, CoordinatePair>> validEdges;
    private ArrayList<Hex> validHexes;

    // Menu Buttons
    private TextButton buildSettlementButton, buildCityButton, buildRoadButton, buildShipButton, buildKnightButton,
            buildCityWallButton, buildMetropolisButton;
    private TextButton rollDiceButton, endTurnButton;
    private TextButton domesticTradeButton, tradeButton;
    private TextButton moveShipButton;
    private TextButton playProgressCardButton;
    
    private TextButton improveTrade, improvePolitics, improveScience;

    /** A table that keeps track of game messages, mostly used for debugging */
    private ScrollPane gameLog;

    /**
     * A table that keeps track of current player
     */
    private Table currentPlayer;
    private Label currentPlayerLabel;

    /** A table that will contain all the progress card images */
    private Table progressCardTable;

    /** A table that contains all temporary functionality */
    private Table temporaryFunctionalityTable;

    /**
     * Three tables that keep track of the players' VPs.
     */
    private Table[] playersVP;
    private Label[] playersVPLabel;

    /** Label displaying the current barbarian position */
    private Label barbarianPositionLabel;

    /**
     * Fish Count table
     */
    private Table oneFishCount;
    private Table twoFishCount;
    private Table threeFishCount;
    private Label oneFishCountLabel;
    private Label twoFishCountLabel;
    private Label threeFishCountLabel;

    /**
     * Fish Button
     */
    private Button tradeFish;

    /**
     * Development Flip Chart Table
     */
    private Table tradeImprovement;
    private Table politicsImprovement;
    private Table scienceImprovement;
    private Label tradeImprovementLevel;
    private Label politicsImprovementLevel;
    private Label scienceImprovementLevel;
    private Label tradeImprovementLabel;
    private Label politicsImprovementLabel;
    private Label scienceImprovementLabel;

    /**
     * Longest road owner table
     * */
    private Table longestRoad;
    private Label longestRoadOwner;
    
    /**
     * Table to show the Old Boot Owner.
     */
    private Table bootOwner;
    private Label bootOwnerLabel;
    private Button giveBoot;

    /**
     * labels that keeps track of available game pieces to build
     */
    private Label availableSettlements;
    private Label availableCities;
    private Label availableRoads;
    private Label availableShips;

    /** The currently active trade window, may be null */
    private TradeWindow tradeWindow;

    /** Set of popup windows */
    private HashSet<Window> popups;

    /**
     * Input adapter that handles general clicks to the screen that are not on buttons or
     * any other of the stage actors. Used for choosing village positions, road positions, etc.
     */
    private InputAdapter inputAdapter;

    private GamePieces gamePieces;

    public SessionScreen(CatanGame pGame) {
        aGame = pGame;
        aSessionController = new SessionController(this);
        validIntersections = new ArrayList<>();
        validEdges = new ArrayList<>();
        validHexes = new ArrayList<>();
        boardHexes = new ArrayList<>();
        boardHarbours = new ArrayList<>();
        villages = new ArrayList<>();
        edgeUnits = new ArrayList<>();
        knights = new ArrayList<>();
        cityWalls = new ArrayList<>();
        highlightedPositions = new ArrayList<>();
        popups = new HashSet<>();
        boardOrigin = new MutablePair<>();
        resourceLabelMap = new EnumMap<>(ResourceKind.class);
        polyBatch = new PolygonSpriteBatch();
        highlightBatch = new PolygonSpriteBatch();
        gamePieces = GamePieces.getInstance();
        robberSprite = gamePieces.createRobber();
        setupBoardOrigin(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // sets the initializing mode of the session screen (VIEWMODE). set to CHOOSEACTIONMODE for testing purposes 
        aMode = SessionScreenModes.CHOOSEACTIONMODE;


        inputAdapter = new InputAdapter() {
            final Vector3 hitPosition = new Vector3();

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                // Check if the user clicked outside any of the windows, if so, dismiss them all
                hitPosition.set(gamePiecesStage.getCamera().unproject(hitPosition.set(screenX, screenY, 0)));
                final Actor hit = gamePiecesStage.hit(hitPosition.x, hitPosition.y, false);
                if (hit == null || !(hit instanceof Window)) {
                    for (Window popup : popups)
                        popup.remove();
                    popups.clear();
                }

                if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE) {
                    for (CoordinatePair validIntersection : validIntersections) {
                        if (screenX > boardOrigin.getLeft() + validIntersection.getLeft() * BASE - 10 &&
                                screenX < boardOrigin.getLeft() + validIntersection.getLeft() * BASE + 10 &&
                                screenY > boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) - 10 &&
                                screenY < boardOrigin.getRight() + validIntersection.getRight() * (LENGTH / 2) + 10) {

                            // The building position is valid, can clear the highlighted positions
                            highlightedPositions.clear();
                            validIntersections.clear();

                            currentlyPerformingMove.performNextMove(validIntersection);

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

                            // The edge position is valid, can clear the highlighted positions
                            highlightedPositions.clear();
                            validEdges.clear();

                            currentlyPerformingMove.performNextMove(validEdge);

                            return true;
                        }
                    }
                } else if (aMode == SessionScreenModes.CHOOSEHEXMODE) {
                    for (Hex validHex : validHexes) {
                        if (screenX > boardOrigin.getLeft() + validHex.getLeftCoordinate() * OFFX - 10 &&
                                screenX < boardOrigin.getLeft() + validHex.getLeftCoordinate() * OFFX + 10 &&
                                screenY > boardOrigin.getRight() + validHex.getRightCoordinate() * OFFY - 10 &&
                                screenY < boardOrigin.getRight() + validHex.getRightCoordinate() * OFFY + 10) {

                            // The hex position is valid, can clear the highlighted positions
                            highlightedPositions.clear();
                            validHexes.clear();

                            currentlyPerformingMove.performNextMove(validHex);

                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    quitGame();
                    return true;
                }
                return false;
            }
        };
    }

    private void quitGame(){
        aGame.switchScreen(ScreenKind.MAIN_MENU);
    }

    public void setValidMoveShipPositions(Pair<CoordinatePair, CoordinatePair> validShip) {
        HashSet<CoordinatePair> validShipEndpoints = aSessionController.requestValidShipEndpoints(aSessionController.getPlayerColor());

        // removes the end point at the intersection not connected to ships other than validShip
        for (CoordinatePair i : aSessionController.requestValidShipEndpoints(aSessionController.getPlayerColor())) {
            if (i.equals(validShip.getLeft()) || i.equals(validShip.getRight())) {
                boolean hasOtherEndpoint = false;
                for (CoordinatePair j : aSessionController.requestValidShipEndpoints(aSessionController.getPlayerColor())) {
                    if (aSessionController.isAdjacent(i, j) && !(j.equals(validShip.getLeft()) || j.equals(validShip.getRight()))) {
                        hasOtherEndpoint = true;
                        break;
                    }
                }
                if (!hasOtherEndpoint) {
                    validShipEndpoints.remove(i);
                }
            }
        }


        for (CoordinatePair i : validShipEndpoints) {
            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                if (aSessionController.isAdjacent(i, j) && !aSessionController.isOnLand(i, j)) {

                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
                    validEdges.add(edge);

                    for (EdgeUnit eu : aSessionController.getRoadsAndShips()) {
                        if (eu.hasEndpoint(i) && eu.hasEndpoint(j)) {
                            validEdges.remove(edge);
                        }
                    }
                }
            }
        }

        // add the valid ship position to validEdges
        validEdges.add(validShip);

        // highlights valid edge positions
        for (Pair<CoordinatePair, CoordinatePair> edge : validEdges) {
            highlightedPositions.add(gamePieces.createShip(edge.getLeft().getLeft(), edge.getLeft().getRight(), edge.getRight().getLeft(), edge.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
        }
    }

    @Override
    public void show() {
        aSessionStage = new Stage();
        gamePiecesStage = new Stage();

        // Combine input (click handling) from the InputAdapter and the Stage
        final InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gamePiecesStage);
        inputMultiplexer.addProcessor(aSessionStage);
        inputMultiplexer.addProcessor(inputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // resource table
        Table resourcesTable = new Table(CatanGame.skin);
        resourcesTable.setBackground("resTableBackground");
        resourcesTable.pad(10);

        for (ResourceKind resourceKind : ResourceKind.values()) {
            Table aTable = createResourceTable(resourceKind);
            resourcesTable.add(aTable).width(60).height(60).pad(5);
        }

        resourcesTable.pack();
        resourcesTable.setPosition(Gdx.graphics.getWidth() / 2 - resourcesTable.getWidth()* 3 / 4, 10);

        //fish table
        Table fishTable = new Table(CatanGame.skin);

        tradeFish = new TextButton("Fish Trade", CatanGame.skin);
        tradeFish.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Get fish token counts
                final FishTokenMap fishHand = aSessionController.getLocalPlayer().getFishTokenHand();
                //defining the following move
                MultiStepMove fishTrade = new MultiStepMove();
                fishTrade.<Pair<FishTokenMap,Integer>>addMove((tokenChoicePair) -> {
                    aSessionController.fishActionHandle(tokenChoicePair);
                    interractionDone();
                });
                // Create the Window
                final FishTradeWindow window = new FishTradeWindow("Fish Trade", CatanGame.skin, fishHand);
                window.setTradeTokenListener((tokenChoicePair) -> {
                    fishTrade.performNextMove(tokenChoicePair);
                });

                aSessionStage.addActor(window);
            }
        });
        fishTable.add(tradeFish).colspan(3).row();

        initFishTable(fishTable);
        fishTable.setPosition(resourcesTable.getX() + resourcesTable.getWidth() + 25, 10);

        //development flip chart table
        Table developmentFlipChart = new Table(CatanGame.skin);
        initDevelopmentFlipChartTable(developmentFlipChart);
        developmentFlipChart.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 10f, Align.top);


        // menu table
        Table menuTable = new Table(CatanGame.skin);
        menuTable.setBackground("resTableBackground");
        menuTable.setPosition(10, 10);

        temporaryFunctionalityTable = new Table(CatanGame.skin);
        temporaryFunctionalityTable.setBackground("resTableBackground");
        temporaryFunctionalityTable.setSize(200, 0);
        temporaryFunctionalityTable.setPosition(Gdx.graphics.getWidth() - 360, Gdx.graphics.getHeight() - 10);

        //end turn table 
        Table turnTable = new Table(CatanGame.skin);
        turnTable.setSize(200, 50);
        turnTable.setPosition(Gdx.graphics.getWidth() - 210, Gdx.graphics.getHeight() - 60);

        // longest road table
        longestRoad = new Table(CatanGame.skin);
        longestRoad.setBackground("resTableBackground");
        longestRoadOwner = new Label("Build an\nunbroken route \nof at least\n5 segments \nto get longest road", CatanGame.skin);
        longestRoadOwner.setAlignment(2);
        longestRoad.add(longestRoadOwner);
        longestRoad.setSize(140, developmentFlipChart.getHeight());
        longestRoad.setPosition(Gdx.graphics.getWidth() / 2f + developmentFlipChart.getWidth() - 20, Gdx.graphics.getHeight() - 10f, Align.top);

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
        setupBuildVillageButton(buildSettlementButton, VillageKind.SETTLEMENT, "Build Settlement");
        buildSettlementButton.pad(0, 10, 0, 10);
        menuTable.add(buildSettlementButton).padBottom(10).row();

        buildCityButton = new TextButton("Build City", CatanGame.skin);
        setupBuildVillageButton(buildCityButton, VillageKind.CITY, "Built City");
        buildCityButton.pad(0, 10, 0, 10);
        menuTable.add(buildCityButton).padBottom(10).row();

        buildRoadButton = new TextButton("Build Road", CatanGame.skin);
        setupBuildEdgeUnitButton(buildRoadButton, EdgeUnitKind.ROAD, "Build Road");
        buildRoadButton.pad(0, 10, 0, 10);
        menuTable.add(buildRoadButton).padBottom(10).row();

        buildShipButton = new TextButton("Build Ship", CatanGame.skin);
        setupBuildEdgeUnitButton(buildShipButton, EdgeUnitKind.SHIP, "Build Ship");
        buildShipButton.pad(0, 10, 0, 10);
        menuTable.add(buildShipButton).padBottom(10).row();

        buildKnightButton = new TextButton("Build Knight", CatanGame.skin);
        buildKnightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO uncomment this
                // Make sure the player has everything required to build a knight
//                if (!aSessionController.requestBuildKnight()) {
//                    addGameMessage("You can't build a knight (not enough resources or no valid positions)");
//                    return;
//                }

                // Create a new multi-step move to allow the player to place a knight
                currentlyPerformingMove = new MultiStepMove();

                // Keep track of the previous session mode
                final SessionScreenModes prevMode = aMode;

                // Switch the mode to allow the player to choose an intersection
                aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;

                // TODO: generate the actual valid positions for the knight
                for (CoordinatePair intersection : aSessionController.requestValidInitializationBuildIntersections()) {
                    validIntersections.add(intersection);
                    highlightedPositions.add(gamePieces.createSettlement(intersection.getLeft(), intersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                }

                // Make the current move place a knight at the specified position
                currentlyPerformingMove.<CoordinatePair>addMove(chosenIntersection -> {
                    // Clear the highlighted positions
                    validIntersections.clear();
                    highlightedPositions.clear();
                    // Build the knight
                    KnightActor knightActor = aSessionController.buildKnight(chosenIntersection);
                    knightActor.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            KnightActionsWindow actionWindow = knightActor.displayActions();
                            actionWindow.setWindowCloseListener(() -> popups.remove(actionWindow));
                            actionWindow.setOnKnightActivateClick(knightActor1 -> {
                                // TODO check for resources
                                knightActor.getKnight().setActive(true);
                                return true;
                            });
                            actionWindow.setOnKnightUpgradeClick(knightActor1 -> {
                                // TODO check for resources
                                knightActor.getKnight().upgrade();
                                return true;
                            });
                            popups.add(actionWindow);
                        }
                    });
                    addKnight(knightActor);
                    // Go back to the previous mode
                    aMode = prevMode;
                    // re-enable all appropriate actions
                    enablePhase(aSessionController.getCurrentGamePhase());
                });
            }
        });
        buildKnightButton.pad(0, 10, 0, 10);
        menuTable.add(buildKnightButton).padBottom(10).row();

        buildMetropolisButton = new TextButton("Build Metropolis", CatanGame.skin);
        buildMetropolisButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //create a new multi step move to allow the player to build a metropolis
                currentlyPerformingMove = new MultiStepMove();

                final SessionScreenModes prevMode = aMode;

                aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;

                for(CoordinatePair intersection: aSessionController.requestCityIntersections()) {
                    validIntersections.add(intersection);
                    highlightedPositions.add(gamePieces.createSettlement(intersection.getLeft(), intersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                }

                currentlyPerformingMove.<CoordinatePair>addMove(chosenIntersection -> {
                    // Clear the highlighted positions
                    validIntersections.clear();
                    highlightedPositions.clear();
                    // Build the metropolis
                    updateIntersection(chosenIntersection, aSessionController.getPlayerColor(), VillageKind.TRADE_METROPOLIS);
                    // Go back to the previous mode
                    aMode = prevMode;
                    // re-enable all appropriate actions
                    enablePhase(aSessionController.getCurrentGamePhase());
                });
            }
        });

        buildCityWallButton = new TextButton("Build City Wall", CatanGame.skin);
        buildCityWallButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!aSessionController.requestBuildCityWall(aSessionController.getPlayerColor())) {
                    addGameMessage("Not enough resources for building city walls");
                    return;
                }
                
                // Create a new multi-step move to allow the player to place a city wall
                MultiStepMove buildCityWall = new MultiStepMove();

                // Make the current move place a city wall at the specified position
                buildCityWall.<CoordinatePair>addMove(chosenIntersection -> {
                    // Build the city wall around chosen city intersection
                    aSessionController.buildCityWall(aSessionController.getPlayerColor(), chosenIntersection, false);
                    
                    // re-enable all appropriate actions
                    interractionDone();
                });
                
                // initiate multistepmove with the valid build positions
                List<CoordinatePair> validCityWallIntersections = aSessionController.requestValidCityWallIntersections(aSessionController.getPlayerColor());
                if (!validCityWallIntersections.isEmpty()) {
                    initChooseIntersectionMove(validCityWallIntersections, buildCityWall);
                } else {
                    addGameMessage("You do not have any cities without walls");
                } 
            }
        });
        buildCityWallButton.pad(0, 10, 0, 10);
        menuTable.add(buildCityWallButton).padBottom(10).row();

        buildMetropolisButton.pad(0,10,0,10);
        menuTable.add(buildMetropolisButton).padBottom(10).row();

        moveShipButton = new TextButton("Move Ship", CatanGame.skin);
        setupMoveShipButton(moveShipButton);
        moveShipButton.pad(0, 10, 0, 10);
        menuTable.add(moveShipButton).padBottom(10).row();

        playProgressCardButton = new TextButton("Play Progress Card", CatanGame.skin);
        setupPlayProgressCardButton(playProgressCardButton, "Play Progress Card", ProgressCardType.ROADBUILDING);
        playProgressCardButton.pad(0, 10, 0, 10);
        menuTable.add(playProgressCardButton).padBottom(10).row();


        // Add roll dice button
        rollDiceButton = new TextButton("Roll Dice", CatanGame.skin);
        rollDiceButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aSessionController.rollDice();
            }
        });
        menuTable.add(rollDiceButton).padBottom(10).row();

        // Add end turn button
        endTurnButton = new TextButton("End Turn", CatanGame.skin);
        endTurnButton.setColor(Color.YELLOW);
        endTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aSessionController.endTurnNotify();
            }
        });
        turnTable.add(endTurnButton).padBottom(10).row();

        // Add the barbarian label
        barbarianPositionLabel = new Label("Barbarian position: " + 7, CatanGame.skin);
        barbarianPositionLabel.setPosition(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() - 20f, Align.top);
        aSessionStage.addActor(barbarianPositionLabel);

        // Add maritime trade button
        domesticTradeButton = new TextButton("Domestic Trade", CatanGame.skin);
        domesticTradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Generate the trade ratios
                final ResourceMap tradeRatios = aSessionController.getTradeRatios();
                // Create the window
                final DomesticTradeWindow window = new DomesticTradeWindow("Domestic Trade", CatanGame.skin, tradeRatios);
                window.setDomesticTradeListener((offer, request, tradeRatio) -> {
                    aSessionController.maritimeTrade(offer, request, tradeRatio);
                    window.updateTradeRatios(aSessionController.getTradeRatios());
                });
                aSessionStage.addActor(window);
            }
        });
        menuTable.add(domesticTradeButton);

        tradeButton = new TextButton("Propose Trade", CatanGame.skin);
        tradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Create the window
//                final TradeWindow window = new TradeWindow("Trade", "test", new ResourceMap(), new ResourceMap(), CatanGame.skin);
                tradeWindow = new TradeWindow(CatanGame.skin);
                aSessionStage.addActor(tradeWindow);
                tradeWindow.requestScrollFocus();
//                tradeWindow.setMaxOffer(aSessionController.getMaxOffer()); // TODO uncomment when done debugging
                tradeWindow.setTradeProposalListener((offer, request) -> aSessionController.proposeTrade(offer, request));
                tradeWindow.setOfferAcceptListener((username, remoteOffer, localOffer) -> aSessionController.acceptTrade(username, remoteOffer, localOffer));
                tradeWindow.setWindowCloseListener(() -> {
                    tradeWindow = null;
                    // TODO maybe show a confirmation message before cancelling the trade?
                    aSessionController.cancelTrade();
                });
            }
        });
        menuTable.row();
        menuTable.add(tradeButton).padTop(10);

        // Pack the menu table to make it fit all its children
        menuTable.pad(10);
        menuTable.pack();

        // current player table
        currentPlayer = new Table(CatanGame.skin);
        currentPlayerLabel = new Label("", CatanGame.skin);
        currentPlayer.add(currentPlayerLabel);
        currentPlayer.setSize(menuTable.getWidth(), 50);
        currentPlayer.setPosition(10, Gdx.graphics.getHeight() - 60);
        updateCurrentPlayer(aSessionController.getCurrentPlayer());

        // current VPs' table
        playersVPLabel = new Label[3];
        playersVP = new Table[3];
        for (int i = 0; i < playersVPLabel.length; i++) {
            playersVPLabel[i] = new Label("", CatanGame.skin);
            Table currentVpTable = new Table(CatanGame.skin);
            currentVpTable.add(playersVPLabel[i]);
            currentVpTable.setSize(menuTable.getWidth(), 40);
            currentVpTable.setPosition(10, Gdx.graphics.getHeight() - 130 - 40 * i);
            playersVP[i] = currentVpTable;
        }
        updateVpTables();
        
        //Old Boot Table
        bootOwnerLabel = new Label("Boot Owner: No one", CatanGame.skin);
        giveBoot = new TextButton("Give Boot", CatanGame.skin);
        giveBoot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                Player localPlayer = aSessionController.getLocalPlayer();
                //addGameMessage("owner is " + aSessionController.getBootOwner().getUsername());
                // Finds the suitable Players
                if (aSessionController.getBootMalus(localPlayer) != 1) {
                    addGameMessage("Luckily you don't have the boot");
                    return;
                }

                ArrayList<Player> opponents =  aSessionController.getValidBootRecepients();

                if (opponents.isEmpty()){
                    addGameMessage("You are the best player");
                    addGameMessage("So you have to keep the Boot");
                    return;
                }

                MultiStepMove choosePlayerToGiveBoot = new MultiStepMove();
                choosePlayerToGiveBoot.<Player>addMove(chosenPlayer -> {
                    UpdateOldBoot request = UpdateOldBoot.newInstance(chosenPlayer.getUsername());
                    CatanGame.client.sendTCP(request);
                    interractionDone();
                });
                chooseOtherPlayer(opponents, choosePlayerToGiveBoot);
            }
        });
        bootOwner = new Table(CatanGame.skin);
        bootOwner.setSize(menuTable.getWidth(), 80);
        bootOwner.setPosition(10, menuTable.getHeight() + 20);
        bootOwner.setBackground(CatanGame.skin.newDrawable("white", Color.BLACK));
        bootOwner.add(bootOwnerLabel).pad(5).row();
        bootOwner.add(giveBoot).pad(5).row();

        // progress card table
        progressCardTable = new Table(CatanGame.skin);
        progressCardTable.setSize(10, 2 * LENGTH);

        progressCardTable.setDebug(true);
        progressCardTable.setPosition(Gdx.graphics.getWidth() / 2 - resourcesTable.getWidth()* 3 / 4, resourcesTable.getHeight() + 20);
        progressCardTable.setTouchable(Touchable.enabled);
        progressCardTable.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final PlayProgressCardWindow playProgressCard = new PlayProgressCardWindow("Hand", CatanGame.skin, aSessionController.getLocalPlayer().getProgressCardHand(), aSessionController.getCurrentGamePhase(), aSessionController.isMyTurn(), aSessionController.getSessionManager().getSession().firstBarbarianAttack);
                playProgressCard.setPlayProgressCardListener((type) -> {
                    // plays the chosen progress card
                    aSessionController.playProgressCard(type);
                });
                aSessionStage.addActor(playProgressCard);
            }
        });


        // sets center of board
        int offsetX, offsetY;

        // creates hexes of the board
        for (Hex hex : aSessionController.getHexes()) {
            offsetX = hex.getLeftCoordinate();
            offsetY = hex.getRightCoordinate();
            boardHexes.add(gamePieces.createHexagon((offsetX * OFFX), -(offsetY * OFFY), boardOrigin.left, boardOrigin.right, hex.getKind()));
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
            placeRobber(robberPos.getLeftCoordinate(), robberPos.getRightCoordinate());
        }

        // places merchant at initial position (off the board)
        // placeMerchant(GameRules.SIZE / 2 + 5, - (GameRules.SIZE / 2));

        // Create the Game Log table
        float pad = 10f;
        float tableWidth = 350f;
        float tableHeight = 300f;
        final Table table = new Table(CatanGame.skin);
        table.left().top();
        gameLog = new ScrollPane(table);
        gameLog.setOverscroll(false, false);
        gameLog.setX(Gdx.graphics.getWidth() - tableWidth - pad);
        gameLog.setY(Gdx.graphics.getHeight() / 2 - tableHeight / 2);
        gameLog.setWidth(tableWidth);
        gameLog.setHeight(tableHeight);

        aSessionStage.addActor(resourcesTable);
        aSessionStage.addActor(fishTable);
        aSessionStage.addActor(availableGamePiecesTable);
        aSessionStage.addActor(menuTable);
        aSessionStage.addActor(turnTable);
        aSessionStage.addActor(currentPlayer);
        aSessionStage.addActor(longestRoad);
        aSessionStage.addActor(temporaryFunctionalityTable);
        aSessionStage.addActor(progressCardTable);
        for (int i = 0; i < playersVP.length; i++) {
            aSessionStage.addActor(playersVP[i]);
        }
        aSessionStage.addActor(bootOwner);
        aSessionStage.addActor(gameLog);
        aSessionStage.addActor(developmentFlipChart);

        // Notify the controller that the session screen is displayed
        aSessionController.onScreenShown();

        // Begin the game
        aSessionController.checkIfMyTurn();
    }

    /** Update the label display the current barbarian position */
    void updateBarbarianPosition(int newDistance) {
        barbarianPositionLabel.setText("Barbarian position: " + newDistance);
    }

    /**
     * @param opponents client can choose from
     * @param move whose next move to perform will be called once a player has been chosen
     * Opens window where client can choose one of the opponent players
     * */
    public void chooseOtherPlayer(ArrayList<Player> opponents, MultiStepMove move) {
        // disable all possible actions
        disableAllButtons();

        final ChoosePlayerWindow choosePlayerWindow = new ChoosePlayerWindow("Choose Player", CatanGame.skin, opponents);
        choosePlayerWindow.setChoosePlayerListener((player) -> {
            // performs the given move with player
            move.performNextMove(player);
        });
        aSessionStage.addActor(choosePlayerWindow);
    }

    /**
     * @param valid intersections client can choose from
     * @param move whose next move to perform will be called once an intersection has been chosen
     * */
    public void initChooseIntersectionMove(List<CoordinatePair> valid, MultiStepMove move) {
    	// disable all possible actions
        disableAllButtons();

        // transfers all the valid intersections of validIntersections, and highlights each position on the board
        for (CoordinatePair i : valid) {
        	validIntersections.add(i);
        	highlightedPositions.add(gamePieces.createHighlightedIntersection(i.getLeft(), i.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
        }

        // sets the currently performing move attribute to given move.
        // When an intersection is chosen, the next move in that MultiStepMove will be performed
        currentlyPerformingMove = move;

        aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
    }

    /**
     * @param valid hexes client can choose from
     * @param move which next move to perform will be called once a hex has been chosen
     * */
    public void initChooseHexMove(List<Hex> valid, MultiStepMove move) {
        // System.out.println("init hex");
        // disable all possible actions
        disableAllButtons();

        // transfers all the valid intersections of validIntersections, and highlights each position on the board
        for (Hex h : valid) {
            validHexes.add(h);
            highlightedPositions.add(gamePieces.createHighlightedIntersection(h.getLeftCoordinate(), h.getRightCoordinate(), OFFX, OFFY*2, LENGTH, PlayerColor.WHITE));
        }

        // sets the currently performing move attribute to given move.
        // When an intersection is chosen, the next move in that MultiStepMove will be performed
        currentlyPerformingMove = move;

        aMode = SessionScreenModes.CHOOSEHEXMODE;
    }

    /**
     * @param valid edges client can choose from
     * @param move whose next move to perform will be called once an edge has been chosen
     * */
    public void initChooseEdgeMove(List<Pair<CoordinatePair, CoordinatePair>> valid, MultiStepMove move) {
    	// disable all possible actions
        disableAllButtons();

        // transfers all the valid intersections of validIntersections, and highlights each position on the board
        for (Pair<CoordinatePair, CoordinatePair> i : valid) {
        	validEdges.add(i);
        	if (aSessionController.isOnLand(i.getLeft(), i.getRight())) {
                highlightedPositions.add(gamePieces.createRoad(i.getLeft().getLeft(), i.getLeft().getRight(), i.getRight().getLeft(), i.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            } else {
                highlightedPositions.add(gamePieces.createShip(i.getLeft().getLeft(), i.getLeft().getRight(), i.getRight().getLeft(), i.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            }
        }

        // sets the currently performing move attribute to given move.
        // When an intersection is chosen, the next move in that MultiStepMove will be performed
        currentlyPerformingMove = move;

        aMode = SessionScreenModes.CHOOSEEDGEMODE;
    }


    /**
     * Opens a window that prompts the client to choose a progress card kind
     *
     * @param move whose next move to perform will be called once an ProgressCardKind has been chosen
     * */
    public void chooseProgressCardKind(MultiStepMove move, List<ProgressCardKind> kindsToChooseFrom) {
    	// disable all possible actions
        disableAllButtons();

        final ChooseProgressCardKindWindow chooseKindWindow = new ChooseProgressCardKindWindow("Choose Progress Card Kind", kindsToChooseFrom, CatanGame.skin);
        chooseKindWindow.setChooseProgressCardKindListener((kind) -> {
            // performs the given move with kind
            move.performNextMove(kind);
        });
        aSessionStage.addActor(chooseKindWindow);
    }

    /**
     * Opens a window that prompts the player to accept drawing a progress card
     * */
    public void chooseDraw(MultiStepMove move) {
     // disable all possible actions
        disableAllButtons();

        final ChooseDraw chooseDrawWindow = new ChooseDraw("Do you want to draw a progress card?", CatanGame.skin);
        chooseDrawWindow.setChooseDrawListener((answer) -> {
            // performs the given move with kind
            move.performNextMove(answer);
        });
        aSessionStage.addActor(chooseDrawWindow);
    }

    /**
     * Opens a window that prompts the client to choose a progress card type from cards
     * @param cards cards that player can choose type from
     * @param move whose next move to perform will be called once a ProgressCard has been chosen
     * */
    public void chooseProgressCard(Collection<ProgressCardType> cards, MultiStepMove move) {
    	// disable all possible actions
        disableAllButtons();

        final ChooseFromEnumCollectionWindow<ProgressCardType> chooseProgressCardWindow = new ChooseFromEnumCollectionWindow<>("Choose Progress Card", CatanGame.skin, cards);
        chooseProgressCardWindow.setChooseCardListener((type) -> {
        	//performs the given move with type
        	move.performNextMove(type);
        });
        aSessionStage.addActor(chooseProgressCardWindow);
    }

    /**
     * Opens a window that prompts the client to choose a resource card type from cards
     * @param resources the resources that player can choose type from
     * @param move whose next move to perform will be called once a ResourceKind has been chosen
     * */
    public void chooseResource(Collection<ResourceKind> resources, MultiStepMove move) {
    	// disable all possible actions
        disableAllButtons();

        final ChooseFromEnumCollectionWindow<ResourceKind> chooseResourceCardWindow = new ChooseFromEnumCollectionWindow<ResourceKind>("Choose Resource Card", CatanGame.skin, resources);
        chooseResourceCardWindow.setChooseCardListener((type) -> {
        	//performs the given move with type
        	move.performNextMove(type);
        });
        aSessionStage.addActor(chooseResourceCardWindow);
    }

    /**
     * Opens a window that prompts the client to choose a resource card type from cards
     * @param resources resources that player can choose type from
     * @param numberToChoose number of cards that player needs to choose
     * @param move whose next move to perform will be called once a ResourceKind has been chosen
     * */
    public void chooseMultipleResource(EnumMap<ResourceKind, Integer> resources, int numberToChoose, MultiStepMove move) {
    	// disable all possible actions
        disableAllButtons();

        final ChooseMultipleResourcesWindow chooseResourcesCardWindow = new ChooseMultipleResourcesWindow("Choose " + numberToChoose + " Resource Cards", CatanGame.skin, resources, numberToChoose);
        chooseResourcesCardWindow.setChooseCardListener((map) -> {
        	//performs the given move with type
        	move.performNextMove(map);
        });
        aSessionStage.addActor(chooseResourcesCardWindow);
    }

    /**
     * Opens a window that prompts the client to choose the red and yellow dice numbers
     * @param move whose next move to perform will be called once the two dice numbers have been chosen
     * */
    public void chooseDiceNumbers(MultiStepMove move) {
        // disable all possible actions
        disableAllButtons();

        final ChooseDiceResultWindow chooseDiceNumberWindow = new ChooseDiceResultWindow("Choose the results of the red and yellow die", CatanGame.skin);
        chooseDiceNumberWindow.setChooseDiceResultsListener((redDiceResult, yellowDiceResult) -> {
            //performs the given move
            move.performNextMove(DiceRollPair.newRoll(redDiceResult, yellowDiceResult));
        });
        aSessionStage.addActor(chooseDiceNumberWindow);
    }

    /**
     * Called after a multi step move has been fully performed, sets the mode of the GUI to CHOOSEACTIONMODE
     * and re-enables all the buttons according to current game phase if it is the client's turn
     * */
    public void interractionDone() {
        // System.out.println("interraction done");
    	// puts mode back to choose action mode
    	aMode = SessionScreenModes.CHOOSEACTIONMODE;

    	// clears the available build positions and highlighted positions
    	highlightedPositions.clear();
        validIntersections.clear();

    	// re-enables all the buttons according to current game phase if it is the client's turn
    	if (aSessionController.isMyTurn()) {
        	enablePhase(aSessionController.getCurrentGamePhase());
        }
        aSessionController.getSessionManager().finishCurrentlyExecutingProgressCard();
    }

    private void setupPlayProgressCardButton(TextButton playButton, String buttonText, ProgressCardType type) {
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!aSessionController.controlPlayProgressCard(aSessionController.getPlayerColor())) {
                    addGameMessage("Some comment about progress card play");
                }
                aSessionController.getProgressCardHandler().handle(type, aSessionController.getPlayerColor());
                System.out.println(type.toString().toLowerCase()+" card played");
            }
        });
    }

    private void setupBuildVillageButton(TextButton buildButton, VillageKind kind, String buttonText) {
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!aSessionController.requestBuildVillage(aSessionController.getPlayerColor(), kind)) {
                    addGameMessage("Not enough resources for building the " + kind.name().toLowerCase());
                    return;
                }

                // create a MultiStepMove and set session screen current multistepmove
                currentlyPerformingMove = new MultiStepMove();

                // disable all other buttons
                disableAllButtons();
                buildButton.setDisabled(false);

                if (aMode != SessionScreenModes.CHOOSEINTERSECTIONMODE) {
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
                    // adds move that will build the village at chosen intersection
                    currentlyPerformingMove.<CoordinatePair>addMove(chosenIntersection -> {
                        aSessionController.buildVillage(chosenIntersection, kind, aSessionController.getPlayerColor(), false, false);
                        aMode = SessionScreenModes.CHOOSEACTIONMODE;
                        buildButton.setText(buttonText);

                        // re-enable all appropriate actions
                        if (aSessionController.isMyTurn()) {
                            enablePhase(aSessionController.getCurrentGamePhase());
                        }
                    });

                    aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;

                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEINTERSECTIONMODE) {
                    validIntersections.clear();
                    highlightedPositions.clear();
                    buildSettlementButton.setText(buttonText);
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;

                    // re-enable all appropriate actions
                    if (aSessionController.isMyTurn()) {
                    	enablePhase(aSessionController.getCurrentGamePhase());
                    }
                }
            }
        });
    }

    private void getValidEdgePosition(EdgeUnitKind kind) {
        if (kind == EdgeUnitKind.ROAD) {
            for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
                for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                    if (aSessionController.isAdjacent(i, j) && aSessionController.isOnLand(i, j)) {

                        Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
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

                        Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
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
    }
    private void getValidEdgePosition() {
        // loops through valid road end points and adds valid edges (both ships and roads)
        for (CoordinatePair i : aSessionController.requestValidRoadEndpoints(aSessionController.getPlayerColor())) {
            for (CoordinatePair j : aSessionController.getIntersectionsAndEdges()) {
                if (aSessionController.isAdjacent(i, j)) {

                    Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(i, j);
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


    public void buildEdgeUnitForFree(){ //TODO does not work
        // create a MultiStepMove and set session screen current multistepmove
        if (aMode != SessionScreenModes.CHOOSEEDGEMODE) {
            // the following loop go through requested valid build positions
            getValidEdgePosition(EdgeUnitKind.ROAD);

            // adds move that will build the village at chosen intersection
            currentlyPerformingMove = new MultiStepMove();
            currentlyPerformingMove.<Pair<CoordinatePair, CoordinatePair>>addMove(chosenEdge -> {
                EdgeUnitKind kind = EdgeUnitKind.ROAD;
                if (!aSessionController.isOnLand(chosenEdge.getLeft(), chosenEdge.getRight())) {
                    kind = EdgeUnitKind.SHIP;
                }
                aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), chosenEdge.getLeft(), chosenEdge.getRight(), kind, false, true);

                aMode = SessionScreenModes.CHOOSEACTIONMODE;
                // re-enable all appropriate actions
                if (aSessionController.isMyTurn()) {
                    enablePhase(aSessionController.getCurrentGamePhase());
                }
            });

            // make a sprite that highlights the areas
            for (Pair<CoordinatePair, CoordinatePair> edge : validEdges) {
                highlightedPositions.add(gamePieces.createRoad(edge.getLeft().getLeft(), edge.getLeft().getRight(), edge.getRight().getLeft(), edge.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            }
            aMode = SessionScreenModes.CHOOSEEDGEMODE;
            // edgePieceKind = kind;
        } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {
            validEdges.clear();
            highlightedPositions.clear();
            aMode = SessionScreenModes.CHOOSEACTIONMODE;
            // re-enable all appropriate actions
            if (aSessionController.isMyTurn()) {
                enablePhase(aSessionController.getCurrentGamePhase());
            }
        }
        addGameMessage("" + highlightedPositions.size());
    }


    private void setupBuildEdgeUnitButton(TextButton buildButton, EdgeUnitKind kind, String buttonText) {
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!aSessionController.requestBuildEdgeUnit(aSessionController.getPlayerColor(), kind)) {
                    addGameMessage("Not enough resources for building a " + kind.name().toLowerCase());
                    return;
                }

                // disable all other buttons
                disableAllButtons();
                buildButton.setDisabled(false);

                // create a MultiStepMove and set session screen current multistepmove
                currentlyPerformingMove = new MultiStepMove();

                if (aMode != SessionScreenModes.CHOOSEEDGEMODE) {
                    // the following loop go through requested valid build positions
                    getValidEdgePosition(kind);

                 // adds move that will build the edge at chosen intersection
                    currentlyPerformingMove.<Pair<CoordinatePair, CoordinatePair>>addMove(chosenEdge -> {
                        aSessionController.buildEdgeUnit(aSessionController.getPlayerColor(), chosenEdge.getLeft(), chosenEdge.getRight(), kind, false, false);

                        aMode = SessionScreenModes.CHOOSEACTIONMODE;
                        buildButton.setText(buttonText);

                        // re-enable all appropriate actions
                        if (aSessionController.isMyTurn()) {
                            enablePhase(aSessionController.getCurrentGamePhase());
                        }
                    });

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
                    // edgePieceKind = kind;
                    buildButton.setText("Cancel");
                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {
                    validEdges.clear();
                    highlightedPositions.clear();
                    buildRoadButton.setText(buttonText);
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                    // re-enable all appropriate actions
                    if (aSessionController.isMyTurn()) {
                    	enablePhase(aSessionController.getCurrentGamePhase());
                    }
                }
            }
        });
    }

    private void setupMoveShipButton(TextButton moveButton) {
        moveButton.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (aMode != SessionScreenModes.CHOOSEEDGEMODE) {
                    // loop through all valip ships to move
                    for (EdgeUnit eu : aSessionController.requestValidShips(aSessionController.getPlayerColor())) {
                        // add its endpoints as a pair of coordinate to validedges
                        Pair<CoordinatePair, CoordinatePair> edge = new MutablePair<>(eu.getAFirstCoordinate(), eu.getASecondCoordinate());
                        validEdges.add(edge);
                    }
                    // create a highlighted piece and add to highlightedpositions
                    for (Pair<CoordinatePair, CoordinatePair> edge : validEdges) {
                        highlightedPositions.add(gamePieces.createShip(edge.getLeft().getLeft(), edge.getLeft().getRight(), edge.getRight().getLeft(), edge.getRight().getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                    }

                    // disable all other buttons
                    disableAllButtons();
                    moveButton.setDisabled(false);

                    // create a MultiStepMovingshipMove and set session screen current multistepmove
                    currentlyPerformingMove = new MultiStepMovingshipMove();

                    // adds move that will save the chosen
                    currentlyPerformingMove.<Pair<CoordinatePair, CoordinatePair>>addMove(chosenShip -> {
                        ((MultiStepMovingshipMove) currentlyPerformingMove).setShipToMove(chosenShip);
                        // highlights valid destinations
                        setValidMoveShipPositions(chosenShip);
                        // chosenShip is removed from GUI game board
                        removeEdgeUnit(chosenShip.getLeft().getLeft(), chosenShip.getLeft().getRight(), chosenShip.getRight().getLeft(), chosenShip.getRight().getRight());
                    });
                    // adds move that will move the ship once a new edge is chosen
                    currentlyPerformingMove.<Pair<CoordinatePair, CoordinatePair>>addMove(chosenDest -> {
                        Pair<CoordinatePair, CoordinatePair> ship = ((MultiStepMovingshipMove) currentlyPerformingMove).getShipToMove();
                        aSessionController.moveEdge(ship.getLeft(), ship.getRight(), chosenDest.getLeft(), chosenDest.getRight(), aSessionController.getPlayerColor(), EdgeUnitKind.SHIP, false);

                        aMode = SessionScreenModes.CHOOSEACTIONMODE;
                        moveShipButton.setText("Move Ship");
                        // re-enable all appropriate actions
                        if (aSessionController.isMyTurn()) {
                            enablePhase(aSessionController.getCurrentGamePhase());
                        }
                    });

                    aMode = SessionScreenModes.CHOOSEEDGEMODE;
                    moveButton.setText("Cancel");

                } else if (aMode == SessionScreenModes.CHOOSEEDGEMODE) {

                    // if there is only one move left, this will imply a ship has already been chosen
                    if (currentlyPerformingMove.movesLeft() == 1) {
                     // put the chosen ship game piece back in GUI
                        Pair<CoordinatePair, CoordinatePair> ship = ((MultiStepMovingshipMove) currentlyPerformingMove).getShipToMove();
                        updateEdge(ship.getLeft(), ship.getRight(), EdgeUnitKind.SHIP, aSessionController.getPlayerColor());
                    }

                    validEdges.clear();
                    highlightedPositions.clear();
                    moveButton.setText("Move Ship");
                    aMode = SessionScreenModes.CHOOSEACTIONMODE;
                    // re-enable all appropriate actions
                    if (aSessionController.isMyTurn()) {
                    	enablePhase(aSessionController.getCurrentGamePhase());
                    }
                }
            }

        });
    }

    private Table createResourceTable(ResourceKind type) {
        // Get the name of the resource
        final String resName = type.toString().toLowerCase();

        Table resourceTable = new Table(CatanGame.skin);
        resourceTable.setBackground(CatanGame.skin.getDrawable(resName + "-color"));

        // Display the resource name
        resourceTable.add(new Label(resName, CatanGame.skin));
        resourceTable.row();

        // Display the resource count
        Label l = new Label("0", CatanGame.skin);
        resourceTable.add(l);
        resourceLabelMap.put(type, l);

        resourceTable.pad(10);
        resourceTable.pack();

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
        int drawnBigFishery = 0;
        for (Hex hex : aSessionController.getHexes()) {
            Integer prob = hex.getDiceNumber();
            if (hex.getKind() == TerrainKind.BIG_FISHERY) {
                float yPos = 0;
                float xPos = 0;
                switch (drawnBigFishery) {
                    case 0:
                         xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX + 8));
                         yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY - 20));
                        break;
                    case 1:
                        xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX + 8));
                        yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY));
                        break;
                    case 2:
                        xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX - 20));
                        yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY - 20));
                        break;
                    case 3:
                        xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX - 20));
                        yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY));
                        break;
                    default:
                        break;
                }
                CatanGame.skin.getFont("default").draw(polyBatch, prob.toString(), xPos, yPos);
                drawnBigFishery++;
            } else if (prob != null && prob != 0) {
                float xPos = (float) (boardOrigin.getLeft() + (hex.getLeftCoordinate() * OFFX - 7));
                float yPos = (float) (boardOrigin.getRight() - (hex.getRightCoordinate() * OFFY - 5));
                CatanGame.skin.getFont("default").draw(polyBatch, prob.toString(), xPos, yPos);
            }
        }
        robberSprite.draw(polyBatch);
        if (merchantRegion != null) {
            polyBatch.draw(merchantRegion, boardOrigin.getLeft(), boardOrigin.getRight());
        }

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

        gamePiecesStage.act(delta);
        gamePiecesStage.draw();
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
        gamePiecesStage.dispose();
    }

    /**
     * Called when a new trade proposal was received.
     *
     * @param username username of the player that initiated the trade
     * @param offer    the offer of the player
     * @param request  the request of the player
     */
    void onIncomingTrade(String username, ResourceMap offer, ResourceMap request) {
        tradeWindow = new TradeWindow(username, offer, request, CatanGame.skin);
//        tradeWindow.setMaxOffer(aSessionController.getMaxOffer()); // TODO uncomment when done debugging
        tradeWindow.setOfferProposalListener(offer1 -> aSessionController.proposeOffer(offer1));
        tradeWindow.setWindowCloseListener(() -> {
            tradeWindow = null;
            aSessionController.cancelOffer();
        });
        aSessionStage.addActor(tradeWindow);
    }

    /**
     * Called when a reply/offer to a trade proposal was received.
     *
     * @param username username of the player proposing the offer
     * @param offer    the offer of the player
     */
    void onTradeOfferReceived(String username, ResourceMap offer) {
        if (tradeWindow != null)
            tradeWindow.addTradeOffer(username, offer);
    }

    /**
     * Called when an offer was cancelled.
     * @param username username of the player who cancelled their offer
     */
    void onTradeOfferCancelled(String username) {
        if (tradeWindow != null)
            tradeWindow.removeTradeOffer(username);
    }

    /**
     * Called when the trade was completed.
     */
    void onTradeCompleted() {
        if (tradeWindow != null)
            tradeWindow.close();
    }

    /**
     * Places Robber on given coordinates
     *
     * @param xCor left coordinate
     * @param yCor right coordinate
     */
    public void placeRobber(int xCor, int yCor) {

        float xPos = xCor * OFFX;
        float yPos = -yCor * OFFY;

        robberSprite.setPosition(boardOrigin.getLeft() + xPos - robberSprite.getWidth() / 2f,
                boardOrigin.getRight() + yPos - robberSprite.getHeight() / 2f);
    }

    /**
     * Places Merchant on given coordinates
     *
     * @param xCor left coordinate
     * @param yCor right coordinate
     * */
    public void placeMerchant(int xCor, int yCor) {

        merchantRegion = gamePieces.createMerchant(xCor, yCor, OFFX, OFFY, PIECEBASE);
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
     * adds a button to the temporary functionality table which on change executes given listener. All these buttons are removed
     * at the end of a turn
     * @param temp  temporary button
     * */

    public void addTemporaryFunctionality(TextButton temp) {
        temporaryFunctionalityTable.setSize(temporaryFunctionalityTable.getWidth(), temporaryFunctionalityTable.getHeight() + 40);
        temporaryFunctionalityTable.setPosition(Gdx.graphics.getWidth() - 360, Gdx.graphics.getHeight() - (temporaryFunctionalityTable.getHeight() + 10));
        temporaryFunctionalityTable.add(temp).pad(5).row();
    }

    /**
     * removes all temporary functionality buttons from the temporary functionality table
     * */
    private void removeTemporaryFunctionalities() {
        temporaryFunctionalityTable.clearChildren();
        temporaryFunctionalityTable.setSize(200, 0);
        temporaryFunctionalityTable.setPosition(Gdx.graphics.getWidth() - 360, Gdx.graphics.getHeight() - 10);
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

            if (xV0 == xPos - PIECEBASE / 2.0 && yV0 == yPos - PIECEBASE / 2.0) {
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
     * puts a new city wall piece at given position of given color. removes the city wall already at that position
     * */
    public void putCityWall(CoordinatePair position, PlayerColor color) {
        int offsetX = position.getLeft();
        int offsetY = position.getRight();
        
        Image cityWall = gamePieces.createCityWall(color);
        float xPos = offsetX * BASE;
        float yPos = -(offsetY * LENGTH / 2);
        
        removeCityWall(position);
        
        cityWall.setPosition(getBoardOrigin().getLeft() + xPos - cityWall.getOriginX(), getBoardOrigin().getRight() + yPos - cityWall.getOriginY());
        cityWalls.add(cityWall);
        gamePiecesStage.addActor(cityWall);
    }
    
    /**
     * removes city wall at given position
     * */
    public void removeCityWall(CoordinatePair position) {
        int offsetX = position.getLeft();
        int offsetY = position.getRight();
        
        float xPos = getBoardOrigin().getLeft() + offsetX * BASE;
        float yPos = getBoardOrigin().getRight() - (offsetY * LENGTH / 2);
        
        Image cityWallToRemove = null;
        
        for (Image cityWall : cityWalls) {
            float xCor = cityWall.getX();
            float yCor = cityWall.getY();
            if (Math.abs(xPos - xCor) <= cityWall.getWidth()/2f && Math.abs(yPos - yCor) <= cityWall.getHeight()/2f) {
                cityWallToRemove = cityWall;
                break;
            }
        }
        
        if (cityWallToRemove != null) {
            cityWalls.remove(cityWallToRemove);
            cityWallToRemove.remove();
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
     * adds image with given progress card type to hand on the board
     * */
    public void addCardToHand(ProgressCardType type) {
        progressCardTable.add(gamePieces.createProgressCard(type, LENGTH * 4/3f, LENGTH * 2)).pad(5);
        progressCardTable.pack();
    }

    /**
     * removes image with given progress card type from hand on the board
     * */
    public void removeCardFromHand(ProgressCardType type) {
        //TODO : aina needs help with this :(
    }
    
    public void updateLongestRoadOwner(PlayerColor newOwner, String name) {
        if (newOwner == null) {
            longestRoadOwner.setText("Build an\nunbroken route \nof at least\n5 segments \nto get longest road");
        } else if (newOwner != aSessionController.getPlayerColor()) {
            longestRoadOwner.setText(name + "\nowns the\nlongest road");
        } else {
            longestRoadOwner.setText("You have the\nlongest road!");
        }
    }

    void disableAllButtons() {
        buildSettlementButton.setDisabled(true);
        buildCityButton.setDisabled(true);
        buildRoadButton.setDisabled(true);
        buildShipButton.setDisabled(true);
        domesticTradeButton.setDisabled(true);
        endTurnButton.setDisabled(true);
        moveShipButton.setDisabled(true);
        rollDiceButton.setDisabled(true);
        buildMetropolisButton.setDisabled(true);
        giveBoot.setDisabled(true);
        buildKnightButton.setDisabled(true);
        buildCityWallButton.setDisabled(true);
        tradeButton.setDisabled(true);
        improveTrade.setDisabled(true);
        improveScience.setDisabled(true);
        improvePolitics.setDisabled(true);
        tradeFish.setDisabled(true);
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
                domesticTradeButton.setDisabled(true);
                endTurnButton.setDisabled(true);
                moveShipButton.setDisabled(true);
                buildMetropolisButton.setDisabled(true);
                giveBoot.setDisabled(true);
//                buildKnightButton.setDisabled(true);
                buildCityWallButton.setDisabled(true);
                tradeButton.setDisabled(true);
                improveTrade.setDisabled(true);
                improveScience.setDisabled(true);
                improvePolitics.setDisabled(true);
                tradeFish.setDisabled(true);

                rollDiceButton.setDisabled(false);
                break;
            case SETUP_PHASE_TWO_CLOCKWISE:
                // Allow the player only to choose a settlement and a road
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                domesticTradeButton.setDisabled(true);
                rollDiceButton.setDisabled(true);
                endTurnButton.setDisabled(true);
                moveShipButton.setDisabled(true);
                buildMetropolisButton.setDisabled(true);
                giveBoot.setDisabled(true);
                buildKnightButton.setDisabled(true);
                buildCityWallButton.setDisabled(true);
                tradeButton.setDisabled(true);
                improveTrade.setDisabled(true);
                improveScience.setDisabled(true);
                improvePolitics.setDisabled(true);
                tradeFish.setDisabled(true);

                initialize(true);
                break;
            case SETUP_PHASE_TWO_COUNTERCLOCKWISE:
                // Allow the player only to choose a city and a road
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                domesticTradeButton.setDisabled(true);
                rollDiceButton.setDisabled(true);
                endTurnButton.setDisabled(true);
                moveShipButton.setDisabled(true);
                buildMetropolisButton.setDisabled(true);
                giveBoot.setDisabled(true);
                buildKnightButton.setDisabled(true);
                buildCityWallButton.setDisabled(true);
                tradeButton.setDisabled(true);
                improveTrade.setDisabled(true);
                improveScience.setDisabled(true);
                improvePolitics.setDisabled(true);
                tradeFish.setDisabled(true);

                initialize(false);
                break;
            case TURN_FIRST_PHASE:
                // Allow the player only to roll the dice (or play the Alchemist, in future updates)
                buildSettlementButton.setDisabled(true);
                buildCityButton.setDisabled(true);
                buildRoadButton.setDisabled(true);
                buildShipButton.setDisabled(true);
                domesticTradeButton.setDisabled(true);
                endTurnButton.setDisabled(true);
                moveShipButton.setDisabled(true);
                buildMetropolisButton.setDisabled(true);
                giveBoot.setDisabled(true);
                buildKnightButton.setDisabled(true);
                buildCityWallButton.setDisabled(true);
                tradeButton.setDisabled(true);
                improveTrade.setDisabled(true);
                improveScience.setDisabled(true);
                improvePolitics.setDisabled(true);
                tradeFish.setDisabled(true);

                rollDiceButton.setDisabled(false);
                break;
            case TURN_SECOND_PHASE:
                // Prevent the player from rolling the dice
                buildSettlementButton.setDisabled(false);
                buildCityButton.setDisabled(false);
                buildRoadButton.setDisabled(false);
                buildShipButton.setDisabled(false);
                domesticTradeButton.setDisabled(false);
                endTurnButton.setDisabled(false);
                moveShipButton.setDisabled(false);
                buildMetropolisButton.setDisabled(false);
                giveBoot.setDisabled(false);
                buildKnightButton.setDisabled(false);
                buildCityWallButton.setDisabled(false);
                tradeButton.setDisabled(false);
                improveTrade.setDisabled(false);
                improveScience.setDisabled(false);
                improvePolitics.setDisabled(false);
                tradeFish.setDisabled(false);

                rollDiceButton.setDisabled(true);
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
        domesticTradeButton.setDisabled(true);
        endTurnButton.setDisabled(true);
        buildMetropolisButton.setDisabled(true);
        buildKnightButton.setDisabled(true);
        buildCityWallButton.setDisabled(true);
        moveShipButton.setDisabled(true);
        tradeButton.setDisabled(true);
        improveTrade.setDisabled(true);
        improveScience.setDisabled(true);
        improvePolitics.setDisabled(true);
        tradeFish.setDisabled(true);
        removeTemporaryFunctionalities();

    }

    /**
     * Triggers initialization mode. All other actions are blocked until an intersection and an edge has been chosen.
     *
     * @param firstInit Flag indicating whether it's the
     */
    private void initialize(boolean firstInit) {
        aMode = SessionScreenModes.CHOOSEINTERSECTIONMODE;
        // Highlight the valid positions where building can be placed
        for (CoordinatePair i : aSessionController.requestValidInitializationBuildIntersections()) {
            validIntersections.add(i);
            if (firstInit) {
                highlightedPositions.add(gamePieces.createSettlement(i.getLeft(), i.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            } else {
                highlightedPositions.add(gamePieces.createCity(i.getLeft(), i.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
            }
        }

        // Creates the MultiStepMove involved in initializing
        currentlyPerformingMove = new MultiStepInitMove();

        // Set the type of village that the player is allowed to place
        final VillageKind villageKind;
        if (firstInit) {
            villageKind = VillageKind.SETTLEMENT;
        } else {
            villageKind = VillageKind.CITY;
        }
           // villagePieceKind = VillageKind.SETTLEMENT;
            // adds the move that will set up road building, and save the chosen intersection
            currentlyPerformingMove.<CoordinatePair>addMove(chosenIntersection -> {
                ((MultiStepInitMove) currentlyPerformingMove).setInitIntersection(chosenIntersection);

             // Building place was picked, can now switch the mode to allow the player to pick a road place
                aMode = SessionScreenModes.CHOOSEEDGEMODE;

                // show a transparent version of settlement on validIntersection
               if (firstInit) {
                   highlightedPositions.add(gamePieces.createSettlement(chosenIntersection.getLeft(), chosenIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
               } else {
                   highlightedPositions.add(gamePieces.createCity(chosenIntersection.getLeft(), chosenIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
               }

                // finds all valid adjacent edges
                for (CoordinatePair i : aSessionController.getIntersectionsAndEdges()) {
                    if (chosenIntersection.isAdjacentTo(i) && !i.isOccupied()) {
                        validEdges.add(new MutablePair<>(i, chosenIntersection));
                        // show a transparent version of valid adjacent roads
                        if (aSessionController.isOnLand(i, chosenIntersection)) {
                            highlightedPositions.add(gamePieces.createRoad(i.getLeft(), i.getRight(), chosenIntersection.getLeft(), chosenIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        } else {
                            highlightedPositions.add(gamePieces.createShip(i.getLeft(), i.getRight(), chosenIntersection.getLeft(), chosenIntersection.getRight(), BASE, LENGTH, PIECEBASE, aSessionController.getPlayerColor()));
                        }
                    }
                }
                // initVillageIntersection = chosenIntersection;
            });

            currentlyPerformingMove.<Pair<CoordinatePair, CoordinatePair>>addMove(chosenEndpoints -> {

                aMode = SessionScreenModes.CHOOSEACTIONMODE;

                EdgeUnitKind Edgekind = EdgeUnitKind.ROAD;
                if (!aSessionController.isOnLand(chosenEndpoints.getLeft(), chosenEndpoints.getRight())) {
                    Edgekind = EdgeUnitKind.SHIP;
                }

                CoordinatePair initIntersection = ((MultiStepInitMove) currentlyPerformingMove).getInitIntersection();

                aSessionController.buildInitialVillageAndRoad(initIntersection, chosenEndpoints.getLeft(), chosenEndpoints.getRight(), villageKind, Edgekind);

                // Stop the initialization phase only after the city was placed
                // if (villagePieceKind == VillageKind.CITY)
                //    initializing = false;

                // End the turn
                aSessionController.endTurn();
            });


        // edgePieceKind = EdgeUnitKind.ROAD;
    }

    /**
     * unlocks menu build bar. IMPORTANT: DO NOT GIVE TURN AT INITIALIZATION
     */
    public void giveTurn() {
        aMode = SessionScreenModes.CHOOSEACTIONMODE;
    }

    /**
     * Sets Table background Color given Player's Color
     */
    void setPlayerTableColor(Table table, PlayerColor playerColor) {
        // sets background color to current player's color
        switch (playerColor) {
            case BLUE:
                table.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
                break;
            case ORANGE:
                table.setBackground(CatanGame.skin.newDrawable("white", Color.ORANGE));
                break;
            case RED:
                table.setBackground(CatanGame.skin.newDrawable("white", Color.RED));
                break;
            case WHITE:
                table.setBackground(CatanGame.skin.newDrawable("white", Color.WHITE));
                break;
            case YELLOW:
                table.setBackground(CatanGame.skin.newDrawable("white", Color.YELLOW));
                break;
            default:
                break;
        }
    }

    /**
     * Updates the current player
     */
    void updateCurrentPlayer(Player newCurrentPlayer) {
        String currentPlayerName = newCurrentPlayer.getAccount().getUsername();
        currentPlayerLabel.setText("Current Player: " + currentPlayerName);
        setPlayerTableColor(currentPlayer, newCurrentPlayer.getColor());
    }

    /**
     * Updates the players' VP
     */
    void updateVpTables() {
        Player[] players = aSessionController.getPlayers();
        for (int i = 0; i < playersVPLabel.length && i < players.length; i++) {
            playersVPLabel[i].setText(players[i].getUsername() + ": " + aSessionController.currentVP(players[i]));
            setPlayerTableColor(playersVP[i], players[i].getColor());
        }
    }

    void showWinner(Player p) {
        disableAllButtons();
        MultiStepMove quit = new MultiStepMove();
        quit.addMove(o -> quitGame());
        WinnerWindow winnerWindow = new WinnerWindow("We have a winner !", CatanGame.skin, p);
        winnerWindow.setWindowCloseListener(() -> {
            // performs the given move with player
            quit.performNextMove(new Object());
        });
        aSessionStage.addActor(winnerWindow);
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

    public void showDice(int redDice, int yellowDice) {

        Table yellow = new Table();
        Table red = new Table();

        yellow.setBackground(CatanGame.skin.newDrawable("white", Color.YELLOW));
        red.setBackground(CatanGame.skin.newDrawable("white", Color.RED));

        yellow.setSize(60, 60);
        red.setSize(60, 60);

        yellow.setPosition(Gdx.graphics.getWidth() - 70, Gdx.graphics.getHeight() - 140);
        red.setPosition(Gdx.graphics.getWidth() - 140, Gdx.graphics.getHeight() - 140);

        yellow.add(new Label("" + yellowDice, CatanGame.skin));
        red.add(new Label("" + redDice, CatanGame.skin));

        aSessionStage.addActor(yellow);
        aSessionStage.addActor(red);
    }

    /** Refresh the resource bar to display the most recent values for the local player's resources */
    public void updateResourceBar(ResourceMap updates) {
        for (Map.Entry<ResourceKind, Integer> entry : updates.entrySet()) {
            ResourceKind resourceKind = entry.getKey();
            Label l = resourceLabelMap.get(resourceKind);
            int newValue = entry.getValue();
            l.setText(newValue + "");
        }
    }

    /** refresh city improvement table */
    public void updateScienceImprovements(int level) {
        String improvement = GameRules.getGameRulesInstance().getScienceImprovmentType(level).toString().toLowerCase();
        scienceImprovementLabel.setText(improvement);
        scienceImprovementLevel.setText("Level: " + Integer.toString(level) + "\n" + improvement);
    }

    public void updateTradeImprovements(int level) {
        String improvement = GameRules.getGameRulesInstance().getTradeImprovementType(level).toString().toLowerCase();
        tradeImprovementLabel.setText(improvement);
        tradeImprovementLevel.setText("Level: " + Integer.toString(level) + "\n" + improvement);
    }

    public void updatePoliticsImprovements(int level) {
        String improvement = GameRules.getGameRulesInstance().getPoliticsImprovementType(level).toString().toLowerCase();
        politicsImprovementLabel.setText(improvement);
        politicsImprovementLevel.setText("Level: " + Integer.toString(level) + "\n" + improvement);
    }

    void initFishTable(Table fishTable) {
        oneFishCount = new Table(CatanGame.skin);
        twoFishCount = new Table(CatanGame.skin);
        threeFishCount  = new Table(CatanGame.skin);
        oneFishCountLabel = new Label("0",CatanGame.skin);
        twoFishCountLabel = new Label("0",CatanGame.skin);
        threeFishCountLabel = new Label("0",CatanGame.skin);

        oneFishCount.add(new Label("1 fish", CatanGame.skin)).row();
        twoFishCount.add(new Label("2 fish", CatanGame.skin)).row();
        threeFishCount.add(new Label("3 fish", CatanGame.skin)).row();
        oneFishCount.add(oneFishCountLabel).row();
        twoFishCount.add(twoFishCountLabel).row();
        threeFishCount.add(threeFishCountLabel).row();

        oneFishCount.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        twoFishCount.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));
        threeFishCount.setBackground(CatanGame.skin.newDrawable("white", Color.BLUE));

        fishTable.add(oneFishCount).width(60).height(60).pad(5);
        fishTable.add(twoFishCount).width(60).height(60).pad(5);
        fishTable.add(threeFishCount).width(60).height(60).pad(5);

        fishTable.setBackground("resTableBackground");
        fishTable.pad(10);
        fishTable.pack();
    }

    void updateFishTable(FishTokenMap newTokenCount) {
            oneFishCountLabel.setText(newTokenCount.getOneFish() + "");
            twoFishCountLabel.setText(newTokenCount.getTwoFish() + "");
            threeFishCountLabel.setText(newTokenCount.getThreeFish() + "");
    }

    void initDevelopmentFlipChartTable(Table developmentFlipChart) {
        tradeImprovement = new Table(CatanGame.skin);
        scienceImprovement = new Table(CatanGame.skin);
        politicsImprovement = new Table(CatanGame.skin);

        tradeImprovementLevel = new Label("Level: 0",CatanGame.skin);
        scienceImprovementLevel = new Label("Level: 0",CatanGame.skin);
        politicsImprovementLevel = new Label("Level: 0",CatanGame.skin);

        tradeImprovementLabel = new Label("", CatanGame.skin);
        scienceImprovementLabel = new Label("", CatanGame.skin);
        politicsImprovementLabel = new Label("", CatanGame.skin);

        tradeImprovement.add(new Label("Trade", CatanGame.skin)).row();
        scienceImprovement.add(new Label("Science", CatanGame.skin)).row();
        politicsImprovement.add(new Label("Politics", CatanGame.skin)).row();

//        tradeImprovement.add(tradeImprovementLabel);
//        scienceImprovement.add(scienceImprovementLabel);
//        politicsImprovement.add(politicsImprovement);

        tradeImprovement.add(tradeImprovementLevel).row();
        scienceImprovement.add(scienceImprovementLevel).row();
        politicsImprovement.add(politicsImprovementLevel).row();

        tradeImprovement.setBackground(CatanGame.skin.newDrawable("white", Color.GOLD));
        scienceImprovement.setBackground(CatanGame.skin.newDrawable("white", Color.FOREST));
        politicsImprovement.setBackground(CatanGame.skin.newDrawable("white", Color.ROYAL));

        improveTrade = new TextButton("Improve", CatanGame.skin);
        improveTrade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!aSessionController.requestTradeImprovement(aSessionController.getPlayerColor())) {
                    addGameMessage("Not enough resources to improve your cities in the area of trade");
                    return;
                } else {
                    //aSessionController.getCurrentPlayer().getCityImprovements().upgradeTradeLevel();
                    aSessionController.tradeCityImprovement(aSessionController.getCurrentPlayer().getColor());
                    updateTradeImprovements(aSessionController.getCurrentPlayer().getCityImprovements().getTradeLevel());
                    updateResourceBar(aSessionController.getCurrentPlayer().getResources());
                    interractionDone();
                }
            }
        });

        improvePolitics = new TextButton("Improve", CatanGame.skin);
        improvePolitics.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!aSessionController.requestPoliticsImprovement(aSessionController.getPlayerColor())) {
                    addGameMessage("Not enough resources to improve your cities in the area of politics");
                    return;
                } else {
                    //aSessionController.getCurrentPlayer().getCityImprovements().upgradePoliticsLevel();
                    aSessionController.politicsCityImprovement(aSessionController.getCurrentPlayer().getColor());
                    updatePoliticsImprovements(aSessionController.getCurrentPlayer().getCityImprovements().getPoliticsLevel());
                    updateResourceBar(aSessionController.getCurrentPlayer().getResources());
                    interractionDone();
                }
            }
        });

        improveScience = new TextButton("Improve", CatanGame.skin);
        improveScience.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!aSessionController.requestScienceImprovement(aSessionController.getPlayerColor())) {
                    addGameMessage("Not enough resources to improve your cities in the area of science");
                    return;
                } else {
                    //aSessionController.getCurrentPlayer().getCityImprovements().upgradeScienceLevel();
                    aSessionController.scienceCityImprovement(aSessionController.getCurrentPlayer().getColor());
                    updateScienceImprovements(aSessionController.getCurrentPlayer().getCityImprovements().getScienceLevel());
                    updateResourceBar(aSessionController.getCurrentPlayer().getResources());
                    interractionDone();
                }

            }
        });

        tradeImprovement.add(improveTrade);
        scienceImprovement.add(improveScience);
        politicsImprovement.add(improvePolitics);

        developmentFlipChart.add(tradeImprovement).width(80).height(100).pad(5);
        developmentFlipChart.add(scienceImprovement).width(80).height(100).pad(5);
        developmentFlipChart.add(politicsImprovement).width(80).height(100).pad(5);

        developmentFlipChart.setBackground("resTableBackground");
        developmentFlipChart.pad(5);
        developmentFlipChart.pack();

    }

    void updateBootOwner(String username) {
        bootOwnerLabel.setText("Boot Owner: " + username);
    }

    void addKnight(KnightActor knightActor) {
        knights.add(knightActor);
        gamePiecesStage.addActor(knightActor);
    }

    /**
     * Add a message to the game log
     *
     * @param message message
     */
    public void addGameMessage(String message) {
        final Table table = (Table) gameLog.getChildren().get(0);
        table.add(new Label(message, CatanGame.skin)).left();
        table.row();
        gameLog.layout();
        gameLog.scrollTo(0, 0, 0, 0);
    }

    public Pair<Integer, Integer> getBoardOrigin() {
        return boardOrigin;
    }
}
