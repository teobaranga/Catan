package com.mygdx.catan.request;

import com.mygdx.catan.enums.ProgressCardType;

public class TakeProgressCard extends TargetedRequest {
    
    private ProgressCardType progressCard;
    
    /**
     * @param resources to give to target player
     * @param sender (username) of resources
     * @param target (username) of resources
     * */
    public static TakeProgressCard newInstance(ProgressCardType progressCard, String sender, String target) {
        TakeProgressCard request = new TakeProgressCard();
        request.progressCard = progressCard;
        request.sender = sender;
        request.target = target;
        
        return request;
    }
    
    public ProgressCardType getProgressCard() {
        return progressCard;
    }
}
