package com.lpi.admissionscommittee.exceptions;

public class NoSuchRequestException extends RuntimeException {
    public NoSuchRequestException(String message) {
        super(message);
    }
}
