package com.hitachi.drone.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with the given input data %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
