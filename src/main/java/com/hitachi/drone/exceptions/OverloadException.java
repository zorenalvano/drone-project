package com.hitachi.drone.exceptions;

public class OverloadException extends RuntimeException {
    public OverloadException(Double weightLimit) {
        super(String.format("Unable to load. Load exceeds weight limit of %s", weightLimit));
    }
}
