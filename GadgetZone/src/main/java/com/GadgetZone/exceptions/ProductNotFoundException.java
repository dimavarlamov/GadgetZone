package com.GadgetZone.exceptions;

public class ProductNotFoundException extends RuntimeException {
    // Конструктор без параметров, вызывающий родительский конструктор с сообщением
    public ProductNotFoundException() {
        super("Product not found");
    }

    // Можно добавить дополнительные конструкторы для более детализированного использования
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Throwable cause) {
        super(cause);
    }
}
