package com.mygdx.catan.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.ui.KnightActor;

/**
 * Helper class that returns PolygonRegions representing various game pieces
 * such as hexes, cities, roads, etc.
 */
public class GamePieces {

    private static GamePieces instance;

    /**
     * Array of regions representing the active knight states for each level.
     * i = 0 -> Basic
     * i = 1 -> Strong
     * i = 2 -> Mighty
     */
    public final TextureRegionDrawable[] knightActive;

    /**
     * Array of regions representing the inactive knight states for each level.
     * i = 0 -> Basic
     * i = 1 -> Strong
     * i = 2 -> Mighty
     */
    public final TextureRegionDrawable[] knightInactive;

    public final TextureAtlas.AtlasRegion knightBg;

    private final PolygonRegion sea, fishery, hills, forest, mountains, pasture, fields, gold;

    private final PolygonRegion robber;

    private final TextureAtlas.AtlasRegion cityWall;

    private Texture aSeaTextureSolid;
    private Texture aHillsTextureSolid;
    private Texture aForestTextureSolid;
    private Texture aMountainTextureSolid;
    private Texture aPastureTextureSolid;
    private Texture aFieldsTextureSolid;

    private Texture aOrangeTextureSolid;
    private Texture aRedTextureSolid;
    private Texture aWhiteTextureSolid;
    private Texture aBlueTextureSolid;
    private Texture aYellowTextureSolid;

    /** Create a new instance of this helper class */
    private GamePieces() {
        // Creating the color filling for hexagons
        aSeaTextureSolid = setupTextureSolid(Color.TEAL);
        aHillsTextureSolid = setupTextureSolid(CatanGame.skin.getColor("brick"));
        aForestTextureSolid = setupTextureSolid(CatanGame.skin.getColor("wood"));
        aMountainTextureSolid = setupTextureSolid(CatanGame.skin.getColor("ore"));
        aPastureTextureSolid = setupTextureSolid(CatanGame.skin.getColor("wool"));
        aFieldsTextureSolid = setupTextureSolid(CatanGame.skin.getColor("grain"));

        //Creating the color filling for player pieces
        aOrangeTextureSolid = setupTextureSolid(Color.ORANGE);
        aRedTextureSolid = setupTextureSolid(Color.RED);
        aWhiteTextureSolid = setupTextureSolid(Color.WHITE);
        aBlueTextureSolid = setupTextureSolid(Color.BLUE);
        aYellowTextureSolid = setupTextureSolid(Color.YELLOW);

        AssetManager assetManager = new AssetManager();
        assetManager.load("gamepieces/gamepieces.atlas", TextureAtlas.class);
        assetManager.finishLoading();
        TextureAtlas gamePieces = assetManager.get("gamepieces/gamepieces.atlas", TextureAtlas.class);
        knightActive = new TextureRegionDrawable[3];
        knightInactive = new TextureRegionDrawable[3];
        knightActive[0] = new TextureRegionDrawable(gamePieces.findRegion("k_basic"));
        knightActive[1] = new TextureRegionDrawable(gamePieces.findRegion("k_strong"));
        knightActive[2] = new TextureRegionDrawable(gamePieces.findRegion("k_mighty"));
        knightInactive[0] = new TextureRegionDrawable(gamePieces.findRegion("k_basic_inactive"));
        knightInactive[1] = new TextureRegionDrawable(gamePieces.findRegion("k_strong_inactive"));
        knightInactive[2] = new TextureRegionDrawable(gamePieces.findRegion("k_mighty_inactive"));
        knightBg = gamePieces.findRegion("bg_knight");

        cityWall = gamePieces.findRegion("cityWall");

        PolygonRegionLoader polygonRegionLoader = new PolygonRegionLoader();

        sea = polygonRegionLoader.load(CatanGame.skin.getRegion("sea"), Gdx.files.internal("hex.psh"));
        fishery = polygonRegionLoader.load(CatanGame.skin.getRegion("sea"), Gdx.files.internal("hex.psh"));
        hills = polygonRegionLoader.load(CatanGame.skin.getRegion("hills"), Gdx.files.internal("hex.psh"));
        forest = polygonRegionLoader.load(CatanGame.skin.getRegion("forest"), Gdx.files.internal("hex.psh"));
        mountains = polygonRegionLoader.load(CatanGame.skin.getRegion("mountains"), Gdx.files.internal("hex.psh"));
        pasture = polygonRegionLoader.load(CatanGame.skin.getRegion("pasture"), Gdx.files.internal("hex.psh"));
        fields = polygonRegionLoader.load(CatanGame.skin.getRegion("fields"), Gdx.files.internal("hex.psh"));
        gold = polygonRegionLoader.load(CatanGame.skin.getRegion("gold"), Gdx.files.internal("hex.psh"));

        robber = polygonRegionLoader.load(CatanGame.skin.getRegion("robber"), Gdx.files.internal("robber.psh"));
    }

