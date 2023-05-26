package com.lpi.admissionscommittee.exceptions;

public class NoSuchApplicantException extends RuntimeException{
    public NoSuchApplicantException(String message) {
        super(message);
    }
}
