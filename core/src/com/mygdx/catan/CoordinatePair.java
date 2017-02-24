package com.mygdx.catan;

import org.apache.commons.lang3.tuple.Pair;


@SuppressWarnings("serial")
public class CoordinatePair<L,R> extends Pair<L,R>{

	  private final L left;
	  private final R right;

	  public CoordinatePair(L pleft, R pright) {
		super();
		left = pleft;
		right = pright;
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

}