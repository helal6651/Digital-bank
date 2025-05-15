package com.apigateway.exceptions;

import java.io.Serial;


public class InvalidJwtToken extends RuntimeException {
    /**
     * Serial version UID for ensuring consistent serialization.
     */
    @Serial
    private static final long serialVersionUID = -294671188037098603L;

    public InvalidJwtToken(String msg) {
        super (msg);
    }
}