    public static GamePieces getInstance() {
        if (instance == null)
            instance = new GamePieces();
        return instance;
    }

    /**
     * Creates a hexagon according to given position and length
     *
     * @param xPos    x position of hexagon center
     * @param yPos    y position of hexagon center
     * @param xOrigin x coordinate of the bottom left corner of the hex's enclosing rectangle
     * @param yOrigin y coordinate of the bottom left corner of the hex's enclosing rectangle
     */
    PolygonSprite createHexagon(int xPos, int yPos, int xOrigin, int yOrigin, TerrainKind pTerrainKind) {

        PolygonRegion region = sea;

        // sets region to relevant texture according to ResourceKind
        switch (pTerrainKind) {
            case HILLS:
                region = hills;
                break;
            case FIELDS:
                region = fields;
                break;
            case FOREST:
                region = forest;
                break;
            case MOUNTAINS:
                region = mountains;
                break;
            case PASTURE:
                region = pasture;
                break;
            case SEA:
                break;
            case GOLDFIELD:
                region = gold;
                break;
            case BIG_FISHERY:
                region = fishery;
                break;
            case SMALL_FISHERY:
                region = fishery;
                break;
            default:
                break;
        }

        final PolygonSprite polygonSprite = new PolygonSprite(region);
        polygonSprite.setPosition(xOrigin + xPos - region.getRegion().getRegionWidth() / 2f,
                yOrigin + yPos - region.getRegion().getRegionHeight() / 2f);
        polygonSprite.setScale(2.0f * SessionScreen.LENGTH / region.getRegion().getRegionHeight());
        return polygonSprite;
    }

    /** Placeholder method for creating progress cards */
    Image createProgressCard(ProgressCardType type, float onBoardWidth, float onBoardHeight) {
        // PlaceHolder image

        // TODO: make this connect to a progress card atlas 
        Image progressCardImage = new Image(aSeaTextureSolid);

        // Scale down the image to on board size
        progressCardImage.setSize(onBoardWidth, onBoardHeight);

        return progressCardImage;
    }

