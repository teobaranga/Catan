package com.mygdx.catan;

import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.Hex;

import java.util.EnumMap;
import java.util.HashMap;

/**
 * Singleton object describing the game rules
 * */
public class GameRules {

	private ResourceMap settlementCost = new ResourceMap();
	private ResourceMap roadCost = new ResourceMap();
	private ResourceMap shipCost = new ResourceMap();
	private ResourceMap cityCost = new ResourceMap();
	private ResourceMap cityWallCost = new ResourceMap();

	private EnumMap<ProgressCardKind, Integer> progressCardOccurences = new EnumMap<ProgressCardKind, Integer>(ProgressCardKind.class);
	private HashMap<Integer,TerrainKind> defaultTerrainKindMap = new HashMap<Integer,TerrainKind>();
	private HashMap<Integer,Integer> defaultDiceNumberMap = new HashMap<Integer,Integer>();
	private HashMap<Integer,HarbourKind> defaultHarbourMap = new HashMap<Integer,HarbourKind>();

	private int vpToWin = 13;
	private int settlementVp = 1;
	private int cityVp = 2;
	private int metropolisVp = 2;

	private final int SIZE = 7;

	// private int genericTradeRatio = 4;
	// private int specialTradeRation = 3;

	private static GameRules aGameRules = new GameRules();

	private GameRules() {
		initializeProgressCardOccurences();
		setupDefaultTerrainMap();
		setupDefaultDiceMap();
		setupDefaultHarbourMap();

		setupSettlementCost();
		setupRoadCost();
		setupShipCost();
		setupCityCost();
		setupCityWallCost();
	}

	/**
	 * hardcodes the standard cost for things that can be built throughout the game
	 */
	private void setupSettlementCost(){
		settlementCost.put(ResourceKind.WOOL, 1);
		settlementCost.put(ResourceKind.WOOD, 1);
		settlementCost.put(ResourceKind.BRICK, 1);
		settlementCost.put(ResourceKind.GRAIN, 1);
	}

	private void setupRoadCost(){
		roadCost.put(ResourceKind.BRICK, 1);
		roadCost.put(ResourceKind.WOOD, 1);
	}

	private void setupShipCost(){
		shipCost.put(ResourceKind.WOOD, 1);
		shipCost.put(ResourceKind.WOOL, 1);
	}

	private void setupCityCost(){
		cityCost.put(ResourceKind.GRAIN, 2);
		cityCost.put(ResourceKind.ORE, 3);
	}

	private void setupCityWallCost(){
		cityWallCost.put(ResourceKind.BRICK, 2);
	}

