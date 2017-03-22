package com.mygdx.catan.request;

import com.mygdx.catan.Player;

/**
 * Requests the server to tell every client to update the Old Boot Owner.
 */
public class UpdateOldBoot extends ForwardedRequest {

    private Player player;

    public Player getPlayer() {
        return player;
    }

    public static UpdateOldBoot newInstance(Player player) {
        UpdateOldBoot request = new UpdateOldBoot();
        request.username = player.getUsername();
        request.player = player;
        request.universal = true;
        return request;
    }
}
