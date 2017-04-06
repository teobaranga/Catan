package com.mygdx.catan.gameboard;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.enums.ProgressCardKind;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.TerrainKind;
import com.mygdx.catan.enums.FishTokenType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class GameBoard {
	
	private GameRules gameRules;

	private ArrayList<Hex> hexes;
	private ArrayList<CoordinatePair> aIntersectionPositions;			// Villages will be available through the intersection positions
	private ArrayList<EdgeUnit> aRoadsAndShips;
	private Hex aRobberPosition;
	private Hex aMerchantPosition;
	private Player aMerchantOwner;
	private Player aBootOwner;
	private Stack<ProgressCardType> aScienceProgressCardStack;
	private Stack<ProgressCardType> aPoliticsProgressCardStack;
	private Stack<ProgressCardType> aTradeProgressCardStack;
	private Stack<FishTokenType> aFishTokenStack;
	private List<Village> villages;
	private ArrayList<Knight> knights;
	
	private final int SIZE = GameRules.getGameRulesInstance().getSize();
	
	public GameBoard() throws Exception {
		gameRules = GameRules.getGameRulesInstance();
		hexes = new ArrayList<>();
		aIntersectionPositions = new ArrayList<>();
		aRoadsAndShips = new ArrayList<>();
		villages = new ArrayList<>();
		knights = new ArrayList<>();
        aScienceProgressCardStack = new Stack<>();
        aPoliticsProgressCardStack = new Stack<>();
        aTradeProgressCardStack = new Stack<>();
        aFishTokenStack = new Stack<>();

		HashMap<Integer,TerrainKind> aHexKindSetup = GameRules.getGameRulesInstance().getDefaultTerrainKindMap();
		HashMap<Integer,Integer> aDiceNumberSetup = GameRules.getGameRulesInstance().getDefaultDiceNumberMap();
		
        int half = SIZE / 2;

        // initialize hex position coordinates, where x=(aHexPositions[i].getLeft()) and y=(aHexPositions[i].getRight())
        // the coordinates describe the offset from the center.

        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = -cols + 2 * col + 1;
                int y = (row - half);
                CoordinatePair hexCoord = CoordinatePair.of(x, y, HarbourKind.NONE);

                // gets the dice number and terrain kind from the hard coded maps in GameRules
                Integer lDiceNumber = aDiceNumberSetup.get(hexCoord.hashCode());
                TerrainKind lTerrainKind = aHexKindSetup.get(hexCoord.hashCode());

                // Creates and adds the Hex. If key was not hard coded (or hard coded wrong) the hex is not added and an exception is thrown
                if (lDiceNumber != null || lTerrainKind != null) {
                    // multiple dice numbers on the same tile. The dice numbers are constant.
                    if ( lTerrainKind == TerrainKind.BIG_FISHERY){
                        hexes.add(new Hex(hexCoord,lTerrainKind,2));
                        hexes.add(new Hex(hexCoord,lTerrainKind,3));
                        hexes.add(new Hex(hexCoord,lTerrainKind,11));
                        hexes.add(new Hex(hexCoord,lTerrainKind,12));
                    } else {
                        Hex hex = new Hex(hexCoord,lTerrainKind,lDiceNumber);
                        hexes.add(hex);
                    }
                    // NOTE: Robber now starts out of the board
                } else {
                    throw new Exception("Mismatch with GameRules");
                }

                /*Creates the intersections*/

                if (y < 0) { // top half of board
                    // Creates the top, and top left points adjacent to current hex
                    aIntersectionPositions.add(CoordinatePair.of(x-1,y*3 - 1,GameRules.getGameRulesInstance().getHarbourKind(x-1,y*3 - 1)));
                    aIntersectionPositions.add(CoordinatePair.of(x, y*3 - 2,GameRules.getGameRulesInstance().getHarbourKind(x,y*3 - 2)));
                    // If the hex is the last column of a row, create the top right point
                    if (col == cols - 1) {
                        aIntersectionPositions.add(CoordinatePair.of(x+1,3*y-1,GameRules.getGameRulesInstance().getHarbourKind(x+1,3*y-1)));
                    }
                } else if (y == 0) { // middle
                    // Creates the top, and top left points adjacent to current hex
                    aIntersectionPositions.add(CoordinatePair.of(x-1,y*3 - 1,GameRules.getGameRulesInstance().getHarbourKind(x-1,3*y-1)));
                    aIntersectionPositions.add(CoordinatePair.of(x, y*3 - 2,GameRules.getGameRulesInstance().getHarbourKind(x,3*y-2)));
                    // Creates the bottom, and bottom left points adjacent to current hex
                    aIntersectionPositions.add(CoordinatePair.of(x-1,y*3 + 1,GameRules.getGameRulesInstance().getHarbourKind(x-1,3*y+1)));
                    aIntersectionPositions.add(CoordinatePair.of(x, y*3 + 2,GameRules.getGameRulesInstance().getHarbourKind(x,3*y+2)));
                    // If the hex is the last column of a row, create the top right and bottom right points
                    if (col == cols - 1) {
                        aIntersectionPositions.add(CoordinatePair.of(x+1,3*y-1,GameRules.getGameRulesInstance().getHarbourKind(x+1,3*y-1)));
                        aIntersectionPositions.add(CoordinatePair.of(x+1,3*y+1,GameRules.getGameRulesInstance().getHarbourKind(x+1,3*y+1)));
                    }
                } else { // bottom half of board
                    // Creates the bottom, and bottom left points adjacent to current hex
                    // FIXME: change to same HarbourKind as above once hashCode function has been fixed
                    aIntersectionPositions.add(CoordinatePair.of(x-1,y*3 + 1,HarbourKind.NONE));
                    aIntersectionPositions.add(CoordinatePair.of(x, y*3 + 2,HarbourKind.NONE));
                    // If the hex is the last column of a row, create the bottom right point
                    if (col == cols - 1) {
                        aIntersectionPositions.add(CoordinatePair.of(x+1,3*y+1,HarbourKind.NONE));
                    }
                }
            }
        }

        // Initialize the Side Fisheries
        for(int i = 0; i < 6; i++) {
            Pair<Integer,Integer> currentPair = GameRules.getGameRulesInstance().getSmallFisheryPair(i);
            CoordinatePair hexCoord = CoordinatePair.of(currentPair.getLeft(), currentPair.getRight(), HarbourKind.NONE);
            Integer lDiceNumber = aDiceNumberSetup.get(hexCoord.hashCode());
            TerrainKind lTerrainKind = aHexKindSetup.get(hexCoord.hashCode());

            if (lDiceNumber != null || lTerrainKind != null) {
                Hex hex = new Hex(hexCoord,lTerrainKind,lDiceNumber);
                hexes.add(hex);
                // Robber now starts out of the board
            } else {
                throw new Exception("Mismatch with GameRules");
            }
        }

        // Fills up the progress card stack and shuffles it
        for (ProgressCardType type : ProgressCardType.values()) {
        	int occurrence = gameRules.getProgressCardOccurrence(type);
        	ProgressCardKind kind = gameRules.getProgressCardKind(type);
        	for (int i = 0; i<occurrence; i++) {
        		if (kind == ProgressCardKind.POLITICS) {
        			aPoliticsProgressCardStack.push(type);
        		} else if (kind == ProgressCardKind.TRADE) {
        			aTradeProgressCardStack.push(type);
        		} else if (kind == ProgressCardKind.SCIENCE) {
        			aScienceProgressCardStack.push(type);
        		}
        	}
        }
        Collections.shuffle(aScienceProgressCardStack);
        Collections.shuffle(aTradeProgressCardStack);
        Collections.shuffle(aPoliticsProgressCardStack);

        //fills the FishToken stack and shuffles it
        fillFishTokenStack();
        Collections.shuffle(aFishTokenStack);

	}

	private void fillFishTokenStack() {
        for (FishTokenType type : FishTokenType.values()) {
            int occurrence = gameRules.getFishTokenOccurrence(type);
            for (int i = 0; i<occurrence; i++) {
                aFishTokenStack.push(type);
            }
        }
    }

	public int getNumberofHexes(){
		return hexes.size();
	}

	public Iterator<Hex> getHexIterator() {
		return hexes.iterator();
	}
	
	public ArrayList<CoordinatePair> getIntersectionsAndEdges() {
		return aIntersectionPositions;
	}
	
	public ArrayList<Hex> getHexes() {
		return hexes;
	}

	public void setMerchantOwner(Player newOwner) {
	    aMerchantOwner = newOwner;
	}
	
	public Player getMerchantOwner() {
	    return aMerchantOwner;
	}

	public void setaBootOwner(Player newOwner) {
		aBootOwner = newOwner;
	}

	public Player getaBootOwner() {
		return aBootOwner;
	}

	/**
	 * @return top card of the science progress card stack. returns null if empty
	 * */
	public ProgressCardType popScienceProgressCardStack() {
		return aScienceProgressCardStack.pop();
	}
	
	/**
	 * @return top card of the politics progress card stack. returns null if empty
	 * */
	public ProgressCardType popPoliticsProgressCardStack() {
		return aPoliticsProgressCardStack.pop();
	}
	
	/**
	 * @return top card of the trade progress card stack. returns null if empty
	 * */
	public ProgressCardType popTradeProgressCardStack() {
		return aTradeProgressCardStack.pop();
	}

    public void removeProgressCard(ProgressCardType cardType, ProgressCardKind cardKind) {
		if (cardKind == ProgressCardKind.POLITICS) {
			aPoliticsProgressCardStack.remove(cardType);
		} else if (cardKind == ProgressCardKind.SCIENCE) {
			aScienceProgressCardStack.remove(cardType);
		} else {
			aTradeProgressCardStack.remove(cardType);
		}
	}

	/**
     * @return top card of the Fish Token stack, null if empty
     */
	public FishTokenType popFishTokenStack() {
	    if (aFishTokenStack.empty()) {
	        fillFishTokenStack();
	        Collections.shuffle(aFishTokenStack);
        }
	    return aFishTokenStack.pop();
	}

    /**
     * Sets robber to newRobberPosition
     *
     * @param newRobberPosition Hex for the new robber position
     */
    public void setRobberPosition(Hex newRobberPosition) {
        if (newRobberPosition != null) {
            aRobberPosition = newRobberPosition;
        }
    }

    /**
	 * @return Hex: robber position. If null some error occured during Game Board setup.
	 * */
	public Hex getRobberPosition() {
		return aRobberPosition;
	}

	/**
	 * Sets merchant to newMerchantPosition
	 *
	 * @param newMerchantPosition Hex for the new merchant position
	 */
	public void setMerchantPosition(Hex newMerchantPosition) {
		if (newMerchantPosition != null) {
			aMerchantPosition = newMerchantPosition;
		}
	}

	/**
	 * @return Hex: robber position. If null Merchant not in play yet.
	 * */
	public Hex getMerchantPosition() {
		return aMerchantPosition;
	}

	/**
	 * @param intersection position of intersection
	 * @param owner        player whose edge unit needs to be adjacent to given intersection
	 * @return true if given player has an edge unit with end point at given intersection
	 */
	public boolean isAdjacentToEdgeUnit(CoordinatePair intersection, Player owner) {
		for (EdgeUnit roadOrShip : aRoadsAndShips) {
			if (roadOrShip.hasEndpoint(intersection)) {
				if (roadOrShip.getOwner().equals(owner)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addRoadOrShip(EdgeUnit eu) {
		aRoadsAndShips.add(eu);
	}
	
	public void removeRoadOrShip(EdgeUnit eu) {
	    aRoadsAndShips.remove(eu);
	}
	
	public ArrayList<EdgeUnit> getRoadsAndShips() {
		return aRoadsAndShips;
	}

	/** Add a village to the list of villages on the game board */
	void addVillage(Village village) {
	    villages.add(village);
    }

    /** Get the list of villages on the game board */
    List<Village> getVillages() {
	    return villages;
    }

	void addKnight(Knight knight) {
		knights.add(knight);
	}

	public List<Knight> getKnights() {
		return knights;
	}
}
