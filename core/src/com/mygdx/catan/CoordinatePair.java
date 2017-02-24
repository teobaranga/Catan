package com.mygdx.catan;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.HarbourKind;
import com.mygdx.catan.gameboard.Village;


@SuppressWarnings("serial")
public class CoordinatePair<L,R> extends Pair<L,R>{

	  private final L left;
	  private final R right;
	  private HarbourKind aHarbourKind;
	  private boolean isOccupied;
	  private Village occupyingVillage;

	  public CoordinatePair(L pleft, R pright, HarbourKind pHarbourKind) {
		super();
		left = pleft;
		right = pright;
		aHarbourKind = pHarbourKind;
		isOccupied = false;
	  }

	    /**
	     * <p>Throws {@code UnsupportedOperationException}.</p>
	     * 
	     * <p>This pair is immutable, so this operation is not supported.</p>
	     *
	     * @param value  the value to set
	     * @return never
	     * @throws UnsupportedOperationException as this operation is not supported
	     */
	    @Override
	    public R setValue(final R value) {
	        throw new UnsupportedOperationException();
	    }

		@Override
		public L getLeft() {
			return left;
		}

		@Override
		public R getRight() {
			return right;
		}
		
	    @Override
	    public int hashCode() {
	        
	        return (getKey() == null ? 0 : getKey().hashCode()*10) +
	                (getValue() == null ? 0 : getValue().hashCode());
	        
	    }
	    
	    public HarbourKind getHarbourKind() {
	    	return aHarbourKind;
	    }
	    
	    public boolean isOccupied(){
	    	return isOccupied;
	    }
	    
	    /**
	     * @return the occupying village, null if this coordinate pair does not represent an intersection, or if the intersection is not occupied
	     * */
	    public Village getOccupyingVillage() {
	    	return occupyingVillage;
	    }
	    
	    /**
	     * puts a village on this coordinate pair. Assert that this CoordinatePair represents an intersection. Does nothing if the intersection is already occupied. Sets the intersection to occupied as a result if successful.
	     * */
	    public void putVillage(Village village) {
	    	if (!isOccupied) {
	    		occupyingVillage = village;
	    		isOccupied = true;
	    	}
	    }
}