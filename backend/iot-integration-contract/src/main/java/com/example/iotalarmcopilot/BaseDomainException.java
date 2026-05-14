package com.example.iotalarmcopilot;

/**
 * 领域基础异常类
 */
public class BaseDomainException extends RuntimeException {

    public BaseDomainException(String message) {
        super(message);
    }
}

