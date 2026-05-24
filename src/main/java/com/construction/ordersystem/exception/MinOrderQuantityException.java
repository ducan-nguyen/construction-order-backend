package com.construction.ordersystem.exception;

@SuppressWarnings("serial")
public class MinOrderQuantityException extends RuntimeException {
    public MinOrderQuantityException(String message) {
        super(message);
    }
}