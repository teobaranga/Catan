package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Request indicating that the sender player rolled the dice.
 */
public class RollTwoDice extends ForwardedRequest {

    /** Pair of integers representing the red and yellow die values */
    private Pair<Integer, Integer> rollResult;

    public static RollTwoDice newInstance(Pair<Integer, Integer> result, String username) {
        RollTwoDice request = new RollTwoDice();
        request.rollResult = result;
        request.username = username;
        request.universal = true;
        return request;
    }

    public Pair<Integer, Integer> getRollResult() {
        return rollResult;
    }
}
