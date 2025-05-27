package com.GadgetZone.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("Insufficient stock");
    }
}