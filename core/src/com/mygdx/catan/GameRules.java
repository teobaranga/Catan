package com.mygdx.catan;

import com.mygdx.catan.enums.FishTokenType;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;
import com.mygdx.catan.gameboard.Hex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
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
	private ResourceMap cityCostWithMedicine = new ResourceMap();
	private ResourceMap buildBasicKnightCost = new ResourceMap();
	private ResourceMap activateKnightCost = new ResourceMap();
	private ResourceMap promoteKnightCost = new ResourceMap();

	private EnumMap<ProgressCardType, Integer> progressCardOccurences = new EnumMap<ProgressCardType, Integer>(ProgressCardType.class);
	private EnumMap<ProgressCardType, ProgressCardKind> progressCardKind = new EnumMap<ProgressCardType, ProgressCardKind>(ProgressCardType.class);
	private EnumMap<FishTokenType, Integer> fishTokenOccurences = new EnumMap<FishTokenType, Integer>(FishTokenType.class);
	private HashMap<Integer,TerrainKind> defaultTerrainKindMap = new HashMap<Integer,TerrainKind>();
	private HashMap<Integer,Integer> defaultDiceNumberMap = new HashMap<Integer,Integer>();
	private HashMap<Integer,HarbourKind> defaultHarbourMap = new HashMap<Integer,HarbourKind>();
	private HashMap<TerrainKind, ResourceKind> producingResourcemap = new HashMap<>();

	private ArrayList<Pair<Integer, Integer>> smallFisheryPosition = new ArrayList<>();

	private int vpToWin = 13;
	private int settlementVp = 1;
	private int cityVp = 2;
	private int metropolisVp = 2;

	public static final int SIZE = 7;

	// private int genericTradeRatio = 4;
	// private int specialTradeRation = 3;

	private static GameRules aGameRules = new GameRules();

	private GameRules() {
		initializeProgressCards();
		initializeFishTokens();
		setupDefaultTerrainMap();
		setupDefaultDiceMap();
		setupDefaultHarbourMap();
        setupDefaultSmallFisheryPair();

		setupSettlementCost();
		setupRoadCost();
		setupShipCost();
		setupCityCost();
		setupCityWallCost();
		setupCityCostWithMedicine();
		setupBuildBasicKnightCost();
		setupActivateKnightCost();
		setupPromoteKnightCost();
		setupProducingResourcesmap();
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

	private void setupCityCostWithMedicine(){
		cityCostWithMedicine.put(ResourceKind.GRAIN, 1);
		cityCostWithMedicine.put(ResourceKind.ORE, 2);
	}

	private void setupCityWallCost(){
		cityWallCost.put(ResourceKind.BRICK, 2);
	}

	private void setupBuildBasicKnightCost() {
		buildBasicKnightCost.put(ResourceKind.WOOL, 1);
		buildBasicKnightCost.put(ResourceKind.ORE, 1);
	}

	private void setupActivateKnightCost() {
		activateKnightCost.put(ResourceKind.GRAIN, 1);
	}

	private void setupPromoteKnightCost() {
		promoteKnightCost.put(ResourceKind.WOOL, 1);
		promoteKnightCost.put(ResourceKind.ORE, 1);
	}

	private void setupDefaultSmallFisheryPair() {
	    smallFisheryPosition.add(new ImmutablePair<>(-7,1));
        smallFisheryPosition.add(new ImmutablePair<>(7,1));
        smallFisheryPosition.add(new ImmutablePair<>(-7,-1));
        smallFisheryPosition.add(new ImmutablePair<>(7,-1));
        smallFisheryPosition.add(new ImmutablePair<>(0,4));
        smallFisheryPosition.add(new ImmutablePair<>(2,4));
    }
	
	private void setupProducingResourcesmap() {
	    producingResourcemap.put(TerrainKind.FIELDS, ResourceKind.GRAIN);
	    producingResourcemap.put(TerrainKind.FOREST, ResourceKind.WOOD);
	    producingResourcemap.put(TerrainKind.HILLS, ResourceKind.BRICK);
	    producingResourcemap.put(TerrainKind.MOUNTAINS, ResourceKind.ORE);
	    producingResourcemap.put(TerrainKind.PASTURE, ResourceKind.WOOL);
	}

    public Pair getSmallFisheryPair(int index) {
	    return smallFisheryPosition.get(index);
    }

	/**
	 * hardcodes the default terrainKind map (each integer represents the hashcode of the CoordinatePair with coordinates x and y (assumes size <= 7)
	 * */
	private void setupDefaultTerrainMap() {
        defaultTerrainKindMap.put(getHashCodeofPair(-7,1), TerrainKind.SMALL_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(7,1), TerrainKind.SMALL_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(-7,-1), TerrainKind.SMALL_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(7,-1), TerrainKind.SMALL_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(0,4), TerrainKind.SMALL_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(2,4), TerrainKind.SMALL_FISHERY);

        defaultTerrainKindMap.put(getHashCodeofPair(0,-2), TerrainKind.BIG_FISHERY);
        defaultTerrainKindMap.put(getHashCodeofPair(-3,-3), TerrainKind.FIELDS);
		defaultTerrainKindMap.put(getHashCodeofPair(1,-3), TerrainKind.HILLS);
  	  	defaultTerrainKindMap.put(getHashCodeofPair(3,-3), TerrainKind.SEA);
		defaultTerrainKindMap.put(getHashCodeofPair(-4,-2), TerrainKind.FOREST);
		defaultTerrainKindMap.put(getHashCodeofPair(-2,-2), TerrainKind.MOUNTAINS);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,-3), TerrainKind.PASTURE);
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
        defaultDiceNumberMap.put(getHashCodeofPair(0,-2), 0);
        defaultDiceNumberMap.put(getHashCodeofPair(-7,1), 6);
        defaultDiceNumberMap.put(getHashCodeofPair(7,1), 8);
        defaultDiceNumberMap.put(getHashCodeofPair(-7,-1), 5);
        defaultDiceNumberMap.put(getHashCodeofPair(7,-1), 9);
        defaultDiceNumberMap.put(getHashCodeofPair(0,4), 4);
        defaultDiceNumberMap.put(getHashCodeofPair(2,4), 10);

        defaultDiceNumberMap.put(getHashCodeofPair(-3,-3), 6);
        defaultDiceNumberMap.put(getHashCodeofPair(-1,-3), 10);
        defaultDiceNumberMap.put(getHashCodeofPair(1,-3), 8);
        defaultDiceNumberMap.put(getHashCodeofPair(3,-3), 0);
        defaultDiceNumberMap.put(getHashCodeofPair(-4,-2), 5);
        defaultDiceNumberMap.put(getHashCodeofPair(-2,-2), 3);
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
	 * initializes progress card occurences and kind
	 * */
	private void initializeProgressCards() {
		progressCardOccurences.put(ProgressCardType.ALCHEMIST, 2);
		progressCardKind.put(ProgressCardType.ALCHEMIST, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.BISHOP, 2);
		progressCardKind.put(ProgressCardType.BISHOP, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.COMMERCIALHARBOUR, 2);
		progressCardKind.put(ProgressCardType.COMMERCIALHARBOUR, ProgressCardKind.TRADE);

		progressCardOccurences.put(ProgressCardType.CONSTITUTION, 1);
		progressCardKind.put(ProgressCardType.CONSTITUTION, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.CRANE, 2);
		progressCardKind.put(ProgressCardType.CRANE, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.DESERTER, 2);
		progressCardKind.put(ProgressCardType.DESERTER, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.DIPLOMAT, 2);
		progressCardKind.put(ProgressCardType.DIPLOMAT, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.ENGINEER, 1);
		progressCardKind.put(ProgressCardType.ENGINEER, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.INTRIGUE, 2);
		progressCardKind.put(ProgressCardType.INTRIGUE, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.INVENTOR, 2);
		progressCardKind.put(ProgressCardType.INVENTOR, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.IRRIGATION, 2);
		progressCardKind.put(ProgressCardType.IRRIGATION, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.MASTERMERCHANT, 2);
		progressCardKind.put(ProgressCardType.MASTERMERCHANT, ProgressCardKind.TRADE);

		progressCardOccurences.put(ProgressCardType.MEDICINE, 2);
		progressCardKind.put(ProgressCardType.MEDICINE, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.MERCHANTFLEET, 2);
		progressCardOccurences.put(ProgressCardType.MINING, 2);
		progressCardKind.put(ProgressCardType.MINING, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.PRINTER, 1);
		progressCardKind.put(ProgressCardType.PRINTER, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.RESOURCEMONOPOLY, 4);
		progressCardKind.put(ProgressCardType.RESOURCEMONOPOLY, ProgressCardKind.TRADE);

		progressCardOccurences.put(ProgressCardType.ROADBUILDING, 2);
		progressCardKind.put(ProgressCardType.ROADBUILDING, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.SABOTEUR, 2);
		progressCardKind.put(ProgressCardType.SABOTEUR, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.SMITH, 2);
		progressCardKind.put(ProgressCardType.SMITH, ProgressCardKind.SCIENCE);

		progressCardOccurences.put(ProgressCardType.SPY, 3);
		progressCardKind.put(ProgressCardType.SPY, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.TRADEMONOPOLY, 2);
		progressCardKind.put(ProgressCardType.TRADEMONOPOLY, ProgressCardKind.TRADE);

		progressCardOccurences.put(ProgressCardType.WARLORD, 2);
		progressCardKind.put(ProgressCardType.WARLORD, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.WEDDING, 2);
		progressCardKind.put(ProgressCardType.WEDDING, ProgressCardKind.POLITICS);

		progressCardOccurences.put(ProgressCardType.MERCHANT, 6);
		progressCardKind.put(ProgressCardType.MERCHANT, ProgressCardKind.TRADE);
	}

	/**
	 *	Initializes the occurrences of each FishToken type.
	 */
	private void initializeFishTokens() {
		fishTokenOccurences.put(FishTokenType.ONE_FISH, 11);
		fishTokenOccurences.put(FishTokenType.TWO_FISH, 10);
		fishTokenOccurences.put(FishTokenType.THREE_FISH, 8);
		fishTokenOccurences.put(FishTokenType.OLD_BOOT, 1);
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
     * @param p x/y coordinates
     * @return the hashCode of the CoordinatePair <x,y>
     */
    private int getHashCodeofPair(Pair<Integer, Integer> p) {
	    return (p.getKey() + 30) * 10 + (p.getValue() + 30);
    }

    public ResourceKind getProducingResource(TerrainKind kind) {
        return producingResourcemap.get(kind);
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
	public int getProgressCardOccurrence(ProgressCardType pKind) {
		return progressCardOccurences.get(pKind);
	}

	/**
	 * @param
	 * @return kind of pType in the progress card stack
	 * */
	public ProgressCardKind getProgressCardKind(ProgressCardType pType) {
		return progressCardKind.get(pType);
	}

	public int getFishTokenOccurrence(FishTokenType fType) {
	    return fishTokenOccurences.get(fType);
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

	public ResourceMap getRoadCost(ProgressCardType type) {
	    if (type == null) {
            return roadCost;
        }
	    switch (type) {
	    case ROADBUILDING:
	        return new ResourceMap();
        default:
            return roadCost;
	    }
	}

	public ResourceMap getShipCost(ProgressCardType type) {
	    if (type == null) {
            return shipCost;
        }
	    switch (type) {
	    case ROADBUILDING:
	        return new ResourceMap();
        default:
            return shipCost;
	    }
	}

	public ResourceMap getCityCost(ProgressCardType type) {
	    if (type == null) {
	        return cityCost;
	    }
	    switch (type) {
	    case MEDICINE:
	        return cityCostWithMedicine;
	    default :
	        return cityCost;
	    }
	}

	public ResourceMap getCityWallCost(ProgressCardType type) {
	    if (type == null) {
            return cityWallCost;
        }
	    switch (type) {
	    case ENGINEER:
	        return new ResourceMap();
        default:
            return cityWallCost;
	    }
	}

	public ResourceMap getbuildBasicKnightCost() {
		return buildBasicKnightCost;
	}

	public ResourceMap getActivateKnightCost(ProgressCardType type) {
		if (type == null) {
			return activateKnightCost;
		}
		switch (type) {
			case WARLORD:
				return new ResourceMap();
			default:
				return activateKnightCost;
		}
	}

	public ResourceMap getPromoteKnightCost(ProgressCardType type) {
	    if (type == null) {
	        return promoteKnightCost;
        }
        switch (type) {
            case SMITH:
                return new ResourceMap();
            default:
                return promoteKnightCost;
        }
    }


}
