package com.smartshop.erp.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }
}
