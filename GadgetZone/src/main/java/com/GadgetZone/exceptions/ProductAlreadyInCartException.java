package com.GadgetZone.exceptions;

public class ProductAlreadyInCartException extends RuntimeException {
    public ProductAlreadyInCartException() {
        super("Product is already in the cart");
    }

    public ProductAlreadyInCartException(String message) {
        super(message);
    }

    public ProductAlreadyInCartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductAlreadyInCartException(Throwable cause) {
        super(cause);
    }
}
