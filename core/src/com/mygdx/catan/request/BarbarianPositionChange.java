package com.mygdx.catan.request;

public class BarbarianPositionChange extends ForwardedRequest {
    
    private int newBarbarianPosition;
    
    public static BarbarianPositionChange newInstance(int newBarbarianPosition, String username) {
        BarbarianPositionChange request = new BarbarianPositionChange();
        
        request.newBarbarianPosition = newBarbarianPosition;
        request.universal = false;
        request.username = username;
        
        return request;
    } 
    
    public int getNewBarbarianPosition() {
        return newBarbarianPosition;
    }

}
