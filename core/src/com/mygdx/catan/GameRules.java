package com.mygdx.catan;

import java.util.EnumMap;
import java.util.HashMap;

import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.VillageKind;

/**
 * Singleton object describing the game rules
 * */
public class GameRules {
	
	private EnumMap<ProgressCardKind, Integer> progressCardOccurences = new EnumMap<ProgressCardKind, Integer>(ProgressCardKind.class);
	private HashMap<Integer,TerrainKind> defaultTerrainKindMap = new HashMap<Integer,TerrainKind>();
	private HashMap<Integer,Integer> defaultDiceNumberMap = new HashMap<Integer,Integer>();
	
	private int vpToWin = 13;
	private int settlementVp = 1;
	private int cityVp = 2;
	private int metropolisVp = 2;
	
	private final int SIZE = 7;

	// private int genericTradeRatio = 4;
	// private int specialTradeRation = 3;
	
	private static GameRules aGameRules = new GameRules();
	
	private GameRules() {
		// hardcodes the progress card occurences
		//TODO: add a map from each of the following progress card kinds to integer
		for (ProgressCardKind pck : ProgressCardKind.values()) {
			switch (pck) {
			case ARECHEMIST:
				break;
			case BISHOP:
				break;
			case COMMERCIALHARBOUR:
				break;
			case CONSTITUTION:
				break;
			case CRANE:
				break;
			case DESERTER:
				break;
			case DIPLOMAT:
				break;
			case ENGINEER:
				break;
			case INTRIGUE:
				break;
			case INVENTOR:
				break;
			case IRRIGATION:
				break;
			case MASTERMERCHANT:
				break;
			case MEDICINE:
				break;
			case MERCHENTFLEET:
				break;
			case MINING:
				break;
			case PRINER:
				break;
			case RESOURCEMONOPOLY:
				break;
			case ROADBUILDING:
				break;
			case SABOTEUR:
				break;
			case SMITH:
				break;
			case SPY:
				break;
			case TRADEMONOPOLY:
				break;
			case WARLORD:
				break;
			case WEDDING:
				break;
			default:
				break;
			}
		}
			
		//hardcodes the default terrainKind map (each integer represents the hashcode of the CoordinatePair with coordinates x and y (assumes size <= 7)
		defaultTerrainKindMap.put(getHashCodeofPair(-3,-3), TerrainKind.FIELDS);
		defaultTerrainKindMap.put(getHashCodeofPair(-1,-3), TerrainKind.FIELDS);
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


		//TODO:hardcode default dice number map (keys exactly as above) (assumes size <= 7)
		defaultDiceNumberMap.put(getHashCodeofPair(-3,-3), 6);
		defaultDiceNumberMap.put(getHashCodeofPair(-1,-3), 11);
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
	 * @param left coordinate
	 * @param right coordinate
	 * @return the hashCode of the CoordinatePair <x,y>
	 * */
	private int getHashCodeofPair(int x, int y) {
		return x*10+y;
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
		case SCIENCEMETROPOLE:
			return metropolisVp;
		case SETTLEMENT:
			return settlementVp;
		case TRADEMETROPLE:
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
}
