package com.example.iotalarmcopilot.shared;

/**
 * 领域基础异常类
 */
public class BaseDomainException extends RuntimeException {

    public BaseDomainException(String message) {
        super(message);
    }
}

