package com.mygdx.catan.request;

import com.mygdx.catan.enums.EventKind;

public class DrawProgressCard extends ForwardedRequest {
    
    private int redDie;
    private EventKind event;
    
    public static DrawProgressCard newInstance(EventKind event, int redDie, String username) {
        DrawProgressCard request = new DrawProgressCard();
        
        request.username = username;
        request.redDie = redDie;
        request.event = event;
        request.universal = true;
        
        return request;
    }
    
    public EventKind getEventKind() {
        return event;
    }
    
    public int getRedDie() {
        return redDie;
    }

}