	/**
	 * hardcodes the default terrainKind map (each integer represents the hashcode of the CoordinatePair with coordinates x and y (assumes size <= 7)
	 * */
	private void setupDefaultTerrainMap() {
		defaultTerrainKindMap.put(getHashCodeofPair(-3,-3), TerrainKind.FIELDS);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,-3), TerrainKind.DESERT);
		defaultTerrainKindMap.put(getHashCodeofPair(1,-3), TerrainKind.HILLS);
		defaultTerrainKindMap.put(getHashCodeofPair(3,-3), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-4,-2), TerrainKind.FOREST);
		defaultTerrainKindMap.put(getHashCodeofPair(-2,-2), TerrainKind.MOUNTAINS);
		defaultTerrainKindMap.put(getHashCodeofPair(0,-2), TerrainKind.PASTURE);
		defaultTerrainKindMap.put(getHashCodeofPair(2,-2), TerrainKind.PASTURE);
		defaultTerrainKindMap.put(getHashCodeofPair(4,-2), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-5,-1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-3,-1), TerrainKind.HILLS);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,-1), TerrainKind.PASTURE);
		defaultTerrainKindMap.put(getHashCodeofPair(1,-1), TerrainKind.MOUNTAINS);
		defaultTerrainKindMap.put(getHashCodeofPair(3,-1), TerrainKind.FIELDS);
		defaultTerrainKindMap.put(getHashCodeofPair(5,-1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-6,0), TerrainKind.HILLS);
		defaultTerrainKindMap.put(getHashCodeofPair(-4,0), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-2,0), TerrainKind.FOREST);
		defaultTerrainKindMap.put(getHashCodeofPair(0,0), TerrainKind.FOREST);
		defaultTerrainKindMap.put(getHashCodeofPair(2,0), TerrainKind.PASTURE);
		defaultTerrainKindMap.put(getHashCodeofPair(4,0), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(6,0), TerrainKind.HILLS);
		defaultTerrainKindMap.put(getHashCodeofPair(-5,1), TerrainKind.MOUNTAINS);
		defaultTerrainKindMap.put(getHashCodeofPair(-3,1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(1,1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(3,1), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(5,1), TerrainKind.GOLDFIELD);
		defaultTerrainKindMap.put(getHashCodeofPair(-4,2), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-2,2), TerrainKind.GOLDFIELD);
		defaultTerrainKindMap.put(getHashCodeofPair(0,2), TerrainKind.FIELDS);
		defaultTerrainKindMap.put(getHashCodeofPair(2,2), TerrainKind.PASTURE);
		defaultTerrainKindMap.put(getHashCodeofPair(4,2), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-3,3), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,3), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(1,3), TerrainKind.MOUNTAINS);
		defaultTerrainKindMap.put(getHashCodeofPair(3,3), TerrainKind.SEA);
	}

	/**
	 * hardcodes default dice number map (keys exactly as above) (assumes size <= 7)
	 * */
	private void setupDefaultDiceMap() {
		defaultDiceNumberMap.put(getHashCodeofPair(-3,-3), 6);
		defaultDiceNumberMap.put(getHashCodeofPair(-1,-3), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(1,-3), 8);
		defaultDiceNumberMap.put(getHashCodeofPair(3,-3), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-4,-2), 5);
		defaultDiceNumberMap.put(getHashCodeofPair(-2,-2), 3);
		defaultDiceNumberMap.put(getHashCodeofPair(0,-2), 10);
		defaultDiceNumberMap.put(getHashCodeofPair(2,-2), 2);
		defaultDiceNumberMap.put(getHashCodeofPair(4,-2), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-5,-1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-3,-1), 11);
		defaultDiceNumberMap.put(getHashCodeofPair(-1,-1), 9);
		defaultDiceNumberMap.put(getHashCodeofPair(1,-1), 5);
		defaultDiceNumberMap.put(getHashCodeofPair(3,-1), 4);
		defaultDiceNumberMap.put(getHashCodeofPair(5,-1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-6,0), 10);
		defaultDiceNumberMap.put(getHashCodeofPair(-4,0), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-2,0), 8);
		defaultDiceNumberMap.put(getHashCodeofPair(0,0), 10);
		defaultDiceNumberMap.put(getHashCodeofPair(2,0), 6);
		defaultDiceNumberMap.put(getHashCodeofPair(4,0), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(6,0), 12);
		defaultDiceNumberMap.put(getHashCodeofPair(-5,1), 8);
		defaultDiceNumberMap.put(getHashCodeofPair(-3,1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-1,1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(1,1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(3,1), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(5,1), 5);
		defaultDiceNumberMap.put(getHashCodeofPair(-4,2), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-2,2), 4);
		defaultDiceNumberMap.put(getHashCodeofPair(0,2), 3);
		defaultDiceNumberMap.put(getHashCodeofPair(2,2), 4);
		defaultDiceNumberMap.put(getHashCodeofPair(4,2), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(-3,3),0);
		defaultDiceNumberMap.put(getHashCodeofPair(-1,3), 0);
		defaultDiceNumberMap.put(getHashCodeofPair(1,3), 9);
		defaultDiceNumberMap.put(getHashCodeofPair(3,3), 0);
	}

	/**
     * hardcodes default harbour map (keys exactly as above) (assumes size <= 7)
     * */
	private void setupDefaultHarbourMap() {
        defaultHarbourMap.put(getHashCodeofPair(-3,-11), HarbourKind.SPECIAL_BRICK);
        defaultHarbourMap.put(getHashCodeofPair(-2,-10), HarbourKind.SPECIAL_BRICK);
        defaultHarbourMap.put(getHashCodeofPair(0,-10), HarbourKind.GENERIC);
        defaultHarbourMap.put(getHashCodeofPair(1,-11), HarbourKind.GENERIC);
        defaultHarbourMap.put(getHashCodeofPair(2,-10), HarbourKind.SPECIAL_ORE);
        defaultHarbourMap.put(getHashCodeofPair(2,-8), HarbourKind.SPECIAL_ORE);
        defaultHarbourMap.put(getHashCodeofPair(-5,-7), HarbourKind.SPECIAL_WOOD);
        defaultHarbourMap.put(getHashCodeofPair(-4,-8), HarbourKind.SPECIAL_WOOD);
        defaultHarbourMap.put(getHashCodeofPair(-5,-5), HarbourKind.GENERIC);
        defaultHarbourMap.put(getHashCodeofPair(-4,-4), HarbourKind.GENERIC);
        defaultHarbourMap.put(getHashCodeofPair(3,-5), HarbourKind.SPECIAL_GRAIN);
        defaultHarbourMap.put(getHashCodeofPair(4,-4), HarbourKind.SPECIAL_GRAIN);
        defaultHarbourMap.put(getHashCodeofPair(-2,2), HarbourKind.SPECIAL_WOOL);
        defaultHarbourMap.put(getHashCodeofPair(-1,1), HarbourKind.SPECIAL_WOOL);
        defaultHarbourMap.put(getHashCodeofPair(1,1), HarbourKind.GENERIC);
        defaultHarbourMap.put(getHashCodeofPair(2,2), HarbourKind.GENERIC);
    }

	/**
	 * initializes progress card occurences
	 * */
	private void initializeProgressCardOccurences() {
		progressCardOccurences.put(ProgressCardKind.ALCHEMIST, 2);
		progressCardOccurences.put(ProgressCardKind.BISHOP, 2);
		progressCardOccurences.put(ProgressCardKind.COMMERCIALHARBOUR, 2);
		progressCardOccurences.put(ProgressCardKind.CONSTITUTION, 1);
		progressCardOccurences.put(ProgressCardKind.CRANE, 2);
		progressCardOccurences.put(ProgressCardKind.DESERTER, 2);
		progressCardOccurences.put(ProgressCardKind.DIPLOMAT, 2);
		progressCardOccurences.put(ProgressCardKind.ENGINEER, 1);
		progressCardOccurences.put(ProgressCardKind.INTRIGUE, 2);
		progressCardOccurences.put(ProgressCardKind.INVENTOR, 2);
		progressCardOccurences.put(ProgressCardKind.IRRIGATION, 2);
		progressCardOccurences.put(ProgressCardKind.MASTERMERCHANT, 2);
		progressCardOccurences.put(ProgressCardKind.MEDICINE, 2);
		progressCardOccurences.put(ProgressCardKind.MERCHANTFLEET, 2);
		progressCardOccurences.put(ProgressCardKind.MINING, 2);
		progressCardOccurences.put(ProgressCardKind.PRINTER, 1);
		progressCardOccurences.put(ProgressCardKind.RESOURCEMONOPOLY, 4);
		progressCardOccurences.put(ProgressCardKind.ROADBUILDING, 2);
		progressCardOccurences.put(ProgressCardKind.SABOTEUR, 2);
		progressCardOccurences.put(ProgressCardKind.SMITH, 2);
		progressCardOccurences.put(ProgressCardKind.SPY, 3);
		progressCardOccurences.put(ProgressCardKind.TRADEMONOPOLY, 2);
		progressCardOccurences.put(ProgressCardKind.WARLORD, 2);
		progressCardOccurences.put(ProgressCardKind.WEDDING, 2);
		progressCardOccurences.put(ProgressCardKind.MERCHANT, 6);
	}


	/**
	 * @param x left coordinate
	 * @param y right coordinate
	 * @return the hashCode of the CoordinatePair <x,y>
	 */
	private int getHashCodeofPair(int x, int y) {
		return (x + 30) * 10 + (y + 30);
	}

	/**
	 * @return singleton instance of GameRules
	 * */
	public static GameRules getGameRulesInstance() {
		return aGameRules;
	}

	public int getVillageVp(VillageKind pVillageKind) {
		switch (pVillageKind) {
		case CITY:
			return cityVp;
		case SCIENCE_METROPOLIS:
			return metropolisVp;
		case SETTLEMENT:
			return settlementVp;
		case TRADE_METROPOLIS:
			return metropolisVp;
		default:
			return 0;}
	}

	public int getVpToWin() {
		return vpToWin;
	}

	/**
	 * @param
	 * @return occurence of pKind in the progress card stack
	 * */
	public int getProgressCardOccurence(ProgressCardKind pKind) {
		return progressCardOccurences.get(pKind);
	}

	/**
	 * @return size of the gameboard: corresponds to the number of tiles on the longest diagonal
	 * */
	public int getSize() {
		return SIZE;
	}

	public HashMap<Integer,TerrainKind> getDefaultTerrainKindMap() {
		return defaultTerrainKindMap;
	}

	public HashMap<Integer,Integer> getDefaultDiceNumberMap() {
		return defaultDiceNumberMap;
	}

	public Integer getDiceNumber(Hex hex) {
	    return defaultDiceNumberMap.get(getHashCodeofPair(hex.getLeftCoordinate(),hex.getRightCoordinate()));
	}

	public HarbourKind getHarbourKind(int leftCoordinate, int rightCoordinate) {
	    if (defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate)) == null) {
	        return HarbourKind.NONE;
	    } else {
	        return defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate));
	    }
	}

	/**
	 * return the x offset for drawing the default harbour on <leftCoordinate,rightCoordinate> on the board
	 * */
	public int getxHarbourOff(int leftCoordinate, int rightCoordinate, int off) {
	    if (defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate)) != null) {
	        switch (defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate))) {
            case GENERIC:
                if (rightCoordinate == -11) {
                    return -off;
                } else if (rightCoordinate == -10) {
                    return 0;
                } else {
                    if (leftCoordinate == -5 || leftCoordinate == 1) {
                        return 0;
                    } else {
                        return -off;
                    }
                }
            case NONE:
                return 0;
            case SPECIAL_BRICK:
                if (leftCoordinate == -3) {
                    return off;
                } else {
                    return 0;
                }
            case SPECIAL_GRAIN:
                if (leftCoordinate == 3) {
                    return off;
                } else {
                    return 0;
                }
            case SPECIAL_ORE:
                return off/2;
            case SPECIAL_WOOD:
                if (leftCoordinate == -5) {
                    return 0;
                } else {
                    return -off;
                }
            case SPECIAL_WOOL:
                if (leftCoordinate == -2) {
                    return off;
                } else {
                    return 0;
                }
            default:
                return 0;

	        }
	    }

	    return 0;
	}

	/**
     * return the y offset for drawing the default harbour on <leftCoordinate,rightCoordinate> on the board
     * */
    public int getyHarbourOff(int leftCoordinate, int rightCoordinate, int off) {
        if (defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate)) != null) {
            switch (defaultHarbourMap.get(getHashCodeofPair(leftCoordinate, rightCoordinate))) {
            case GENERIC:
                if (rightCoordinate == -11) {
                    return 0;
                } else if (rightCoordinate == -10) {
                    return -off;
                } else {
                    if (leftCoordinate == -5 || leftCoordinate == 1) {
                        return off;
                    } else {
                        return 0;
                    }
                }
            case NONE:
                return 0;
            case SPECIAL_BRICK:
                if (leftCoordinate == -3) {
                    return 0;
                } else {
                    return -off;
                }
            case SPECIAL_GRAIN:
                if (leftCoordinate == 3) {
                    return 0;
                } else {
                    return -off;
                }
            case SPECIAL_ORE:
                if (rightCoordinate == -10) {
                    return off/2;
                } else {
                    return -off/2;
                }
            case SPECIAL_WOOD:
                if (leftCoordinate == -5) {
                    return -off;
                } else {
                    return 0;
                }
            case SPECIAL_WOOL:
                if (leftCoordinate == -2) {
                    return 0;
                } else {
                    return off;
                }
            default:
                return 0;

            }
        }

        return 0;
    }

    /**
     * highly arbitrary function that returns -1 if harbour needs to be turned on the gameboard, and 0 if it is vertical. Will only work for default board
     * @return -1, 0 or 1
     * */
    public int getDefaultHarbourDirection(int leftCoordinate, int rightCoordinate, HarbourKind kind) {
        int dir = 1;

        if (kind == HarbourKind.SPECIAL_WOOD
                || kind == HarbourKind.SPECIAL_WOOL
                || (leftCoordinate == 0 && rightCoordinate == -10)
                || (leftCoordinate == 1 && rightCoordinate == -11)) {
            dir = -1;
        }

        if (kind == HarbourKind.SPECIAL_ORE) {dir = 0;}

        return dir;
    }

	/**
	 * @return standard cost of things that can be built throughout the game
	 */
	public ResourceMap getSettlementCost() { return settlementCost; }
	public ResourceMap getRoadCost() { return roadCost; }
	public ResourceMap getShipCost() { return shipCost; }
	public ResourceMap getCityCost() { return cityCost; }
	public ResourceMap getCityWallCost() { return cityWallCost; }


}
