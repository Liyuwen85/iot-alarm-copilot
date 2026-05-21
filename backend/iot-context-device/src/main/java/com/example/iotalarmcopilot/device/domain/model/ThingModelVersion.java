package com.example.iotalarmcopilot.device.domain.model;

import com.example.iotalarmcopilot.BaseDomainException;

/**
 * 物模型版本
 */
public record ThingModelVersion(int value) {

    public ThingModelVersion {
        if (value <= 0) {
            throw new BaseDomainException("thingModelVersion must be greater than 0");
        }
    }
}
