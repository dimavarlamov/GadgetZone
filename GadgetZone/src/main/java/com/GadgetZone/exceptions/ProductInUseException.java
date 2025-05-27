package com.GadgetZone.exceptions;

public class ProductInUseException extends RuntimeException {
    public ProductInUseException() {
        super("Product is used in orders");
    }
}