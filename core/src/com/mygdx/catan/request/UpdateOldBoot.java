package com.mygdx.catan.request;

/**
 * Requests the server to tell every client to update the Old Boot Owner.
 */
public class UpdateOldBoot extends ForwardedRequest {

    public static UpdateOldBoot newInstance(String username) {
        UpdateOldBoot request = new UpdateOldBoot();
        request.username = username;
        request.universal = true;
        return request;
    }
}
