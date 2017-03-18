package com.mygdx.catan.moves;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that describes a succession of Moves, together forming one full move
 * */
public class MultiStepMove {
    private Queue<Move> queueOfMoves;

    public MultiStepMove() {
        queueOfMoves = new LinkedList<Move>();
    }
    
    public void addMove(Move move) {
        queueOfMoves.add(move);
    }
    
    /**
     * Performs the next move in the queue, if empty nothing happens
     * 
     * @param o can be any object, implementation of Move determine more specific type
     * */
    public void performNextMove(Object o) {
        if (queueOfMoves.isEmpty()) {return;}
        queueOfMoves.poll().doMove(o);
    }
    
    public int movesLeft() {
        return queueOfMoves.size();
    }
}
