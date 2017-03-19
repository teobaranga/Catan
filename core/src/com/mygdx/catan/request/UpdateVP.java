package com.mygdx.catan.request;

/**
 * Requests the server to tell every client to update the VPtables.
 */
public class UpdateVP extends ForwardedRequest {

    public static UpdateVP newInstance(String username) {
        UpdateVP request = new UpdateVP();
        request.username = username;
        request.universal = true;
        return request;
    }
}
