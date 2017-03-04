package com.mygdx.catan.session;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.TerrainKind;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that returns PolygonRegions representing various game pieces
 * such as hexes, cities, roads, etc.
 */
class GamePieces {

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

    private Texture aSeaTextureSolid;
    private Texture aDesertTextureSolid;
    private Texture aHillsTextureSolid;
    private Texture aForestTextureSolid;
    private Texture aMountainTextureSolid;
    private Texture aPastureTextureSolid;
    private Texture aFieldsTextureSolid;
    private Texture aGoldfieldTextureSolid;

    private Texture aOrangeTextureSolid;
    private Texture aRedTextureSolid;
    private Texture aWhiteTextureSolid;
    private Texture aBlueTextureSolid;
    private Texture aYellowTextureSolid;

    /** Create a new instance of this helper class */
    GamePieces() {
        // Creating the color filling for hexagons
        aSeaTextureSolid = setupTextureSolid(Color.TEAL);
        aDesertTextureSolid = setupTextureSolid(Color.GOLDENROD);
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
    }

    /**
     * Creates a hexagon according to given position and length
     *
     * @param xPos   x position of hexagon center
     * @param yPos   y position of hexagon center
     * @param length length of the side of the hexagon
     */
    PolygonRegion createHexagon(int xPos, int yPos, int length, int base, TerrainKind pTerrainKind) {

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

        return new PolygonRegion(new TextureRegion(aTexture),
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

    /**
     * Creates a metropolis according to given position
     *
     * @param xCor x coordinate of game piece center
     * @param yCor y coordinate of game piece center
     */
    PolygonRegion createMetropolis(int xCor, int yCor, int base, int length, int pieceBase, PlayerColor color) {
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
        Texture aTexture = aSeaTextureSolid;
        
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
            Yoffset = GameRules.getGameRulesInstance().getyHarbourOff(x, y, length/2);
        }
        
       
        
        float[] v0 = new float[2], v1 = new float[2];
        v0[0] = x*base;
        v0[1] = -y*length/2;
        v1[0] = x*base + Xoffset;
        v1[1] = -(y*length/2 + Yoffset);
        
        return new PolygonRegion(new TextureRegion(aTexture),
                new float[]{ 
                        v0[0], v0[1],                                        // Vertex 0                2
                        v1[0] + dir*length/8, v1[1] + diry*length/10,        // Vertex 1                1          (with rotation)  
                        v1[0] - dir*length/8, v1[1] - diry*length/10,        // Vertex 2         0                
                        


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
