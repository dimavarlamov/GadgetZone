package com.GadgetZone.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}