    PolygonRegion createHighlightedIntersection(int xCor, int yCor, int base, int length, int pieceBase, PlayerColor color) {
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

        int highlightLength = pieceBase;
        int highlightBase = (int) Math.sqrt(Math.pow(highlightLength, 2) - Math.pow(highlightLength / 2, 2));

        float xPos = +(xCor * base);
        float yPos = -(yCor * length / 2);

        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        xPos - highlightBase, yPos - highlightLength / 2,             // Vertex 0                4
                        xPos, yPos - highlightLength,                                 // Vertex 1           5         3
                        xPos + highlightBase, yPos - highlightLength / 2,             // Vertex 2
                        xPos + highlightBase, yPos + highlightLength / 2,             // Vertex 3           0         2
                        xPos, yPos + highlightLength,                                 // Vertex 4                1
                        xPos - highlightBase, yPos + highlightLength / 2              // Vertex 5
                }, new short[]{
                0, 1, 4,         // Sets up triangulation according to vertices above
                0, 4, 5,
                1, 2, 3,
                1, 3, 4
        });
    }

    PolygonRegion createMerchant(int xCor, int yCor, int xOff, int yOff, int piecebase) {
        Texture merchantTexture = setupTextureSolid(Color.valueOf("660066"));

        float xPos = +(xCor * xOff);
        float yPos = -(yCor * yOff);

        return new PolygonRegion(new TextureRegion(merchantTexture),
                new float[]{
                        xPos - piecebase / 2f, yPos + piecebase / 2f,              // Vertex 0               2
                        xPos + piecebase / 2f, yPos + piecebase / 2f,              // Vertex 1
                        xPos, yPos + 2 * piecebase                                 // Vertex 2             0   1

                }, new short[]{
                0, 1, 2,
        });
    }

    PolygonSprite createRobber() {
        final PolygonSprite polygonSprite = new PolygonSprite(robber);
        polygonSprite.setScale(0.4f);
        return polygonSprite;
    }

    /**
     * Create a new Knight, represented as an Image.
     *
     * @param knight the knight associated with this actor
     */
    KnightActor createKnight(Knight knight) {
        KnightActor knightActor = new KnightActor(knight);

        // Scale down the image
        final float knightScale = 1 / 8f;
        knightActor.setSize(knightScale * knightActor.getWidth(), knightScale * knightActor.getHeight());

        // Place the origin in the center of the image to make it easier to draw
        knightActor.setOrigin(knightActor.getWidth() / 2f, knightActor.getHeight() / 2f);

        return knightActor;
    }

    /**
     * Create an image used to highlight valid positions for a knight.
     *
     * @param localPlayer the local player, always
     */
    public Image createKnightPosition(Player localPlayer) {
        Image knightPos = new Image(knightBg);

        // The highlighted position should be a bit transparent
        float alpha = 0.75f;

        // It should also take the color of the local player
        switch (localPlayer.getColor()) {
            case WHITE:
                knightPos.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
                break;
            case BLUE:
                knightPos.setColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, alpha);
                break;
            case RED:
                knightPos.setColor(Color.RED.r, Color.RED.g, Color.RED.b, alpha);
                break;
            case ORANGE:
                knightPos.setColor(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, alpha);
                break;
            case YELLOW:
                knightPos.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, alpha);
                break;
        }

        // Scale down the image
        final float knightScale = 1 / 8f;
        knightPos.setSize(knightScale * knightPos.getWidth(), knightScale * knightPos.getHeight());

        // Place the origin in the center of the image to make it easier to draw
        knightPos.setOrigin(knightPos.getWidth() / 2f, knightPos.getHeight() / 2f);

        return knightPos;
    }

    /**
     * Creates a settlement according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    PolygonRegion createSettlement(int xCor, int yCor, int base, int length, int pieceBase, PlayerColor color) {
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

        float xPos = (xCor * base);
        float yPos = -(yCor * length / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        (float) (xPos - pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 0                
                        (float) (xPos + pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 1           4        
                        (float) (xPos + pieceBase / 2.0), (float) (yPos + pieceBase / 4.0),        // Vertex 2		   3    2
                        (float) (xPos - pieceBase / 2.0), (float) (yPos + pieceBase / 4.0),        // Vertex 3         0    1     
                        xPos, (float) (yPos + pieceBase * 0.7),                                    // Vertex 4
                }, new short[]{
                0, 1, 2,         // Sets up triangulation according to vertices above
                0, 2, 3,
                3, 2, 4
        });
    }

    /**
     * Creates a city according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    PolygonRegion createCity(int xCor, int yCor, int base, int length, int pieceBase, PlayerColor color) {
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

        float xPos = +(xCor * base);
        float yPos = -(yCor * length / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{      // Six vertices
                        (float) (xPos - pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 0           3
                        (float) (xPos), (float) (yPos + pieceBase / 3.0),                          // Vertex 1         2    1  
                        (float) (xPos - pieceBase / 2.0), (float) (yPos + pieceBase / 3.0),        // Vertex 2              6  4 
                        (float) (xPos - pieceBase / 4.0), (float) (yPos + pieceBase * 0.7),        // Vertex 3         0       5 
                        (float) (xPos + pieceBase / 2.0), (float) (yPos),                          // Vertex 4
                        (float) (xPos + pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 5
                        (float) (xPos), (float) (yPos),                                            // Vertex 6

                }, new short[]{
                0, 5, 4,         // Sets up triangulation according to vertices above
                0, 4, 6,
                0, 6, 2,
                6, 1, 2,
                2, 1, 3
        });
    }

    /** Create a city wall image with the provided color */
    Image createCityWall(PlayerColor color) {
        // Create the image from the texture region
        Image cityWallImage = new Image(cityWall);

        // Set the color
        switch (color) {
            case WHITE:
                cityWallImage.setColor(Color.WHITE);
                break;
            case BLUE:
                cityWallImage.setColor(Color.BLUE);
                break;
            case RED:
                cityWallImage.setColor(Color.RED);
                break;
            case ORANGE:
                cityWallImage.setColor(Color.ORANGE);
                break;
            case YELLOW:
                cityWallImage.setColor(Color.YELLOW);
                break;
        }

        // Scale down the image
        final float imageScale = 1 / 4f;
        cityWallImage.setSize(imageScale * cityWallImage.getWidth(), imageScale * cityWallImage.getHeight());

        // Place the origin in the center of the image to make it easier to draw
        cityWallImage.setOrigin(cityWallImage.getWidth() / 2f, cityWallImage.getHeight() / 2f);

        return cityWallImage;
    }

    /**
     * Creates a metropolis according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    PolygonRegion createMetropolis(int xCor, int yCor, int base, int length, int pieceBase, VillageKind kind) {
        Texture aTexture = aSeaTextureSolid;

        switch (kind) {
        case POLITICS_METROPOLIS:
            aTexture = setupTextureSolid(Color.ROYAL);
            break;
        case SCIENCE_METROPOLIS:
            aTexture = setupTextureSolid(Color.FOREST);
            break;
        case TRADE_METROPOLIS:
            aTexture = setupTextureSolid(Color.GOLD);
            break;
        default:
            break;
        }

        float xPos = (xCor * base);
        float yPos = -(yCor * length / 2);


        // all player pieces will have 0 vertex at xPos - length / 2, yPos - length / 2, where length is a value that depends on hex side length
        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{
                        (float) (xPos - pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 0
                        (float) (xPos/* + pieceBase / 2.0*/), (float) (yPos - pieceBase / 2.0),    // Vertex 1
                        (float) (xPos), (float) (yPos + pieceBase / 10.0),                         // Vertex 2
                        (float) (xPos + pieceBase / 2.0), (float) (yPos + pieceBase / 10.0),       // Vertex 3        12      7
                        (float) (xPos + pieceBase / 2.0), (float) (yPos - pieceBase / 2.0),        // Vertex 4      13  11   8  6
                        (float) (xPos + pieceBase), (float) (yPos - pieceBase / 2.0),              // Vertex 5
                        (float) (xPos + pieceBase), (float) (yPos + pieceBase / 2.0),              // Vertex 6          10   9  
                        (float) (xPos + 3 * pieceBase / 4.0), (float) (yPos + pieceBase * 0.8),    // Vertex 7          2    3
                        (float) (xPos + pieceBase / 2.0), (float) (yPos + pieceBase / 2.0),        // Vertex 8       
                        (float) (xPos + pieceBase / 2.0), (float) (yPos + pieceBase / 3.0),        // Vertex 9       0  1    4  5
                        (float) (xPos), (float) (yPos + pieceBase / 3.0),                          // Vertex 10
                        (float) (xPos /*+ pieceBase / 2.0*/), (float) (yPos + pieceBase / 2.0),    // Vertex 11
                        (float) (xPos - pieceBase / 4.0), (float) (yPos + (pieceBase * 0.8)),      // Vertex 12
                        (float) (xPos - pieceBase / 2.0), (float) (yPos + pieceBase / 2.0),        // Vertex 13

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
    }

    /**
     * Creates a road according to given positions. Assumes the coordinates correspond to adjacent intersections
     *
     * @param x1    x coordinate of game piece first endpoint
     * @param y1    y coordinate of game piece first endpoint
     * @param x2    x coordinate of game piece second endpoint
     * @param y2    y coordinate of game piece second endpoint
     * @param color of game piece
     */
    PolygonRegion createRoad(int x1, int y1, int x2, int y2, int base, int length, int pieceBase, PlayerColor color) {
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

        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);

        // Sets the identifying coordinates as middle between the two end points
        vm[0] = (float) (xMin + (xMax - xMin) / 2.0);
        vm[1] = (float) (yMin + (yMax - yMin) / 2.0);

        // Determines which direction the EdgeUnit will be facing, and gives appropriate vertex values
        if (x1 == x2) {

            v0[0] = (float) (((x1 * base)) - pieceBase / 4.0);
            v0[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

            v1[0] = (float) (((x1 * base)) + pieceBase / 4.0);
            v1[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

            v2[0] = (float) (((x1 * base)) + pieceBase / 4.0);
            v2[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

            v3[0] = (float) ((+(x1 * base)) - pieceBase / 4.0);
            v3[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

        } else {
            if ((Math.min(x1, x2) == x1 && Math.max(y1, y2) == y1) ||
                    (Math.min(x1, x2) == x2 && Math.max(y1, y2) == y2)) {

                v0[0] = (float) ((+(xMin * base)) + pieceBase / 2.0);
                v0[1] = (float) ((-(yMax * length / 2)) + pieceBase / 15.0);

                v1[0] = (float) ((+(xMin * base)) + pieceBase / 4.0);
                v1[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

                v2[0] = (float) ((+(xMax * base)) - pieceBase / 2.0);
                v2[1] = (float) ((-(yMin * length / 2)) - pieceBase / 15.0);

                v3[0] = (float) ((+(xMax * base)) - pieceBase / 4.0);
                v3[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

            } else {

                v0[0] = (float) ((+(xMin * base)) + pieceBase / 4.0);
                v0[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

                v1[0] = (float) ((+(xMin * base)) + pieceBase / 2.0);
                v1[1] = (float) ((-(yMin * length / 2)) - pieceBase / 15.0);

                v2[0] = (float) ((+(xMax * base)) - pieceBase / 4.0);
                v2[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

                v3[0] = (float) ((+(xMax * base)) - pieceBase / 2.0);
                v3[1] = (float) ((-(yMax * length / 2)) + pieceBase / 15.0);

            }
        }

        return new PolygonRegion(new TextureRegion(aTexture),
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
    }


    PolygonRegion createHarbour(int x, int y, int base, int length, HarbourKind kind) {
        Texture aTexture;

        switch (kind) {
            case SPECIAL_BRICK:
                aTexture = aHillsTextureSolid;
                break;
            case SPECIAL_GRAIN:
                aTexture = aFieldsTextureSolid;
                break;
            case SPECIAL_ORE:
                aTexture = aMountainTextureSolid;
                break;
            case SPECIAL_WOOD:
                aTexture = aForestTextureSolid;
                break;
            case SPECIAL_WOOL:
                aTexture = aPastureTextureSolid;
                break;
            default:
                aTexture = setupTextureSolid(Color.WHITE);
        }

        int dir = GameRules.getGameRulesInstance().getDefaultHarbourDirection(x, y, kind);
        int diry = 1;

        int Xoffset, Yoffset;
        if (dir == 0) {
            diry = 0;
            Xoffset = GameRules.getGameRulesInstance().getxHarbourOff(x, y, base);
            Yoffset = GameRules.getGameRulesInstance().getyHarbourOff(x, y, length);
            dir = 1;
        } else {
            Xoffset = GameRules.getGameRulesInstance().getxHarbourOff(x, y, base);
            Yoffset = GameRules.getGameRulesInstance().getyHarbourOff(x, y, length / 2);
        }


        float[] v0 = new float[2], v1 = new float[2];
        v0[0] = x * base;
        v0[1] = -y * length / 2;
        v1[0] = x * base + Xoffset;
        v1[1] = -(y * length / 2 + Yoffset);

        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{
                        v0[0], v0[1],                                                // Vertex 0                2
                        v1[0] + dir * length / 8, v1[1] + diry * length / 10,        // Vertex 1                1          (with rotation)
                        v1[0] - dir * length / 8, v1[1] - diry * length / 10,        // Vertex 2         0


                }, new short[]{
                0, 1, 2         // Sets up triangulation according to vertices above
        });

    }


    /**
     * Creates a ship according to given positions. Assumes the coordinates correspond to adjacent intersections
     *
     * @param x1    x coordinate of game piece first endpoint
     * @param y1    y coordinate of game piece first endpoint
     * @param x2    x coordinate of game piece second endpoint
     * @param y2    y coordinate of game piece second endpoint
     * @param color of game piece
     */
    PolygonRegion createShip(int x1, int y1, int x2, int y2, int base, int length, int pieceBase, PlayerColor color) {
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

        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);

        // Sets the identifying coordinates as middle between the two end points
        vm[0] = (float) (xMin + (xMax - xMin) / 2.0);
        vm[1] = (float) (yMin + (yMax - yMin) / 2.0);

        // Determines which direction the EdgeUnit will be facing, and gives appropriate vertex values
        if (x1 == x2) {

            v0[0] = (float) ((+(x1 * base)) - pieceBase / 4.0);
            v0[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

            v1[0] = (float) ((+(x1 * base)) + pieceBase / 4.0);
            v1[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

            v2[0] = (float) ((+(x1 * base)) + pieceBase / 4.0);
            v2[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

            v3[0] = (float) ((+(x1 * base)) - pieceBase / 4.0);
            v3[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

            v4[0] = (float) (+(vm[0] * base + pieceBase * 1.1));
            v4[1] = (float) (-(vm[1] * (length / 2)));

            v5[0] = (float) ((+(x1 * base)) + pieceBase / 4.0);
            v5[1] = (float) ((-(yMin * length / 2)) - pieceBase);

            v6[0] = (float) ((+(x1 * base)) + pieceBase / 4.0);
            v6[1] = (float) ((-(yMax * length / 2)) + pieceBase * 0.8);

        } else {
            if ((Math.min(x1, x2) == x1 && Math.max(y1, y2) == y1) ||
                    (Math.min(x1, x2) == x2 && Math.max(y1, y2) == y2)) {

                v0[0] = (float) ((+(xMin * base)) + pieceBase / 2.0);
                v0[1] = (float) ((-(yMax * length / 2)) + pieceBase / 15.0);

                v1[0] = (float) ((+(xMin * base)) + pieceBase / 4.0);
                v1[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

                v2[0] = (float) ((+(xMax * base)) - pieceBase / 2.0);
                v2[1] = (float) ((-(yMin * length / 2)) - pieceBase / 15.0);

                v3[0] = (float) ((+(xMax * base)) - pieceBase / 4.0);
                v3[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

                v4[0] = (float) (+(vm[0] * base - pieceBase / 3.0));
                v4[1] = (float) (-(vm[1] * (length / 2) - pieceBase));

                v5[0] = (float) ((+(xMin * base)) + pieceBase * 0.5);
                v5[1] = (float) ((-(yMax * length / 2)) + pieceBase * 0.4);

                v6[0] = (float) ((+(xMax * base)) - pieceBase * 0.8);
                v6[1] = (float) ((-(yMin * length / 2)) - pieceBase * 0.5);

            } else {

                v0[0] = (float) ((+(xMin * base)) + pieceBase / 4.0);
                v0[1] = (float) ((-(yMin * length / 2)) - pieceBase / 2.0);

                v1[0] = (float) ((+(xMin * base)) + pieceBase / 2.0);
                v1[1] = (float) ((-(yMin * length / 2)) - pieceBase / 15.0);

                v2[0] = (float) ((+(xMax * base)) - pieceBase / 4.0);
                v2[1] = (float) ((-(yMax * length / 2)) + pieceBase / 2.0);

                v3[0] = (float) ((+(xMax * base)) - pieceBase / 2.0);
                v3[1] = (float) ((-(yMax * length / 2)) + pieceBase / 15.0);

                v4[0] = (float) (+(vm[0] * base + pieceBase / 3.0));
                v4[1] = (float) (-(vm[1] * (length / 2) - pieceBase));

                v5[0] = (float) ((+(xMin * base)) + pieceBase * 0.8);
                v5[1] = (float) ((-(yMin * length / 2)) - pieceBase * 0.5);

                v6[0] = (float) ((+(xMax * base)) - pieceBase * 0.5);
                v6[1] = (float) ((-(yMax * length / 2)) + pieceBase * 0.4);
            }
        }

        return new PolygonRegion(new TextureRegion(aTexture),
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
    }


    private Texture setupTextureSolid(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color); // DE is red, AD is green and BE is blue.
        pix.fill();
        return new Texture(pix);
    }
}
